package dev.buchstabet.bottraining.soup;

import dev.buchstabet.bottraining.BotTraining;
import dev.buchstabet.bottraining.combat.Combat;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SoupHandler implements Listener {


  @EventHandler
  public void onInteract(PlayerInteractEvent e) {
    if (BotTraining.getInstance().getCombatHandler().getCombats().containsKey(e.getPlayer().getUniqueId())) {
      Combat combat = BotTraining.getInstance().getCombatHandler().getCombats().get(e.getPlayer().getUniqueId());
      if (!combat.isSouping()) {
        return;
      }

      if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (item.getType().equals(Material.MUSHROOM_STEW)) {
          ItemStack stack = new ItemStack(Material.BOWL);
          e.getPlayer().getInventory().setItemInMainHand(stack);
          double health = e.getPlayer().getHealth() + 4;
          if (health > e.getPlayer().getMaxHealth()) {
            health = e.getPlayer().getMaxHealth();
          }
          e.getPlayer().setHealth(health);
        }

      }
    }
  }


}
