package com.example.customenchants.items;

import com.example.customenchants.Main;
import com.example.customenchants.enchantments.CustomEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class ItemNBT {

    public static void setEnchant(ItemStack item, CustomEnchant enchant, int level) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        
        NamespacedKey key = Main.getInstance().getKey("enchant_" + enchant.name().toLowerCase());
        pdc.set(key, PersistentDataType.INTEGER, level);
        item.setItemMeta(meta);
        
        // Only update lore if it's not a book, as book lore is static
        if (!isCustomBook(item)) {
            ItemBuilder.updateLore(item);
        }
    }

    public static int getEnchantLevel(ItemStack item, CustomEnchant enchant) {
        if (item == null || !item.hasItemMeta()) return 0;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = Main.getInstance().getKey("enchant_" + enchant.name().toLowerCase());
        
        return pdc.getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    public static Map<CustomEnchant, Integer> getCustomEnchants(ItemStack item) {
        Map<CustomEnchant, Integer> enchants = new HashMap<>();
        if (item == null || !item.hasItemMeta()) return enchants;
        
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        for (CustomEnchant enchant : CustomEnchant.values()) {
            NamespacedKey key = Main.getInstance().getKey("enchant_" + enchant.name().toLowerCase());
            if (pdc.has(key, PersistentDataType.INTEGER)) {
                enchants.put(enchant, pdc.get(key, PersistentDataType.INTEGER));
            }
        }
        return enchants;
    }

    public static void setCustomBook(ItemStack item, boolean isBook) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = Main.getInstance().getKey("is_custom_book");
        if (isBook) {
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        } else {
            meta.getPersistentDataContainer().remove(key);
        }
        item.setItemMeta(meta);
    }

    public static boolean isCustomBook(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey key = Main.getInstance().getKey("is_custom_book");
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }

    public static CustomEnchant getBookEnchant(ItemStack item) {
        if (!isCustomBook(item)) return null;
        Map<CustomEnchant, Integer> enchants = getCustomEnchants(item);
        if (!enchants.isEmpty()) {
            return enchants.keySet().iterator().next();
        }
        return null;
    }
}