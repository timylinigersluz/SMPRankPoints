package ch.ksrminecraft.sMPRankPoints.commands;

import ch.ksrminecraft.rangAPI.DBAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PointsCommand implements CommandExecutor {

    private final DBAPI dbAPI;

    public PointsCommand(DBAPI dbAPI) {
        this.dbAPI = dbAPI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                int points = dbAPI.getPoints(player);
                player.sendMessage("§aDu hast §e" + points + " §aPunkte.");
            } else {
                sender.sendMessage("§cNur Spieler können ihren eigenen Punktestand abfragen.");
            }
        } else if (args.length == 1 && sender.hasPermission("smprankpoints.admin")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (target.hasPlayedBefore() && target.getPlayer() != null) {
                int points = dbAPI.getPoints(target.getPlayer());
                sender.sendMessage("§e" + target.getName() + " §ahat §e" + points + " §aPunkte.");
            } else {
                sender.sendMessage("§cSpieler nicht gefunden oder war nie online.");
            }
        } else {
            sender.sendMessage("§cVerwendung: /points [spieler]");
        }
        return true;
    }
}
