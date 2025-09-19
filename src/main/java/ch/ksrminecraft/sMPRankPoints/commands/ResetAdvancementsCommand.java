package ch.ksrminecraft.sMPRankPoints.commands;

import ch.ksrminecraft.sMPRankPoints.SMPRankPoints;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;

public class ResetAdvancementsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§cDu hast keine Berechtigung für diesen Befehl.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cVerwendung: /resetadvancements <Spieler>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage("§cSpieler nicht gefunden oder offline.");
            return true;
        }

        Iterator<Advancement> advancements = Bukkit.advancementIterator();
        int count = 0;
        while (advancements.hasNext()) {
            Advancement advancement = advancements.next();
            for (String criterion : advancement.getCriteria()) {
                target.getAdvancementProgress(advancement).revokeCriteria(criterion);
            }
            count++;
        }

        sender.sendMessage("§aAlle Advancements von §e" + target.getName() + " §awurden zurückgesetzt (§e" + count + "§a insgesamt).");
        target.sendMessage("§cDeine Advancements wurden von einem Administrator zurückgesetzt.");

        // Log-Ausgabe über LogHelper (englisch)
        SMPRankPoints.instance.getLoggerHelper()
                .info("Advancements of player {} were reset by {} ({} total).",
                        target.getName(),
                        sender.getName(),
                        count);

        return true;
    }
}
