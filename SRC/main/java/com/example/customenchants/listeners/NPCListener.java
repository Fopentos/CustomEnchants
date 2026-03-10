package com.example.customenchants.listeners;

import com.example.customenchants.Main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class NPCListener implements Listener {

    private final Main plugin;

    public NPCListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        if (entity instanceof Villager) {
            String npcName = plugin.getConfigManager().getMessage("npc.villager-name"); // Might need adjustment based on config structure
            if (entity.getCustomName() != null && entity.getCustomName().equals(npcName)) {
                event.setCancelled(true);
                if (player.hasPermission("customenchants.use")) {
                    plugin.getEnchanterGUI().openGUI(player);
                } else {
                    player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                }
            }
        }
    }
}