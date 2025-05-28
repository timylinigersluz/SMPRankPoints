package ch.ksrminecraft.sMPRankPoints.commands;

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
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUsage: /resetadvancements <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage("§cPlayer not found or offline.");
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

        sender.sendMessage("§aAll advancements reset for §e" + target.getName() + "§a (" + count + " total).");
        target.sendMessage("§cYour advancements have been reset by an administrator.");

        return true;
    }
}
