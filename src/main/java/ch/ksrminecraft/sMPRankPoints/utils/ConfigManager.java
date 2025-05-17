package ch.ksrminecraft.sMPRankPoints.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.advancement.Advancement;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class ConfigManager {

    private final FileConfiguration config;
    private FileConfiguration advancementsConfig;
    private boolean debugEnabled;

    private int endBossPoints;
    private int defaultAdvancementPoints;
    private int blockBreakEvery, blockBreakPoints;
    private int blockPlaceEvery, blockPlacePoints;

    public ConfigManager(JavaPlugin plugin) {
        this.config = plugin.getConfig();
        loadConfig(plugin);
    }

    public void loadConfig(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        File advFile = new File(plugin.getDataFolder(), "advancements.yml");
        if (!advFile.exists()) {
            plugin.saveResource("advancements.yml", false);
        }
        advancementsConfig = YamlConfiguration.loadConfiguration(advFile);
        debugEnabled = config.getBoolean("debug", false);

        endBossPoints = config.getInt("points.endboss-kill.ender_dragon", 0);
        defaultAdvancementPoints = config.getInt("defaults.advancement-points", 0);

        blockBreakEvery = config.getInt("block-activity.break.every", 0);
        blockBreakPoints = config.getInt("block-activity.break.points", 0);
        blockPlaceEvery = config.getInt("block-activity.place.every", 0);
        blockPlacePoints = config.getInt("block-activity.place.points", 0);
    }

    public void generateAdvancementConfig(JavaPlugin plugin) {
        boolean changed = false;

        Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
        while (it.hasNext()) {
            Advancement advancement = it.next();
            String key = advancement.getKey().toString();
            if (!advancementsConfig.contains(key)) {
                advancementsConfig.set(key, 0);
                changed = true;
            }
        }

        if (changed) {
            try {
                advancementsConfig.save(new File(plugin.getDataFolder(), "advancements.yml"));
                plugin.getLogger().info("Advancement-Konfiguration ergänzt.");
            } catch (IOException e) {
                plugin.getLogger().warning("Fehler beim Speichern der advancements.yml: " + e.getMessage());
            }
        }
    }

    public boolean isDebug() { return debugEnabled; }
    public int getAdvancementPoints(String key) {
        return advancementsConfig.getInt(key, defaultAdvancementPoints);
    }

    public int getBlockBreakEvery() { return blockBreakEvery; }
    public int getBlockBreakPoints() { return blockBreakPoints; }
    public int getBlockPlaceEvery() { return blockPlaceEvery; }
    public int getBlockPlacePoints() { return blockPlacePoints; }
    public int getEndBossPoints() { return endBossPoints; }
}
