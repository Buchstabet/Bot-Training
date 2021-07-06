package dev.buchstabet.bottraining.utils;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Reflections {

  public void sendPacket(Packet<?> packet) {
    Bukkit.getOnlinePlayers().forEach(player -> {
      PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;
      playerConnection.sendPacket(packet);
    });
  }

  public void sendPacket(Packet<?> packet, Player player) {
    PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().b;
    playerConnection.sendPacket(packet);
  }

}
