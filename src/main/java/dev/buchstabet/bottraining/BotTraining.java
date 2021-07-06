package dev.buchstabet.bottraining;

import dev.buchstabet.bottraining.combat.Combat;
import dev.buchstabet.bottraining.combat.CombatHandler;
import dev.buchstabet.bottraining.commands.BuildCommand;
import dev.buchstabet.bottraining.events.BlockListener;
import dev.buchstabet.bottraining.events.InventoryClickListener;
import dev.buchstabet.bottraining.events.PlayerJoinListener;
import dev.buchstabet.bottraining.events.PlayerQuitListener;
import dev.buchstabet.bottraining.kithandler.KitHandler;
import dev.buchstabet.bottraining.npc.PacketReader;
import dev.buchstabet.bottraining.soup.SoupHandler;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class BotTraining extends JavaPlugin {

  public static BotTraining getInstance() {
    return botTraining;
  }

  private static BotTraining botTraining;
  private final CombatHandler combatHandler = new CombatHandler();
  private String prefix;
  private String botName;
  private PacketReader packetReader;
  private KitHandler kitHandler;

  @Override
  public void onLoad() {
    botTraining = this;
  }

  public PacketReader getPacketReader() {
    return packetReader;
  }

  public String getBotName() {
    return botName;
  }

  public KitHandler getKitHandler() {
    return kitHandler;
  }

  @Override
  public void onEnable() {
    // Plugin startup logic
    saveDefaultConfig();
    prefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Prefix"));
    botName = ChatColor.translateAlternateColorCodes('&', getConfig().getString("BotName"));

    PluginManager pm = this.getServer().getPluginManager();
    pm.registerEvents(new PlayerJoinListener(), this);
    pm.registerEvents(new PlayerQuitListener(), this);
    pm.registerEvents(new InventoryClickListener(), this);
    pm.registerEvents(new SoupHandler(), this);
    pm.registerEvents(combatHandler, this);
    pm.registerEvents(new BlockListener(), this);

    try {
      kitHandler = new KitHandler();
      ((CraftServer) getServer()).getCommandMap().register("Bot-Training", kitHandler);
    } catch (IOException exception) {
      exception.printStackTrace();
    }
    this.getCommand("training").setExecutor(this);
    ((CraftServer) getServer()).getCommandMap().register("Bot-Training", new BuildCommand());

    for (World world : Bukkit.getWorlds()) {
      world.setFullTime(14512L);
      world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
      world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
      if (world.getDifficulty().equals(Difficulty.PEACEFUL)) {
        world.setDifficulty(Difficulty.HARD);
      }
    }

    packetReader = new PacketReader();
    Bukkit.getOnlinePlayers().forEach(player -> packetReader.inject(player));
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    for (Combat value : getCombatHandler().getCombats().values()) {
      value.stop();
    }
    Bukkit.getOnlinePlayers().forEach(player -> packetReader.unInject(player));
  }

  public CombatHandler getCombatHandler() {
    return combatHandler;
  }


  public String getPrefix() {
    return prefix;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (sender instanceof Player) {
      Player player = (Player) sender;
      if (combatHandler.getCombats().containsKey(player.getUniqueId())) {
        player.sendMessage(BotTraining.getInstance().getPrefix() + "§cDu bist bereits in einem Kampf!");
        return true;
      }

      Inventory inventory = Bukkit.createInventory(null, 9 * 3,
              "§8► §aStarte ein Training §8◄");
      ItemStack placeholder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
      ItemMeta placeholderItemMeta = placeholder.getItemMeta();
      placeholderItemMeta.setDisplayName("§3");
      placeholder.setItemMeta(placeholderItemMeta);

      for (int i = 0; i < 9; i++) {
        inventory.setItem(i, placeholder);
        inventory.setItem(i + 18, placeholder);
      }

      {
        ItemStack soupItem = new ItemStack(Material.MUSHROOM_STEW);
        ItemMeta soupItemMeta = soupItem.getItemMeta();
        soupItemMeta.setDisplayName("Soup-Training");
        soupItem.setItemMeta(soupItemMeta);

        inventory.setItem(11, soupItem);
      }

      {
        ItemStack soupItem = new ItemStack(Material.IRON_SWORD);
        ItemMeta soupItemMeta = soupItem.getItemMeta();
        soupItemMeta.setDisplayName("Training");
        soupItem.setItemMeta(soupItemMeta);

        inventory.setItem(15, soupItem);
      }
      player.openInventory(inventory);
    }
    return false;
  }

  public ItemStack[] getDefaultInventory() {
    ItemStack[] stacks = new ItemStack[41];
    ItemStack item = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName("§aTraining");
    item.setItemMeta(meta);
    stacks[4] = item;
    return stacks;
  }
}
