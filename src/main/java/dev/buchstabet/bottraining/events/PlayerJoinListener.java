package dev.buchstabet.bottraining.events;

import dev.buchstabet.bottraining.BotTraining;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerJoinListener implements Listener {

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {

    e.getPlayer().getInventory().setContents(BotTraining.getInstance().getDefaultInventory());
    e.setJoinMessage(null);


    e.getPlayer().setPlayerTime(7300, false);
    BotTraining.getInstance().getPacketReader().inject(e.getPlayer());
    BotTraining.getInstance().getCombatHandler().getCombats().forEach((uuid, combat) -> {
      combat.getItems().forEach(item -> ((CraftPlayer) e.getPlayer()).getHandle().b.sendPacket(new PacketPlayOutEntityDestroy(item.getEntityId())));
      ((CraftPlayer) e.getPlayer()).getHandle().b.sendPacket(new PacketPlayOutEntityDestroy(combat.getEntity().getEntityId()));
    });
  }

  @EventHandler
  public void onLogin(PlayerLoginEvent e) {
    BotTraining.getInstance().getCombatHandler().getCombats().values().forEach(combat -> combat.getPlayer().hidePlayer(BotTraining.getInstance(), e.getPlayer()));
  }

}
