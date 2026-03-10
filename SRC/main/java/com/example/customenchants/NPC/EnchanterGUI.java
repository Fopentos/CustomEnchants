package com.example.customenchants.npc;

import com.example.customenchants.Main;
import com.example.customenchants.enchantments.CustomEnchant;
import com.example.customenchants.items.ItemNBT;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnchanterGUI implements Listener {

    private final Main plugin;
    private final String title;
    
    private final int ITEM_SLOT = 11;
    private final int BOOK_SLOT = 15;
    private final int CONFIRM_SLOT = 22;

    public EnchanterGUI(Main plugin) {
        this.plugin = plugin;
        this.title = ChatColor.translateAlternateColorCodes('&', plugin.getConfigManager().getConfig().getString("npc.gui-title", "&8Мастер древних чар"));
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, title);
        
        // Fill background
        ItemStack bg = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bgMeta = bg.getItemMeta();
        bgMeta.setDisplayName(" ");
        bg.setItemMeta(bgMeta);
        
        for (int i = 0; i < inv.getSize(); i++) {
            if (i != ITEM_SLOT && i != BOOK_SLOT && i != CONFIRM_SLOT) {
                inv.setItem(i, bg);
            }
        }
        
        updateConfirmButton(inv, null, null);
        
        player.openInventory(inv);
    }

    private void updateConfirmButton(Inventory inv, ItemStack item, ItemStack book) {
        ItemStack confirm = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = confirm.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Положите предмет и книгу");
        
        if (item != null && item.getType() != Material.AIR && book != null && book.getType() != Material.AIR) {
            CustomEnchant enchant = ItemNBT.getBookEnchant(book);
            int bookLevel = ItemNBT.getCustomEnchants(book).getOrDefault(enchant, 0);
            
            if (enchant != null && isApplicable(item.getType())) {
                Map<CustomEnchant, Integer> currentEnchants = ItemNBT.getCustomEnchants(item);
                
                if (currentEnchants.containsKey(enchant)) {
                    meta.setDisplayName(ChatColor.RED + "Это зачарование уже есть на предмете!");
                } else if (currentEnchants.size() >= plugin.getEnchantManager().getMaxEnchants()) {
                    meta.setDisplayName(ChatColor.RED + "Достигнут лимит зачарований!");
                } else {
                    double price = calculatePrice(bookLevel, currentEnchants.size());
                    confirm.setType(Material.LIME_STAINED_GLASS_PANE);
                    meta.setDisplayName(ChatColor.GREEN + "Подтвердить зачарование");
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Зачарование: " + ChatColor.GOLD + enchant.getDisplayName() + " " + bookLevel);
                    lore.add(ChatColor.GRAY + "Цена: " + ChatColor.YELLOW + price + " монет");
                    meta.setLore(lore);
                }
            } else {
                meta.setDisplayName(ChatColor.RED + "Неподходящий предмет или книга!");
            }
        }
        
        confirm.setItemMeta(meta);
        inv.setItem(CONFIRM_SLOT, confirm);
    }

    private double calculatePrice(int bookLevel, int currentEnchantCount) {
        double basePrice = plugin.getConfigManager().getConfig().getDouble("npc.base-prices.level" + bookLevel, 1000);
        double multiplier = plugin.getConfigManager().getConfig().getDouble("npc.price-multiplier", 2.5);
        return basePrice * Math.pow(multiplier, currentEnchantCount);
    }

    private boolean isApplicable(Material material) {
        String name = material.name();
        return name.endsWith("_SWORD") || name.endsWith("_AXE") || name.equals("BOW") || name.equals("CROSSBOW") || name.equals("TRIDENT");
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(title)) return;
        
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        int slot = event.getRawSlot();
        
        // Allow clicking in player inventory
        if (slot >= inv.getSize()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                updateConfirmButton(inv, inv.getItem(ITEM_SLOT), inv.getItem(BOOK_SLOT));
            }, 1L);
            return;
        }
        
        event.setCancelled(true);
        
        if (slot == ITEM_SLOT || slot == BOOK_SLOT) {
            ItemStack clicked = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            
            event.setCurrentItem(cursor);
            event.getView().setCursor(clicked);
            
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                updateConfirmButton(inv, inv.getItem(ITEM_SLOT), inv.getItem(BOOK_SLOT));
            }, 1L);
            return;
        }
        
        if (slot == CONFIRM_SLOT) {
            ItemStack confirmItem = inv.getItem(CONFIRM_SLOT);
            if (confirmItem != null && confirmItem.getType() == Material.LIME_STAINED_GLASS_PANE) {
                ItemStack item = inv.getItem(ITEM_SLOT);
                ItemStack book = inv.getItem(BOOK_SLOT);
                
                CustomEnchant enchant = ItemNBT.getBookEnchant(book);
                int bookLevel = ItemNBT.getCustomEnchants(book).getOrDefault(enchant, 0);
                Map<CustomEnchant, Integer> currentEnchants = ItemNBT.getCustomEnchants(item);
                
                double price = calculatePrice(bookLevel, currentEnchants.size());
                
                if (plugin.getEconomy() != null) {
                    if (!plugin.getEconomy().has(player, price)) {
                        String msg = plugin.getConfigManager().getMessage("no-money").replace("%price%", String.valueOf(price));
                        player.sendMessage(msg);
                        return;
                    }
                    plugin.getEconomy().withdrawPlayer(player, price);
                }
                
                // Apply enchant
                ItemNBT.setEnchant(item, enchant, bookLevel);
                
                // Return item, clear book
                inv.setItem(ITEM_SLOT, null);
                inv.setItem(BOOK_SLOT, null);
                player.getInventory().addItem(item).values().forEach(leftover -> {
                    player.getWorld().dropItem(player.getLocation(), leftover);
                });
                
                updateConfirmButton(inv, null, null);
                player.sendMessage(plugin.getConfigManager().getMessage("success"));
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(title)) return;
        
        Inventory inv = event.getInventory();
        Player player = (Player) event.getPlayer();
        
        ItemStack item = inv.getItem(ITEM_SLOT);
        ItemStack book = inv.getItem(BOOK_SLOT);
        
        if (item != null && item.getType() != Material.AIR) {
            player.getInventory().addItem(item).values().forEach(leftover -> {
                player.getWorld().dropItem(player.getLocation(), leftover);
            });
        }
        if (book != null && book.getType() != Material.AIR) {
            player.getInventory().addItem(book).values().forEach(leftover -> {
                player.getWorld().dropItem(player.getLocation(), leftover);
            });
        }
    }
}