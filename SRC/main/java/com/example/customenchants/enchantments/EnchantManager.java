package com.example.customenchants.enchantments;

import com.example.customenchants.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantManager {

    private final Main plugin;

    // Cache for levels: level -> {chance, duration}
    private final Map<Integer, Double> levelChances = new HashMap<>();
    private final Map<Integer, Double> levelDurations = new HashMap<>();

    // Cache for effects: effectName -> {level1, level2, ...}
    private final Map<String, List<Integer>> effectLevels = new HashMap<>();

    public EnchantManager(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = plugin.getConfigManager().getConfig();

        levelChances.clear();
        levelDurations.clear();
        effectLevels.clear();

        for (int i = 1; i <= 5; i++) {
            levelChances.put(i, config.getDouble("enchantments.levels." + i + ".chance"));
            levelDurations.put(i, config.getDouble("enchantments.levels." + i + ".duration"));
        }

        for (String key : config.getConfigurationSection("enchantments.effects").getKeys(false)) {
            effectLevels.put(key, config.getIntegerList("enchantments.effects." + key + ".levels"));
        }
    }

    public double getChance(int level) {
        return levelChances.getOrDefault(level, 0.0);
    }

    public double getDuration(int level) {
        return levelDurations.getOrDefault(level, 0.0);
    }

    public int getEffectAmplifier(CustomEnchant enchant, int level) {
        if (enchant == CustomEnchant.VAMPIRISM) return 0;
        List<Integer> levels = effectLevels.get(enchant.name());
        if (levels != null && levels.size() >= level) {
            // Configuration levels start from 1, PotionEffect amplifier starts from 0
            return Math.max(0, levels.get(level - 1) - 1);
        }
        return 0;
    }

    public int getMaxEnchants() {
        return plugin.getConfigManager().getConfig().getInt("enchantments.max-per-item", 8);
    }
}