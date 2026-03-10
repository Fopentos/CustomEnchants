package com.example.customenchants.items;

import com.example.customenchants.Main;
import com.example.customenchants.enchantments.CustomEnchant;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemBuilder {

    public static ItemStack createEnchantBook(CustomEnchant enchant, int level) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + enchant.getDisplayName() + " " + toRoman(level));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Книга древних чар");
            lore.add(ChatColor.GRAY + "Используйте у Мастера чар");
            meta.setLore(lore);
            item.setItemMeta(meta);

            // Add PDC
            ItemNBT.setEnchant(item, enchant, level);
            // Add a special tag to identify it as a custom book
            ItemNBT.setCustomBook(item, true);
        }
        return item;
    }

    public static void updateLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        // Remove old custom enchants lore
        lore.removeIf(line -> line.startsWith(ChatColor.GRAY + "» "));

        // Add new custom enchants
        Map<CustomEnchant, Integer> enchants = ItemNBT.getCustomEnchants(item);
        for (Map.Entry<CustomEnchant, Integer> entry : enchants.entrySet()) {
            lore.add(ChatColor.GRAY + "» " + ChatColor.GOLD + entry.getKey().getDisplayName() + " " + toRoman(entry.getValue()));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private static String toRoman(int number) {
        switch (number) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            default: return String.valueOf(number);
        }
    }
}