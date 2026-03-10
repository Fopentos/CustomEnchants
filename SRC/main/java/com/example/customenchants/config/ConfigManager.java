package com.example.customenchants.config;

import com.example.customenchants.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final Main plugin;
    private FileConfiguration config;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getMessage(String path) {
        String msg = config.getString("messages." + path);
        if (msg == null) return "Missing message: " + path;
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}