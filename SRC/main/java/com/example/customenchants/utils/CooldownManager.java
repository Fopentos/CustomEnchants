package com.example.customenchants.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    // Player UUID -> Next allowed time in ms
    private final Map<UUID, Long> vampirismCooldowns = new HashMap<>();
    private final Map<UUID, Long> immunityDurations = new HashMap<>();

    public void setVampirismCooldown(UUID uuid, long seconds) {
        vampirismCooldowns.put(uuid, System.currentTimeMillis() + (seconds * 1000));
    }

    public boolean canUseVampirism(UUID uuid) {
        return System.currentTimeMillis() >= vampirismCooldowns.getOrDefault(uuid, 0L);
    }
    
    public long getVampirismRemaining(UUID uuid) {
        long remaining = vampirismCooldowns.getOrDefault(uuid, 0L) - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }

    public void setImmunity(UUID uuid, long seconds) {
        immunityDurations.put(uuid, System.currentTimeMillis() + (seconds * 1000));
    }

    public boolean hasImmunity(UUID uuid) {
        return System.currentTimeMillis() < immunityDurations.getOrDefault(uuid, 0L);
    }
}