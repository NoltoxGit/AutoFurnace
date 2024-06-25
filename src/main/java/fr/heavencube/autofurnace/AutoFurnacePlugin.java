package fr.heavencube.autofurnace;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoFurnacePlugin extends JavaPlugin {
    private Map<Material, Material> furnaceRecipes = new HashMap<>();
    private FileConfiguration messagesConfig;
    private MiniMessage miniMessage;
    private BukkitAudiences adventure;
    private Map<String, Long> cooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("AutoFurnacePlugin has been enabled!");
        saveDefaultConfig();
        loadFurnaceRecipes();
        loadMessagesConfig();
        miniMessage = MiniMessage.miniMessage();
        adventure = BukkitAudiences.create(this);

        PluginCommand autofurnaceCommand = getCommand("autofurnace");
        if (autofurnaceCommand != null) {
            autofurnaceCommand.setExecutor(new AutoFurnaceCommand(this));
            List<String> aliases = getConfig().getStringList("CommandAliases");
            autofurnaceCommand.setAliases(aliases);
        }

        PluginCommand autofurnaceAdminCommand = getCommand("autofurnaceadmin");
        if (autofurnaceAdminCommand != null) {
            autofurnaceAdminCommand.setExecutor(new AutoFurnaceAdminCommand(this));
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("AutoFurnacePlugin has been disabled.");
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    public Map<Material, Material> getFurnaceRecipes() {
        return furnaceRecipes;
    }

    public Component getMessage(String key) {
        String message = messagesConfig.getString("messages." + key, "Message key '" + key + "' not found.");
        return miniMessage.deserialize(message);
    }

    private void loadFurnaceRecipes() {
        furnaceRecipes.clear();
        FileConfiguration config = getConfig();
        for (String key : config.getConfigurationSection("Furnaces").getKeys(false)) {
            String hand = config.getString("Furnaces." + key + ".Hand");
            String result = config.getString("Furnaces." + key + ".Result");

            try {
                Material handMaterial = Material.valueOf(hand);
                Material resultMaterial = Material.valueOf(result);
                furnaceRecipes.put(handMaterial, resultMaterial);
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid material in config: " + hand + " or " + result);
            }
        }
    }

    private void loadMessagesConfig() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void reloadConfigData() {
        reloadConfig();
        loadFurnaceRecipes();
        reloadMessagesConfig();
        getLogger().info("Configuration reloaded!");
    }

    private void reloadMessagesConfig() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public BukkitAudiences adventure() {
        return adventure;
    }

    public Map<String, Long> getCooldowns() {
        return cooldowns;
    }

    public int getCooldownTime(Player player) {
        FileConfiguration config = getConfig();
        for (String key : config.getConfigurationSection("Cooldowns.Permissions").getKeys(false)) {
            if (player.hasPermission(key)) {
                return config.getInt("Cooldowns.Permissions." + key);
            }
        }
        return 0;
    }
}