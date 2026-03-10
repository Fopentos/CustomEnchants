package com.example.customenchants.listeners;

import com.example.customenchants.Main;
import com.example.customenchants.enchantments.CustomEnchant;
import com.example.customenchants.items.ItemNBT;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class VampirismListener implements Listener {

    private final Main plugin;
    private final Random random = new Random();

    public VampirismListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVampirismAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) event.getDamager();
        LivingEntity target = (LivingEntity) event.getEntity();

        ItemStack item = player.getInventory().getItemInMainHand();
        int vampLevel = ItemNBT.getEnchantLevel(item, CustomEnchant.VAMPIRISM);
        if (vampLevel <= 0) return;

        FileConfiguration config = plugin.getConfigManager().getConfig();
        double chance = config.getDouble("vampirism.level" + vampLevel + ".chance", 0.0);
        double healthToSteal = config.getDouble("vampirism.level" + vampLevel + ".health", 0.0);

        if (random.nextDouble() * 100 > chance) return;

        // Check cooldown
        if (!plugin.getCooldownManager().canUseVampirism(player.getUniqueId())) {
            // Optional: send message about cooldown
            /*
            long remaining = plugin.getCooldownManager().getVampirismRemaining(player.getUniqueId()) / 1000;
            String msg = plugin.getConfigManager().getMessage("cooldown").replace("%time%", String.valueOf(remaining));
            player.sendMessage(msg);
            */
            return;
        }

        double cooldownSeconds = config.getDouble("vampirism.cooldown", 1.0);
        plugin.getCooldownManager().setVampirismCooldown(player.getUniqueId(), (long) cooldownSeconds);

        applyVampirism(player, target, healthToSteal);
    }

    private void applyVampirism(Player attacker, LivingEntity target, double amount) {
        double absorption = target.getAbsorptionAmount();
        double stolenAbsorption = Math.min(amount, absorption);
        
        // Remove from absorption first
        target.setAbsorptionAmount(absorption - stolenAbsorption);
        double remainingAmount = amount - stolenAbsorption;

        // Remove from normal health
        if (remainingAmount > 0) {
            double currentHealth = target.getHealth();
            double healthToTake = Math.min(remainingAmount, currentHealth); // Can't take more than they have
            target.setHealth(Math.max(0, currentHealth - healthToTake));
        }

        // Heal attacker
        double attackerHealth = attacker.getHealth();
        // Spigot 1.16 max health attribute
        double maxHealth = attacker.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        double newHealth = Math.min(maxHealth, attackerHealth + amount); // Heal by full amount stolen (or attempted to steal)
        attacker.setHealth(newHealth);
    }
}