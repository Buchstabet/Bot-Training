package dev.buchstabet.bottraining.kithandler;

import dev.buchstabet.bottraining.BotTraining;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

public class KitHandler extends Command {

  private ItemStack[] soupKit, normalKit;
  private final File file;
  private final YamlConfiguration yaml;

  public KitHandler() throws IOException {
    super("setKit");
    File dir = new File("plugins//" + BotTraining.getInstance().getName());
    if (!dir.exists()) {
      dir.mkdirs();
    }

    file = new File(dir,"Kits.yml");
    if (!file.exists()) {
      file.createNewFile();
    }

    yaml = YamlConfiguration.loadConfiguration(file);

    if (yaml.isSet("Normal")) {
      normalKit = BukkitSerialization.itemStackArrayFromBase64(yaml.getString("Normal"));
    } else normalKit = new ItemStack[41];

    if (yaml.isSet("Soup")) {
      soupKit = BukkitSerialization.itemStackArrayFromBase64(yaml.getString("Soup"));
    } else soupKit = new ItemStack[41];
  }

  @Override
  public boolean execute(CommandSender sender, String s, String[] args) {
    if (sender instanceof Player) {
      Player player = (Player) sender;
      if (player.hasPermission("bottraining.commands.setkit")) {
        if (args.length == 1 && args[0].equalsIgnoreCase("Normal")) {
          normalKit = player.getInventory().getContents();
          String data = BukkitSerialization.itemStackArrayToBase64(player.getInventory().getContents());
          yaml.set("Normal", data);
          try {
            yaml.save(file);
          } catch (IOException exception) {
            exception.printStackTrace();
          }
          player.sendMessage(BotTraining.getInstance().getPrefix() + "§aDu hast ein Kit angepasst.");
        } else if (args.length == 1 && args[0].equalsIgnoreCase("Soup")) {
          soupKit = player.getInventory().getContents();

          String data = BukkitSerialization.itemStackArrayToBase64(player.getInventory().getContents());
          yaml.set("Soup", data);
          try {
            yaml.save(file);
          } catch (IOException exception) {
            exception.printStackTrace();
          }
          player.sendMessage(BotTraining.getInstance().getPrefix() + "§aDu hast ein Kit angepasst.");
        } else {
          player.sendMessage(BotTraining.getInstance().getPrefix() + "§cPlease use /setkit Normal/Soup");
        }
      } else {
        player.sendMessage("§cDu bist nicht berechtigt, Kits zu setzen.");
      }
    }
    return false;
  }

  public ItemStack[] getSoupKit() {
    return soupKit;
  }

  public ItemStack[] getNormalKit() {
    return normalKit;
  }
}
