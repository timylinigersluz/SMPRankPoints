package ch.ksrminecraft.sMPRankPoints.commands;

import ch.ksrminecraft.sMPRankPoints.SMPRankPoints;
import ch.ksrminecraft.sMPRankPoints.utils.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadConfigCommand implements CommandExecutor {

    private final SMPRankPoints plugin;

    public ReloadConfigCommand(SMPRankPoints plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.reloadConfig();
        plugin.getLoggerHelper().info("Reloading configuration...");

        ConfigManager newConfig = new ConfigManager(plugin);
        plugin.setConfigManager(newConfig);

        plugin.getLoggerHelper().info("Configuration successfully reloaded.");

        sender.sendMessage((sender instanceof Player ? "§a" : "") + "[SMPRankPoints] Konfiguration neu geladen.");
        return true;
    }
}
