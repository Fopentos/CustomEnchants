package com.example.customenchants.enchantments;

import org.bukkit.potion.PotionEffectType;

public enum CustomEnchant {
    WITHER(PotionEffectType.WITHER, "Иссушение"),
    POISON(PotionEffectType.POISON, "Отравление"),
    BLINDNESS(PotionEffectType.BLINDNESS, "Слепота"),
    NAUSEA(PotionEffectType.CONFUSION, "Тошнота"),
    SLOWNESS(PotionEffectType.SLOW, "Замедление"),
    MINING_FATIGUE(PotionEffectType.SLOW_DIGGING, "Усталость"),
    WEAKNESS(PotionEffectType.WEAKNESS, "Слабость"),
    HUNGER(PotionEffectType.HUNGER, "Голод"),
    VAMPIRISM(null, "Вампиризм"); // Special case

    private final PotionEffectType effectType;
    private final String displayName;

    CustomEnchant(PotionEffectType effectType, String displayName) {
        this.effectType = effectType;
        this.displayName = displayName;
    }

    public PotionEffectType getEffectType() {
        return effectType;
    }

    public String getDisplayName() {
        return displayName;
    }
}