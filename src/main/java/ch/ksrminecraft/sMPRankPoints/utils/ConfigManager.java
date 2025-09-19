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

public class ConfigManager {

    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final LogHelper logger;
    private FileConfiguration advancementsConfig;
    private FileConfiguration hardnessConfig;

    private int endBossPoints;
    private int defaultAdvancementPoints;
    private final Map<String, Set<String>> hardnessClasses = new HashMap<>();
    private final Map<String, Double> multipliers = new HashMap<>();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();

        // Logger von der Hauptklasse übernehmen
        this.logger = ((ch.ksrminecraft.sMPRankPoints.SMPRankPoints) plugin).getLoggerHelper();

        loadConfig(plugin);
    }

    public void loadConfig(JavaPlugin plugin) {
        // advancements.yml laden oder aus Ressourcen extrahieren
        File advFile = new File(plugin.getDataFolder(), "advancements.yml");
        if (!advFile.exists()) {
            plugin.saveResource("advancements.yml", false);
            logger.info("advancements.yml aus Ressourcen erstellt.");
        }
        advancementsConfig = YamlConfiguration.loadConfiguration(advFile);

        // hardness.yml laden oder aus Ressourcen extrahieren
        File hardnessFile = new File(plugin.getDataFolder(), "hardness.yml");
        if (!hardnessFile.exists()) {
            plugin.saveResource("hardness.yml", false);
            logger.info("hardness.yml aus Ressourcen erstellt.");
        }
        hardnessConfig = YamlConfiguration.loadConfiguration(hardnessFile);
        loadHardnessConfig();

        // allgemeine Werte
        endBossPoints = config.getInt("points.endboss-kill.ender_dragon", 0);
        defaultAdvancementPoints = config.getInt("defaults.advancement-points", 0);

        generateAdvancementConfig(plugin);
    }

    private void loadHardnessConfig() {
        hardnessClasses.clear();
        multipliers.clear();

        if (hardnessConfig.contains("multipliers")) {
            for (String key : Objects.requireNonNull(hardnessConfig.getConfigurationSection("multipliers")).getKeys(false)) {
                double val = hardnessConfig.getDouble("multipliers." + key);
                multipliers.put(key.toUpperCase(), val);
            }
        }

        if (hardnessConfig.contains("blocks")) {
            for (String key : Objects.requireNonNull(hardnessConfig.getConfigurationSection("blocks")).getKeys(false)) {
                List<String> blocks = hardnessConfig.getStringList("blocks." + key);
                hardnessClasses.put(key.toUpperCase(), new HashSet<>(blocks));
            }
        }

        if (isDebug()) {
            logger.debug("[Hardness] Loaded multipliers: {}", multipliers);
            logger.debug("[Hardness] Loaded classes: {}", hardnessClasses.keySet());
        }
    }

    public double getHardnessMultiplier(Material material) {
        String matName = material.toString();
        for (Map.Entry<String, Set<String>> entry : hardnessClasses.entrySet()) {
            if (entry.getValue().contains(matName)) {
                double multiplier = multipliers.getOrDefault(entry.getKey(), 1.0);
                if (isDebug()) {
                    logger.debug("[Hardness] {} → Class: {} → Multiplier: {}", matName, entry.getKey(), multiplier);
                }
                return multiplier;
            }
        }
        if (isDebug()) {
            logger.debug("[Hardness] {} → No class found, using 1.0", matName);
        }
        return 1.0;
    }

    public int getAdvancementPoints(String key) {
        return advancementsConfig.getInt(key, defaultAdvancementPoints);
    }

    public int getBaseThreshold(String type) {
        return config.getInt("block-activity." + type + ".every", 10);
    }

    public double getFatigueGain(String type) {
        return config.getDouble("block-activity." + type + ".fatigue.gain-per-action", 0.05);
    }

    public double getFatigueDecay(String type) {
        return config.getDouble("block-activity." + type + ".fatigue.decay-per-minute", 1.0);
    }

    public int getEndBossPoints() {
        return endBossPoints;
    }

    public boolean isDebug() {
        return plugin.getConfig().getBoolean("debug", false);
    }

    public void generateAdvancementConfig(JavaPlugin plugin) {
        boolean changed = false;

        int count = 0;
        Iterator<Advancement> itCheck = Bukkit.getServer().advancementIterator();
        while (itCheck.hasNext()) {
            count++;
            itCheck.next();
        }
        logger.info("[SMPRankPoints] Anzahl erkannter Advancements: {}", count);

        Set<String> existingKeys = new HashSet<>(advancementsConfig.getKeys(false));
        for (String key : existingKeys) {
            if (key.startsWith("minecraft:recipes/")) {
                advancementsConfig.set(key, null);
                changed = true;
            }
        }

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

        if (changed) {
            try {
                advancementsConfig.save(new File(plugin.getDataFolder(), "advancements.yml"));
                logger.info("Advancement configuration updated (recipes excluded, defaults applied).");
            } catch (IOException e) {
                logger.error("Error saving advancements.yml: {}", e.getMessage());
            }
        }
    }

    public boolean isStaffPointsEnabled() {
        return plugin.getConfig().getBoolean("staff.give-points", false);
    }
}
