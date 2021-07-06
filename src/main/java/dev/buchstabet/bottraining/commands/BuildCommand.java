package dev.buchstabet.bottraining.commands;

import dev.buchstabet.bottraining.BotTraining;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BuildCommand extends Command {

  public static final List<Player> BUILDERS = new ArrayList<>();

  public BuildCommand() {
    super("build");
  }

  @Override
  public boolean execute(CommandSender sender, String s, String[] args) {
    if (sender instanceof Player) {
      Player player = (Player) sender;
      if (!player.hasPermission("bottraining.commands.build")) {
        player.sendMessage("§cDu bist nicht berechtigt, in den Baumodus zu wechseln.");
        return true;
      }

      if (BUILDERS.contains(player)) {
        BUILDERS.remove(player);
      } else {
        BUILDERS.add(player);
      }

      player.sendMessage(BotTraining.getInstance().getPrefix() + "§aDu hast den Baumodus gewechselt.");
    }
    return false;
  }
}
