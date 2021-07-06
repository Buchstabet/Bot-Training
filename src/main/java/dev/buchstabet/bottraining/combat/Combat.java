package dev.buchstabet.bottraining.combat;

import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import dev.buchstabet.bottraining.BotTraining;
import dev.buchstabet.bottraining.npc.NPC;
import dev.buchstabet.bottraining.utils.Reflections;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Combat extends Reflections {

  private final NPC npc;
  private final Player player;
  private final List<BukkitTask> bukkitTasks = new ArrayList<>();
  private final Zombie entity;
  private final List<Item> items = new ArrayList<>();
  private final int attackDamage;
  private final boolean souping;

  public Combat(Player player) {
    this(player, getTextures(player), BotTraining.getInstance().getBotName(), 7, true);
  }

  public Combat(Player player, String botName) {
    this(player, getTextures(player), botName, 7, true);
  }

  public boolean isSouping() {
    return souping;
  }

  public Combat(Player player, boolean souping) {
    this(player, getTextures(player), BotTraining.getInstance().getBotName(), 4, souping);
  }

  public Combat(Player player, String botName, int attackDamage) {
    this(player, getTextures(player), botName, attackDamage, true);
  }

  public Combat(Player player, String botName, int attackDamage, boolean souping) {
    this(player, getTextures(player), botName, attackDamage, souping);
  }

  public Combat(Player player, String[] textures, String botName, int attackDamage, boolean souping) throws IllegalStateException {
    this.attackDamage = attackDamage;
    this.souping = souping;
    if (BotTraining.getInstance().getCombatHandler().isInCombat(player)) {
      throw new IllegalStateException("The player " + player.getName() + " is already in a fight");
    }

    this.player = player;
    this.npc = new NPC(botName, player.getLocation(), textures);

    Bukkit.getOnlinePlayers().forEach(target -> {
      if (!target.equals(player)) {
        player.hidePlayer(BotTraining.getInstance(), target);
      }
    });

    this.entity = (Zombie) player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);
    this.entity.setTarget(player);
    this.entity.setSilent(true);
    this.entity.setMaxHealth(20);
    this.entity.setHealth(20);
    this.entity.setNoDamageTicks(10);
    this.entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false));
    PacketPlayOutEntityDestroy packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entity.getEntityId());
    sendPacket(packetPlayOutEntityDestroy);

    npc.spawn(player);

    bukkitTasks.add(Bukkit.getScheduler().runTaskTimerAsynchronously(BotTraining.getInstance(), () -> {
      npc.teleport(entity.getLocation(), player);
      npc.rotateHead(player);
    }, 1, 1));

    List<Pair<EnumItemSlot, ItemStack>> pairs = new ArrayList<>();
    pairs.add(new Pair<>(EnumItemSlot.a, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.IRON_SWORD))));
    PacketPlayOutEntityEquipment packetPlayOutEntityEquipment = new PacketPlayOutEntityEquipment(npc.getEntityPlayer().getId(),
            pairs);
    sendPacket(packetPlayOutEntityEquipment, player);
    startSoup();

    loadInventory();
  }

  private void loadInventory() {
    org.bukkit.inventory.ItemStack[] contents = isSouping() ? BotTraining.getInstance().getKitHandler().getSoupKit() : BotTraining.getInstance().getKitHandler().getNormalKit();
    player.getInventory().setContents(contents);
  }

  public List<Item> getItems() {
    return items;
  }

  private void startSoup() {
    setTarget(player);

    bukkitTasks.add(Bukkit.getScheduler().runTaskTimer(BotTraining.getInstance(), () -> {
      if (entity.getHealth() <= 0) {
        return;
      }

      if (entity.getHealth() <= entity.getMaxHealth() - 1) {
        entity.setHealth(entity.getHealth() + 1);
      }

      if (souping) {
        List<Pair<EnumItemSlot, ItemStack>> pairs = new ArrayList<>();
        if (entity.getHealth() < 6 && (entity.getLocation().distance(player.getLocation()) > 15 || new Random().nextInt(3) == 1)) {
          pairs.add(new Pair<>(EnumItemSlot.a, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.MUSHROOM_STEW))));
          entity.setHealth(entity.getHealth() + 4);

          Item item = entity.getWorld().dropItemNaturally(entity.getLocation(), new org.bukkit.inventory.ItemStack(Material.BOWL));
          items.add(item);
          PacketPlayOutEntityDestroy packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(item.getEntityId());
          Bukkit.getOnlinePlayers().forEach(target -> {
            if (!target.equals(player)) {
              sendPacket(packetPlayOutEntityDestroy, target);
            }
          });

          Bukkit.getScheduler().runTaskLaterAsynchronously(BotTraining.getInstance(), () -> {
            List<Pair<EnumItemSlot, ItemStack>> p = new ArrayList<>();
            pairs.add(new Pair<>(EnumItemSlot.a, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.IRON_SWORD))));
            PacketPlayOutEntityEquipment packetPlayOutEntityEquipment = new PacketPlayOutEntityEquipment(npc.getEntityPlayer().getId(),
                    p);
            sendPacket(packetPlayOutEntityEquipment, player);
          }, 5);
        } else {
          pairs.add(new Pair<>(EnumItemSlot.a, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.IRON_SWORD))));
        }

        PacketPlayOutEntityEquipment packetPlayOutEntityEquipment = new PacketPlayOutEntityEquipment(npc.getEntityPlayer().getId(),
                pairs);
        sendPacket(packetPlayOutEntityEquipment, player);
      }
    }, 20, 20));
  }

  private void setTarget(LivingEntity entity) {
    this.entity.setTarget(entity);
  }

  public void stop() {
    bukkitTasks.forEach(BukkitTask::cancel);
    items.forEach(Entity::remove);
    entity.remove();
    npc.despawn(player);

    if (!player.isDead()) {
      player.getInventory().setContents(BotTraining.getInstance().getDefaultInventory());
      player.setFoodLevel(20);
      player.setHealth(20);
    }

    Bukkit.getOnlinePlayers().forEach(target -> {
      if (!target.equals(player)) {
        player.showPlayer(BotTraining.getInstance(), target);
      }
    });
  }

  public NPC getNpc() {
    return npc;
  }

  public Player getPlayer() {
    return player;
  }

  public static String[] getTextures(Player player) {
    String[] textures = new String[2];
    for (Property property : ((CraftPlayer) player).getProfile().getProperties().get("textures")) {
      if (!property.getName().equals("textures")) {
        continue;
      }

      textures[0] = property.getValue();
      textures[1] = property.getSignature();
    }
    return textures;
  }

  public int getAttackDamage() {
    return attackDamage;
  }

  public Entity getEntity() {
    return entity;
  }

}
