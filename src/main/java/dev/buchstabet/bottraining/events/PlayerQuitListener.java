package dev.buchstabet.bottraining.events;

import dev.buchstabet.bottraining.BotTraining;
import dev.buchstabet.bottraining.combat.Combat;
import dev.buchstabet.bottraining.commands.BuildCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    e.setQuitMessage(null);
    BuildCommand.BUILDERS.remove(e.getPlayer());

    BotTraining.getInstance().getPacketReader().unInject(e.getPlayer());
    if (!BotTraining.getInstance().getCombatHandler().isInCombat(e.getPlayer())) {
      return;
    }

    Combat combat = BotTraining.getInstance().getCombatHandler().getCombats().remove(e.getPlayer().getUniqueId());
    combat.stop();
  }

}
