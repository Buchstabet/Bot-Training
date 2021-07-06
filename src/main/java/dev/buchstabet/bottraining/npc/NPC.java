package dev.buchstabet.bottraining.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.buchstabet.bottraining.BotTraining;
import dev.buchstabet.bottraining.utils.Reflections;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NPC extends Reflections {

  private final EntityPlayer entityPlayer;
  private final String name;
  private Location location;
  private final String[] textures;

  public NPC(String name, Location location, String[] textures) {
    this.name = name;
    this.location = location;
    this.textures = textures;
    entityPlayer = setupEntityPlayer();
  }

  public void spawn() {
    Bukkit.getOnlinePlayers().forEach(this::spawn);
  }

  public void spawn(Player player) {
    DataWatcher watcher = entityPlayer.getDataWatcher();
    watcher.set(new DataWatcherObject<Byte>(17, DataWatcherRegistry.a), (byte) 127);
    PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entityPlayer.getId(), watcher, true);
    PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, entityPlayer);
    PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawn(entityPlayer);


    sendPacket(packetPlayOutPlayerInfo, player);
    sendPacket(packetPlayOutNamedEntitySpawn, player);
    sendPacket(packetPlayOutEntityMetadata, player);
    sendPacket(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) (this.location.getYaw() * 256.0F / 360.0F)), player);

    Bukkit.getScheduler().runTaskLater(BotTraining.getInstance(), () -> sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, entityPlayer)), 10);
  }

  public EntityPlayer getEntityPlayer() {
    return entityPlayer;
  }

  public String getName() {
    return name;
  }

  public Location getLocation() {
    return location;
  }

  public String[] getTextures() {
    return textures;
  }

  public void teleport(Location location, Player player) {
    this.location = location;
    entityPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    sendPacket(new PacketPlayOutEntityTeleport(entityPlayer), player);
  }

  public void rotateHead(Player player) {
    sendPacket(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) (this.location.getYaw() * 256.0F / 360.0F)), player);
  }

  public void despawn(Player player) {
    sendPacket(new PacketPlayOutEntityDestroy(entityPlayer.getId()), player);
  }

  private EntityPlayer setupEntityPlayer() {
    MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
    WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
    GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);
    gameProfile.getProperties().put("textures", new Property("textures", textures[0], textures[1]));

    EntityPlayer entityPlayer = new EntityPlayer(minecraftServer, worldServer, gameProfile);
    entityPlayer.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    return entityPlayer;
  }
}
