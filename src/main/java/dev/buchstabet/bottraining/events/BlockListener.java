package dev.buchstabet.bottraining.events;

import dev.buchstabet.bottraining.commands.BuildCommand;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockListener implements Listener {

  @EventHandler
  public void onBlockBreak(BlockBreakEvent e) {
    if (BuildCommand.BUILDERS.contains(e.getPlayer())) {
      return;
    }

    e.setCancelled(true);
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent e) {
    if (BuildCommand.BUILDERS.contains(e.getPlayer())) {
      return;
    }

    e.setCancelled(true);
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent e) {
    if (BuildCommand.BUILDERS.contains(e.getPlayer())) {
      return;
    }

    if (e.getAction().equals(Action.PHYSICAL)) {
      e.setCancelled(true);
    }

    if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
      if (e.getItem() != null) {
        if (e.getItem().getType().equals(Material.AMETHYST_SHARD) && e.getItem().hasItemMeta() &&
                e.getItem().getItemMeta().hasDisplayName() && e.getItem().getItemMeta().getDisplayName().equals("§aTraining")) {
          e.getPlayer().chat("/training");
        }
      }
    }
  }

}
