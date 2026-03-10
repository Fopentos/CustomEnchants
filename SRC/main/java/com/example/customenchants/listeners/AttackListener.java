package com.example.customenchants.listeners;

import com.example.customenchants.Main;
import com.example.customenchants.enchantments.CustomEnchant;
import com.example.customenchants.items.ItemNBT;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Map;
import java.util.Random;

public class AttackListener implements Listener {

    private final Main plugin;
    private final Random random = new Random();

    public AttackListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) event.getDamager();
        LivingEntity target = (LivingEntity) event.getEntity();

        if (!isCriticalHit(player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;

        Map<CustomEnchant, Integer> enchants = ItemNBT.getCustomEnchants(item);

        for (Map.Entry<CustomEnchant, Integer> entry : enchants.entrySet()) {
            CustomEnchant enchant = entry.getKey();
            if (enchant == CustomEnchant.VAMPIRISM) continue; // Handled in VampirismListener

            int level = entry.getValue();
            double chance = plugin.getEnchantManager().getChance(level);
            
            if (random.nextDouble() * 100 <= chance) {
                int durationTicks = (int) (plugin.getEnchantManager().getDuration(level) * 20);
                int amplifier = plugin.getEnchantManager().getEffectAmplifier(enchant, level);
                
                target.addPotionEffect(new PotionEffect(enchant.getEffectType(), durationTicks, amplifier));
            }
        }
    }

    private boolean isCriticalHit(Player player) {
        return player.getFallDistance() > 0.0F &&
               !player.isOnGround() &&
               !player.isInWater() && // In 1.16.5, being in water prevents crits
               !player.hasPotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS) &&
               player.getVehicle() == null && // Not riding
               !player.isClimbing(); // Not on ladder/vine
    }
}