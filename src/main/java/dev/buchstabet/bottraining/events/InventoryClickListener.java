package dev.buchstabet.bottraining.events;

import dev.buchstabet.bottraining.BotTraining;
import dev.buchstabet.bottraining.commands.BuildCommand;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

  @EventHandler
  public void onClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player)) {
      return;
    }


    Player player = (Player) e.getWhoClicked();
    if (BuildCommand.BUILDERS.contains(player)) {
      return;
    }
    
    if (!BotTraining.getInstance().getCombatHandler().getCombats().containsKey(player.getUniqueId())) {

      e.setCancelled(true);
    }

    if (e.getView().getTitle().equals("§8► §aStarte ein Training §8◄")) {
      e.setCancelled(true);
      if (e.getRawSlot() == 11 || e.getRawSlot() == 15) {
        try {
          BotTraining.getInstance().getCombatHandler().createCombat(player, e.getRawSlot() == 11);
        } catch (IllegalStateException exception) {
          player.sendMessage(BotTraining.getInstance().getPrefix() + "§cDu bist bereits in einem Kampf!");
          player.closeInventory();
          return;
        }
        player.setNoDamageTicks(10);
        player.closeInventory();
      } else {
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1, 1);
      }
    }
  }

}
