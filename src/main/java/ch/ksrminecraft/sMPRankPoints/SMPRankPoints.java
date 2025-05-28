package ch.ksrminecraft.sMPRankPoints;

import ch.ksrminecraft.RankPointsAPI.PointsAPI;
import ch.ksrminecraft.sMPRankPoints.commands.PointsCommand;
import ch.ksrminecraft.sMPRankPoints.commands.ResetAdvancementsCommand;
import ch.ksrminecraft.sMPRankPoints.listeners.PlayerActionListener;
import ch.ksrminecraft.sMPRankPoints.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.logging.Logger;

public class SMPRankPoints extends JavaPlugin {

    private ConfigManager configManager;
    private PointsAPI pointsAPI;
    private Logger logger;

    // Globale Referenz auf die Plugininstanz
    public static SMPRankPoints instance;

    @Override
    public void onEnable() {
        instance = this;
        this.logger = getLogger();

        // Konfigurationen laden
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.generateAdvancementConfig(this);

        // RankPointsAPI initialisieren
        String url = getConfig().getString("mysql.host");
        String user = getConfig().getString("mysql.user");
        String pass = getConfig().getString("mysql.password");
        boolean debug = getConfig().getBoolean("debug", false);

        if (url == null || user == null || pass == null) {
            logger.severe("MySQL-Zugangsdaten fehlen in der config.yml!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            this.pointsAPI = new PointsAPI(url, user, pass, logger, debug);
        } catch (Exception e) {
            logger.severe("Fehler beim Initialisieren der RankPointsAPI: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Events und Befehle registrieren
        Bukkit.getPluginManager().registerEvents(new PlayerActionListener(pointsAPI, configManager), this);
        getCommand("resetadvancements").setExecutor(new ResetAdvancementsCommand());
        Objects.requireNonNull(getCommand("points")).setExecutor(new PointsCommand(pointsAPI));
    }

    /**
     * Befehl /apreload – lädt Konfigurationsdateien neu.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, String label, @NotNull String[] args) {
        if (label.equalsIgnoreCase("apreload")) {
            reloadConfig();
            configManager.loadConfig(this);
            configManager.generateAdvancementConfig(this);
            sender.sendMessage("§aSMPRankPoints Config neu geladen.");
            return true;
        }
        return false;
    }

    public PointsAPI getPointsAPI() {
        return pointsAPI;
    }
}
