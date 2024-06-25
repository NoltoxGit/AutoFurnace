package fr.heavencube.autofurnace;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public class AutoFurnaceCommand implements CommandExecutor {
    private final AutoFurnacePlugin plugin;

    public AutoFurnaceCommand(AutoFurnacePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.adventure().sender(sender).sendMessage(plugin.getMessage("prefix").append(Component.text("This command can only be used by players.")));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("autofurnace.use")) {
            plugin.adventure().player(player).sendMessage(plugin.getMessage("prefix").append(plugin.getMessage("noPermission")));
            return true;
        }

        // Vérifier le délai
        String playerName = player.getName();
        long currentTime = System.currentTimeMillis();
        long cooldownEnd = plugin.getCooldowns().getOrDefault(playerName, 0L);
        if (currentTime < cooldownEnd) {
            long remainingTime = (cooldownEnd - currentTime) / 1000;
            Component message = plugin.getMessage("prefix").append(plugin.getMessage("cooldownActive").replaceText(builder -> builder.matchLiteral("%time%").replacement(String.valueOf(remainingTime))));
            plugin.adventure().player(player).sendMessage(message);
            return true;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            plugin.adventure().player(player).sendMessage(plugin.getMessage("prefix").append(plugin.getMessage("mustHoldItem")));
            return true;
        }

        Material handMaterial = itemInHand.getType();
        Material resultMaterial = plugin.getFurnaceRecipes().get(handMaterial);

        if (resultMaterial == null) {
            plugin.adventure().player(player).sendMessage(plugin.getMessage("prefix").append(plugin.getMessage("itemCannotBeCooked")));
            return true;
        }

        itemInHand.setType(resultMaterial);
        Component message = plugin.getMessage("prefix").append(plugin.getMessage("itemCooked").replaceText(builder -> builder.matchLiteral("%result%").replacement(resultMaterial.toString())));
        plugin.adventure().player(player).sendMessage(message);

        // Définir le délai pour le joueur
        int cooldownTime = plugin.getCooldownTime(player);
        plugin.getCooldowns().put(playerName, currentTime + (cooldownTime * 1000L));

        return true;
    }
}
