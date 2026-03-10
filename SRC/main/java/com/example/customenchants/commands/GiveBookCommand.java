package com.example.customenchants.commands;

import com.example.customenchants.Main;
import com.example.customenchants.enchantments.CustomEnchant;
import com.example.customenchants.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveBookCommand implements CommandExecutor {

    private final Main plugin;

    public GiveBookCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customenchants.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getConfigManager().getMessage("usage"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reload();
            plugin.getEnchantManager().loadConfig();
            sender.sendMessage(plugin.getConfigManager().getMessage("reloaded"));
            return true;
        }

        if (args[0].equalsIgnoreCase("givebook")) {
            if (args.length < 4) {
                sender.sendMessage(plugin.getConfigManager().getMessage("usage"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
                return true;
            }

            CustomEnchant enchant;
            try {
                enchant = CustomEnchant.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(plugin.getConfigManager().getMessage("invalid-enchant"));
                return true;
            }

            int level;
            try {
                level = Integer.parseInt(args[3]);
                int maxLevel = (enchant == CustomEnchant.VAMPIRISM) ? 2 : 5;
                if (level < 1 || level > maxLevel) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("invalid-level"));
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getConfigManager().getMessage("invalid-level"));
                return true;
            }

            ItemStack book = ItemBuilder.createEnchantBook(enchant, level);
            target.getInventory().addItem(book).values().forEach(leftover -> {
                target.getWorld().dropItem(target.getLocation(), leftover);
            });

            String msg = plugin.getConfigManager().getMessage("book-given").replace("%player%", target.getName());
            sender.sendMessage(msg);
            return true;
        }

        sender.sendMessage(plugin.getConfigManager().getMessage("usage"));
        return true;
    }
}