package com.example.customenchants.listeners;

import com.example.customenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Arrays;
import java.util.List;

public class PotionListener implements Listener {

    private final Main plugin;
    
    private static final List<PotionEffectType> NEGATIVE_EFFECTS = Arrays.asList(
            PotionEffectType.POISON, PotionEffectType.WITHER, PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING, PotionEffectType.WEAKNESS, PotionEffectType.HUNGER,
            PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION, PotionEffectType.LEVITATION,
            PotionEffectType.GLOWING
    );

    public PotionListener(Main plugin) {
        this.plugin = plugin;
    }

    public static void registerCleansingRecipe(Main plugin) {
        if (!plugin.getConfigManager().getConfig().getBoolean("potions.cleansing.craft.enabled", true)) {
            return;
        }

        ItemStack result = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) result.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Взрывное зелье очищения");
        meta.setColor(Color.WHITE);
        
        NamespacedKey key = new NamespacedKey(plugin, "potion_cleansing");
        meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
        result.setItemMeta(meta);

        ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(plugin, "cleansing_potion"), result);
        // Requirement: Milk bucket + mundane splash potion
        recipe.addIngredient(Material.MILK_BUCKET);
        recipe.addIngredient(Material.SPLASH_POTION);
        
        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        ItemStack item = event.getPotion().getItem();
        if (item == null || !item.hasItemMeta()) return;

        NamespacedKey key = new NamespacedKey(plugin, "potion_cleansing");
        if (item.getItemMeta().getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.BYTE)) {
            event.setCancelled(true); // Cancel vanilla splash effects if any
            
            double radius = plugin.getConfigManager().getConfig().getDouble("potions.cleansing.radius", 4.0);
            
            // Effect all entities in radius
            for (Entity entity : event.getPotion().getNearbyEntities(radius, radius, radius)) {
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    for (PotionEffect effect : livingEntity.getActivePotionEffects()) {
                        if (isNegative(effect.getType())) {
                            livingEntity.removePotionEffect(effect.getType());
                        }
                    }
                }
            }
            // Add particles visually here if needed, but spigot handles splash potion particles automatically usually
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.POTION || !item.hasItemMeta()) return;

        NamespacedKey key = new NamespacedKey(plugin, "potion_immunity");
        if (item.getItemMeta().getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.BYTE)) {
            Player player = event.getPlayer();
            long duration = plugin.getConfigManager().getConfig().getLong("potions.immunity.duration", 60);
            plugin.getCooldownManager().setImmunity(player.getUniqueId(), duration);
            player.sendMessage(ChatColor.GOLD + "Вы получили иммунитет к негативным эффектам на " + duration + " секунд!");
            
            // Remove existing negative effects
            for (PotionEffect effect : player.getActivePotionEffects()) {
                if (isNegative(effect.getType())) {
                    player.removePotionEffect(effect.getType());
                }
            }
        }
    }

    @EventHandler
    public void onPotionEffectAdd(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (event.getAction() == EntityPotionEffectEvent.Action.ADDED || event.getAction() == EntityPotionEffectEvent.Action.CHANGED) {
            PotionEffect effect = event.getNewEffect();
            if (effect != null && isNegative(effect.getType())) {
                if (plugin.getCooldownManager().hasImmunity(player.getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isNegative(PotionEffectType type) {
        return NEGATIVE_EFFECTS.contains(type);
    }
}