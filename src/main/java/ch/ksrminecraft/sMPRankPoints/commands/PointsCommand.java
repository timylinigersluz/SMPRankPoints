package ch.ksrminecraft.sMPRankPoints.commands;

import ch.ksrminecraft.RankPointsAPI.PointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Befehlsklasse für /points.
 * Ermöglicht es Spielern, ihren aktuellen Punktestand abzufragen.
 * Admins mit der Berechtigung "smprankpoints.admin" können auch den Punktestand anderer Spieler einsehen.
 */
public class PointsCommand implements CommandExecutor {

    private final PointsAPI pointsAPI;

    public PointsCommand(PointsAPI pointsAPI) {
        this.pointsAPI = pointsAPI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Spieler fragt seinen eigenen Punktestand ab
        if (args.length == 0) {
            if (sender instanceof Player player) {
                UUID uuid = player.getUniqueId();
                int points = pointsAPI.getPoints(uuid);
                player.sendMessage("§aDu hast §e" + points + " §aPunkte.");
            } else {
                sender.sendMessage("§cNur Spieler können ihren eigenen Punktestand abfragen.");
            }

            // Admin fragt Punktestand eines anderen Spielers ab
        } else if (args.length == 1 && sender.hasPermission("smprankpoints.admin")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            UUID uuid = target.getUniqueId();

            if (target.hasPlayedBefore()) {
                int points = pointsAPI.getPoints(uuid);
                sender.sendMessage("§e" + target.getName() + " §ahat §e" + points + " §aPunkte.");
            } else {
                sender.sendMessage("§cSpieler nicht gefunden oder war nie online.");
            }

            // Falsche Verwendung
        } else {
            sender.sendMessage("§cVerwendung: /points [spieler]");
        }

        return true;
    }
}
