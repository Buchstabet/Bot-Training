package dev.buchstabet.bottraining.combat;

import com.google.common.collect.Maps;
import dev.buchstabet.bottraining.BotTraining;
import dev.buchstabet.bottraining.utils.Reflections;
import net.minecraft.network.protocol.game.PacketPlayInClientCommand;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;

public class CombatHandler extends Reflections implements Listener {

  private final Map<UUID, Combat> combats = Maps.newConcurrentMap();

  public Combat createCombat(Player player, boolean souping) {
    Combat combat = new Combat(player, souping);
    combats.put(player.getUniqueId(), combat);
    return combat;
  }

  @EventHandler
  public void onDamage(EntityDamageByEntityEvent e) {
    if (e.getDamager() instanceof Player) {
      Player damage = (Player) e.getDamager();
      if (!combats.containsKey(damage.getUniqueId())) {
        e.setCancelled(true);
        return;
      }
    }

    combats.forEach((uuid, combat) -> {
      if (combat.getEntity().equals(e.getDamager())) {
        PacketPlayOutAnimation packetPlayOutAnimation = new PacketPlayOutAnimation(combat.getNpc().getEntityPlayer(),
                0);

        sendPacket(packetPlayOutAnimation, combat.getPlayer());
      }
    });
  }

  @EventHandler
  public void onEntityPotionEffect(EntityPotionEffectEvent e) {
    if (e.getModifiedType().equals(PotionEffectType.WITHER)) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onPickup(EntityPickupItemEvent e) {
    if (!combats.containsKey(e.getEntity().getUniqueId())) {
      e.setCancelled(true);
      return;
    }

    combats.forEach((uuid, combat) -> {
      if (combat.getItems().contains(e.getItem()) && !combat.getPlayer().equals(e.getEntity())) {
        e.setCancelled(true);
      }
    });
  }

  @EventHandler
  public void onDamage(EntityDamageEvent e) {
    if (e.getEntity() instanceof Player) {
      Player player = (Player) e.getEntity();
      if (!combats.containsKey(player.getUniqueId())) {
        e.setCancelled(true);
        return;
      }
    }

    combats.forEach((uuid, combat) -> {
      if (combat.getEntity().equals(e.getEntity())) {
        Entity entity = combat.getEntity();
        if (e.getEntity().equals(entity)) {
          sendPacket(new PacketPlayOutAnimation(combat.getNpc().getEntityPlayer(), 1), combat.getPlayer());
        }
      }
    });
  }

  public Map<UUID, Combat> getCombats() {
    return combats;
  }

  public boolean isInCombat(Player player) {
    return combats.containsKey(player.getUniqueId());
  }

  @EventHandler
  public void onTarget(EntityTargetEvent e) {
    combats.forEach((uuid, combat) -> {
      if (e.getEntity().equals(combat.getEntity())) {
        combat.getPlayer().setNoDamageTicks(15);
        if (e.getTarget() == null) {
          combat.getPlayer().sendMessage(BotTraining.getInstance().getPrefix() + "§cDu sollst nicht wegrennen.");
          e.getEntity().teleport(combat.getPlayer());
        }

        e.setTarget(combat.getPlayer());
      }
    });
  }

  @EventHandler
  public void onFoodLevel(FoodLevelChangeEvent e) {
    if (e.getEntity() instanceof Player) {
      Player player = (Player) e.getEntity();
      if (!combats.containsKey(player.getUniqueId())) {
        e.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onDrop(PlayerDropItemEvent e) {
    if(e.isCancelled())
      return;

    if (!combats.containsKey(e.getPlayer().getUniqueId())) {
      e.setCancelled(true);
      return;
    }

    if (combats.containsKey(e.getPlayer().getUniqueId())) {
      Combat combat = combats.get(e.getPlayer().getUniqueId());
      combat.getItems().add(e.getItemDrop());
      Bukkit.getScheduler().runTaskLaterAsynchronously(BotTraining.getInstance(), () -> {
        PacketPlayOutEntityDestroy packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(e.getItemDrop().getEntityId());
        Bukkit.getOnlinePlayers().forEach(target -> {
          if (!target.equals(e.getPlayer())) {
            sendPacket(packetPlayOutEntityDestroy, target);
          }
        });
      }, 1);
    }
  }

  @EventHandler
  public void onDie(PlayerDeathEvent e) {
    e.setDeathMessage(null);
    PacketPlayInClientCommand packetPlayInClientCommand = new PacketPlayInClientCommand(PacketPlayInClientCommand.EnumClientCommand.a);
    Bukkit.getScheduler().runTaskLater(BotTraining.getInstance(), () -> ((CraftPlayer) e.getEntity()).getHandle().b.a(packetPlayInClientCommand), 3);
  }

  @EventHandler
  public void onRespawn(PlayerRespawnEvent e) {
    e.getPlayer().getInventory().setContents(BotTraining.getInstance().getDefaultInventory());
  }

  @EventHandler
  public void onDie(EntityDeathEvent e) {
    e.getDrops().clear();
    e.setDroppedExp(0);

    combats.forEach(((uuid, combat) -> {
      if (combat.getEntity().equals(e.getEntity())) {
        combat.stop();
        combat.getPlayer().sendMessage(BotTraining.getInstance().getPrefix() + "§aDu hast gegen den Bot gewonnen.");
        combats.remove(uuid);

      } else {
        if (combat.getPlayer().equals(e.getEntity())) {
          combat.stop();
          combats.remove(uuid);
          combat.getPlayer().sendMessage(BotTraining.getInstance().getPrefix() + "§cDu hast gegen deinen Bot verloren.");
        }
      }
    }));
  }

}
