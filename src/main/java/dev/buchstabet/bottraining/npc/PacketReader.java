package dev.buchstabet.bottraining.npc;

import dev.buchstabet.bottraining.BotTraining;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PacketReader {

    Channel channel;
    public static Map<UUID, Channel> channels = new HashMap<UUID, Channel>();

    public void inject(final Player p) {
        CraftPlayer cp = (CraftPlayer) p;
        channel = cp.getHandle().b.a.k;
        channels.put(p.getUniqueId(), channel);

        if (channel.pipeline().get("PacketInjector") != null) return;

        channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<PacketPlayInUseEntity>() {

            @Override
            protected void decode(ChannelHandlerContext channel, PacketPlayInUseEntity packet, List<Object> arg) throws Exception {
                arg.add(packet);
                readPacket(p, packet);
            }
        });

    }

    public void unInject(final Player p) {
        channel = channels.get(p.getUniqueId());
        if (channel.pipeline().get("PacketInjector") != null) {
            channel.pipeline().remove("PacketInjector");
        }
    }

    public void readPacket(final Player player, Packet<?> packet) {
        if (packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInUseEntity")) {
            if (getValue(packet, "b").getClass().getSimpleName().length() != 0) {
                return;
            }

            int id = (int) getValue(packet, "a");
            BotTraining.getInstance().getCombatHandler().getCombats().values().forEach(combat -> {
                if (combat.getNpc().getEntityPlayer().getId() == id) {
                    double damage = switch (player.getInventory().getItemInMainHand().getType()) {
                        case WOODEN_SWORD, STONE_SWORD, GOLDEN_SWORD, IRON_SWORD, DIAMOND_SWORD, NETHERITE_SWORD -> combat.getAttackDamage();
                        default -> 0.5;
                    } * player.getAttackCooldown();

                    Bukkit.getScheduler().runTask(BotTraining.getInstance(), () -> {
                        ((Damageable) combat.getEntity()).damage(damage, player);
                    });
                }
            });
        }
    }

    private Object getValue(final Object instance, final String name) {
        Object result = null;

        try {
            Field filed = instance.getClass().getDeclaredField(name);
            filed.setAccessible(true);
            result = filed.get(instance);
            filed.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
