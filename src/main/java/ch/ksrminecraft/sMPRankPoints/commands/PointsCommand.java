package ch.ksrminecraft.sMPRankPoints.commands;

import ch.ksrminecraft.rangAPI.DBAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Befehlsklasse für /points.
 * Ermöglicht es Spielern, ihren aktuellen Punktestand abzufragen.
 * Admins mit der Berechtigung "smprankpoints.admin" können auch den Punktestand anderer Spieler einsehen.
 */
public class PointsCommand implements CommandExecutor {

    // Referenz zur Rang-Datenbank-API (z. B. aus RangAPI)
    private final DBAPI dbAPI;

    /**
     * Konstruktor für den PointsCommand.
     *
     * @param dbAPI Instanz der Rang-Datenbank-API zur Punkteabfrage
     */
    public PointsCommand(DBAPI dbAPI) {
        this.dbAPI = dbAPI;
    }

    /**
     * Führt den Befehl /points aus.
     * - Ohne Argumente: Gibt dem Spieler seinen eigenen Punktestand aus.
     * - Mit Spielername (und Adminrecht): Gibt den Punktestand eines anderen Spielers aus.
     *
     * @param sender  Der Sender des Befehls (Spieler oder Konsole)
     * @param command Das ausgeführte Befehl-Objekt
     * @param label   Der eigentliche Befehl, der eingegeben wurde
     * @param args    Zusätzliche Argumente (z. B. Spielername)
     * @return true, wenn der Befehl erfolgreich verarbeitet wurde
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Fall: Spieler fragt seinen eigenen Punktestand ab
        if (args.length == 0) {
            if (sender instanceof Player player) {
                int points = dbAPI.getPoints(player);
                player.sendMessage("§aDu hast §e" + points + " §aPunkte.");
            } else {
                sender.sendMessage("§cNur Spieler können ihren eigenen Punktestand abfragen.");
            }

            // Fall: Admin fragt Punktestand eines anderen Spielers ab
        } else if (args.length == 1 && sender.hasPermission("smprankpoints.admin")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

            if (target.hasPlayedBefore() && target.getPlayer() != null) {
                int points = dbAPI.getPoints(target.getPlayer());
                sender.sendMessage("§e" + target.getName() + " §ahat §e" + points + " §aPunkte.");
            } else {
                sender.sendMessage("§cSpieler nicht gefunden oder war nie online.");
            }

            // Fall: Falsche Verwendung
        } else {
            sender.sendMessage("§cVerwendung: /points [spieler]");
        }

        return true;
    }
}
