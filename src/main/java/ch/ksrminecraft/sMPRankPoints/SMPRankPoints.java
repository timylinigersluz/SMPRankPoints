package ch.ksrminecraft.sMPRankPoints;

import ch.ksrminecraft.RankPointsAPI.PointsAPI;
import ch.ksrminecraft.sMPRankPoints.commands.ReloadConfigCommand;
import ch.ksrminecraft.sMPRankPoints.commands.ResetAdvancementsCommand;
import ch.ksrminecraft.sMPRankPoints.listeners.PlayerActionListener;
import ch.ksrminecraft.sMPRankPoints.utils.ConfigManager;
import ch.ksrminecraft.sMPRankPoints.utils.LogHelper;
import ch.ksrminecraft.sMPRankPoints.utils.LogLevel;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SMPRankPoints extends JavaPlugin {

    private ConfigManager configManager;
    private PointsAPI pointsAPI;
    private LogHelper logger;

    // Globale Referenz auf die Plugininstanz
    public static SMPRankPoints instance;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        configManager = new ConfigManager(this);

        // Logger initialisieren (Level aus config.yml, Default INFO)
        LogLevel level = LogLevel.valueOf(getConfig().getString("log.level", "INFO").toUpperCase());
        this.logger = new LogHelper(getSLF4JLogger(), level);

        // Advancements erst generieren, wenn alles geladen ist
        Bukkit.getScheduler().runTask(this, () -> {
            configManager.generateAdvancementConfig(this);
        });

        // RankPointsAPI initialisieren (Logger = Bukkit-Logger!)
        String url = getConfig().getString("mysql.host");
        String user = getConfig().getString("mysql.user");
        String pass = getConfig().getString("mysql.password");
        boolean debug = getConfig().getBoolean("debug", false);

        // Staff-Handling: true = Staff kriegt Punkte, false = Staff ausgeschlossen
        boolean staffGivePoints = getConfig().getBoolean("staff.give-points", false);
        boolean excludeStaff = !staffGivePoints;

        if (url == null || user == null || pass == null) {
            logger.error("MySQL-Zugangsdaten fehlen in der config.yml!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            // Hier bewusst getLogger() (java.util.logging.Logger) für die API
            this.pointsAPI = new PointsAPI(url, user, pass, getLogger(), debug, excludeStaff);
        } catch (Exception e) {
            logger.error("Fehler beim Initialisieren der RankPointsAPI: {}", e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Listener & Commands registrieren
        Bukkit.getPluginManager().registerEvents(
                new PlayerActionListener(pointsAPI, configManager, logger), this);

        getCommand("resetadvancements").setExecutor(new ResetAdvancementsCommand());
        getCommand("srpreload").setExecutor(new ReloadConfigCommand(this));
    }

    public PointsAPI getPointsAPI() {
        return pointsAPI;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LogHelper getLoggerHelper() {
        return logger;
    }
}
