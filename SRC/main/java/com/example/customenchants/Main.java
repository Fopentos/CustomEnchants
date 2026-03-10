package com.example.customenchants;

import com.example.customenchants.commands.GiveBookCommand;
import com.example.customenchants.config.ConfigManager;
import com.example.customenchants.enchantments.EnchantManager;
import com.example.customenchants.listeners.AttackListener;
import com.example.customenchants.listeners.NPCListener;
import com.example.customenchants.listeners.PotionListener;
import com.example.customenchants.listeners.VampirismListener;
import com.example.customenchants.npc.EnchanterGUI;
import com.example.customenchants.utils.CooldownManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private Economy econ = null;
    private ConfigManager configManager;
    private EnchantManager enchantManager;
    private CooldownManager cooldownManager;
    private EnchanterGUI enchanterGUI;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy() && getServer().getPluginManager().getPlugin("Vault") != null) {
            getLogger().warning("Vault найден, но экономика не настроена (нет плагина экономики, например Essentials). Используется бесплатный режим.");
        }

        // Init managers
        configManager = new ConfigManager(this);
        configManager.load();

        enchantManager = new EnchantManager(this);
        cooldownManager = new CooldownManager();
        enchanterGUI = new EnchanterGUI(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new AttackListener(this), this);
        getServer().getPluginManager().registerEvents(new VampirismListener(this), this);
        getServer().getPluginManager().registerEvents(new PotionListener(this), this);
        getServer().getPluginManager().registerEvents(new NPCListener(this), this);

        // Register commands
        getCommand("ce").setExecutor(new GiveBookCommand(this));
        getCommand("enchanter").setExecutor(new com.example.customenchants.commands.EnchanterCommand(this));

        // Add potion recipe
        PotionListener.registerCleansingRecipe(this);

        getLogger().info("CustomEnchants enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomEnchants disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Main getInstance() {
        return instance;
    }

    public Economy getEconomy() {
        return econ;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EnchantManager getEnchantManager() {
        return enchantManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public EnchanterGUI getEnchanterGUI() {
        return enchanterGUI;
    }

    public NamespacedKey getKey(String key) {
        return new NamespacedKey(this, key);
    }
}