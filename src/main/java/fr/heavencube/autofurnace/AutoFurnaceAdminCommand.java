package fr.heavencube.autofurnace;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AutoFurnaceAdminCommand implements CommandExecutor {
    private final AutoFurnacePlugin plugin;

    public AutoFurnaceAdminCommand(AutoFurnacePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("autofurnaceadmin.reload")) {
            plugin.adventure().sender(sender).sendMessage(plugin.getMessage("prefix").append(plugin.getMessage("noPermission")));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfigData();
            plugin.adventure().sender(sender).sendMessage(plugin.getMessage("prefix").append(plugin.getMessage("configReloaded")));
        } else {
            plugin.adventure().sender(sender).sendMessage(plugin.getMessage("prefix").append(Component.text("Usage: /autofurnaceadmin reload")));
        }
        return true;
    }
}