package ch.ksrminecraft.sMPRankPoints.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages all configuration files and related settings.
 * Loads and processes: config.yml, advancements.yml, hardness.yml
 */
public class ConfigManager {

    private final FileConfiguration config;
    private FileConfiguration advancementsConfig;
    private FileConfiguration hardnessConfig;

    private final JavaPlugin plugin;

    private boolean debugEnabled;

    private int endBossPoints;
    private int defaultAdvancementPoints;
    private int blockBreakEvery, blockBreakPoints;
    private int blockPlaceEvery, blockPlacePoints;

    // Hardness class mappings and multipliers from hardness.yml
    private final Map<String, Set<String>> hardnessClasses = new HashMap<>();
    private final Map<String, Double> multipliers = new HashMap<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadConfig(plugin);
    }

    /**
     * Loads or reloads all config files and initializes values.
     */
    public void loadConfig(JavaPlugin plugin) {
        plugin.saveDefaultConfig();

        // Load advancements.yml
        File advFile = new File(plugin.getDataFolder(), "advancements.yml");
        if (!advFile.exists()) {
            plugin.saveResource("advancements.yml", false);
        }
        advancementsConfig = YamlConfiguration.loadConfiguration(advFile);

        // Load hardness.yml
        File hardnessFile = new File(plugin.getDataFolder(), "hardness.yml");
        if (!hardnessFile.exists()) {
            plugin.saveResource("hardness.yml", false);
        }
        hardnessConfig = YamlConfiguration.loadConfiguration(hardnessFile);
        loadHardnessConfig();

        // Load general values
        debugEnabled = config.getBoolean("debug", false);
        endBossPoints = config.getInt("points.endboss-kill.ender_dragon", 0);
        defaultAdvancementPoints = config.getInt("defaults.advancement-points", 0);

        blockBreakEvery = config.getInt("block-activity.break.every", 0);
        blockBreakPoints = config.getInt("block-activity.break.points", 0);
        blockPlaceEvery = config.getInt("block-activity.place.every", 0);
        blockPlacePoints = config.getInt("block-activity.place.points", 0);

        generateAdvancementConfig(plugin);
    }

    /**
     * Parses hardness.yml and builds multiplier mappings.
     */
    private void loadHardnessConfig() {
        hardnessClasses.clear();
        multipliers.clear();

        if (hardnessConfig.contains("multipliers")) {
            for (String key : hardnessConfig.getConfigurationSection("multipliers").getKeys(false)) {
                double val = hardnessConfig.getDouble("multipliers." + key);
                multipliers.put(key.toUpperCase(), val);
            }
        }

        if (hardnessConfig.contains("blocks")) {
            for (String key : hardnessConfig.getConfigurationSection("blocks").getKeys(false)) {
                List<String> blocks = hardnessConfig.getStringList("blocks." + key);
                hardnessClasses.put(key.toUpperCase(), new HashSet<>(blocks));
            }
        }

        if (debugEnabled) {
            plugin.getLogger().info("[Hardness] Loaded multipliers: " + multipliers);
            plugin.getLogger().info("[Hardness] Loaded classes: " + hardnessClasses.keySet());
        }
    }

    /**
     * Returns multiplier for a given block type based on its hardness class.
     */
    public double getHardnessMultiplier(Material material) {
        String matName = material.toString();
        for (Map.Entry<String, Set<String>> entry : hardnessClasses.entrySet()) {
            if (entry.getValue().contains(matName)) {
                double multiplier = multipliers.getOrDefault(entry.getKey(), 1.0);
                if (debugEnabled) {
                    plugin.getLogger().info("[Hardness] " + matName + " → Class: " + entry.getKey() + " → Multiplier: " + multiplier);
                }
                return multiplier;
            }
        }
        if (debugEnabled) {
            plugin.getLogger().info("[Hardness] " + matName + " → No class found, using 1.0");
        }
        return 1.0;
    }

    /**
     * Returns advancement points from advancements.yml or default.
     */
    public int getAdvancementPoints(String key) {
        return advancementsConfig.getInt(key, defaultAdvancementPoints);
    }

    public int getBlockBreakEvery() { return blockBreakEvery; }

    public int getBlockBreakPoints() { return blockBreakPoints; }

    public int getBlockPlaceEvery() { return blockPlaceEvery; }

    public int getBlockPlacePoints() { return blockPlacePoints; }

    public int getEndBossPoints() { return endBossPoints; }

    public boolean isDebug() { return debugEnabled; }

    /**
     * Utility method for accessing nested config values.
     * Example: getConfigSectionValue("block-activity.break.fatigue-decay-per-minute")
     */
    public double getConfigSectionValue(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }

    /**
     * Generates missing entries in advancements.yml for all registered advancements.
     */
    public void generateAdvancementConfig(JavaPlugin plugin) {
        boolean changed = false;

        // Entferne unerwünschte Einträge (Rezepte)
        Set<String> existingKeys = new HashSet<>(advancementsConfig.getKeys(false));
        for (String key : existingKeys) {
            if (key.startsWith("minecraft:recipes/")) {
                advancementsConfig.set(key, null);
                changed = true;
            }
        }

        // Füge neue Advancements hinzu (ohne Rezepte)
        Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
        while (it.hasNext()) {
            Advancement advancement = it.next();
            String key = advancement.getKey().toString();

            if (key.startsWith("minecraft:recipes/")) continue;

            if (!advancementsConfig.contains(key)) {
                advancementsConfig.set(key, defaultAdvancementPoints);
                changed = true;
            }
        }

        // Speichern falls nötig
        if (changed) {
            try {
                advancementsConfig.save(new File(plugin.getDataFolder(), "advancements.yml"));
                plugin.getLogger().info("Advancement configuration updated (recipes excluded, defaults applied).");
            } catch (IOException e) {
                plugin.getLogger().warning("Error saving advancements.yml: " + e.getMessage());
            }
        }
    }

    public double getFatigueDecay() {
        return config.getDouble("block-activity.break.fatigue-decay-per-minute", 0.2);
    }
}
