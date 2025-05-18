package ch.ksrminecraft.sMPRankPoints;

import ch.ksrminecraft.rangAPI.DBAPI;
import ch.ksrminecraft.rangAPI.RangAPI;
import ch.ksrminecraft.sMPRankPoints.commands.PointsCommand;
import ch.ksrminecraft.sMPRankPoints.listeners.PlayerActionListener;
import ch.ksrminecraft.sMPRankPoints.utils.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SMPRankPoints extends JavaPlugin {

    private ConfigManager configManager;

    // Globale Referenz auf die Plugininstanz
    public static SMPRankPoints instance;

    @Override
    public void onEnable() {
        instance = this;

        // Konfigurationen laden
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.generateAdvancementConfig(this);

        RangAPI rangAPI;
        DBAPI dbAPI;

        // RangAPI suchen und laden
        if (getServer().getPluginManager().getPlugin("RangAPI") instanceof RangAPI api) {
            rangAPI = api;
            dbAPI = rangAPI.dbAPI; // Zugriff auf RangAPI-Datenbankzugriff
        } else {
            getLogger().severe("RangAPI nicht gefunden!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Events und Befehle registrieren
        Bukkit.getPluginManager().registerEvents(new PlayerActionListener(dbAPI, configManager), this);
        Objects.requireNonNull(getCommand("points")).setExecutor(new PointsCommand(dbAPI));
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
}
