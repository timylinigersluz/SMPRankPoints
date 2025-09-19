package ch.ksrminecraft.sMPRankPoints.listeners;

import ch.ksrminecraft.RankPointsAPI.PointsAPI;
import ch.ksrminecraft.sMPRankPoints.utils.ConfigManager;
import ch.ksrminecraft.sMPRankPoints.utils.LogHelper;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerActionListener implements Listener {

    private final PointsAPI pointsAPI;
    private final ConfigManager config;
    private final LogHelper logger;

    private final Map<UUID, Long> lastBreakTimestamps = new HashMap<>();
    private final Map<UUID, Long> lastPlaceTimestamps = new HashMap<>();
    private final Map<UUID, Double> blockBreakBuffer = new HashMap<>();
    private final Map<UUID, Double> blockPlaceBuffer = new HashMap<>();
    private final Map<UUID, Double> fatigueBreak = new HashMap<>();
    private final Map<UUID, Double> fatiguePlace = new HashMap<>();

    public PlayerActionListener(PointsAPI pointsAPI, ConfigManager config, LogHelper logger) {
        this.pointsAPI = pointsAPI;
        this.config = config;
        this.logger = logger;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Material blockType = event.getBlock().getType();

        long now = System.currentTimeMillis();
        long lastBreak = lastBreakTimestamps.getOrDefault(uuid, now);
        double minutesElapsed = (now - lastBreak) / 60000.0;

        double fatigue = fatigueBreak.getOrDefault(uuid, 1.0);
        fatigue = Math.max(1.0, fatigue - minutesElapsed * config.getFatigueDecay("break"));
        fatigue += config.getFatigueGain("break");
        fatigueBreak.put(uuid, fatigue);

        lastBreakTimestamps.put(uuid, now);

        double hardnessMultiplier = config.getHardnessMultiplier(blockType);
        double buffered = blockBreakBuffer.getOrDefault(uuid, 0.0);
        buffered += hardnessMultiplier;
        blockBreakBuffer.put(uuid, buffered);

        int baseThreshold = config.getBaseThreshold("break");
        int threshold = (int) Math.ceil(baseThreshold * fatigue);

        if (buffered >= threshold) {
            givePoints(player, 1, "BlockBreak (Fatigue: " + String.format("%.2f", fatigue) + ")");
            blockBreakBuffer.put(uuid, 0.0);
        }

        if (config.isDebug()) {
            debugProgress(uuid, player, buffered, threshold, "BlockBreak");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        long now = System.currentTimeMillis();
        long lastPlace = lastPlaceTimestamps.getOrDefault(uuid, now);
        double minutesElapsed = (now - lastPlace) / 60000.0;

        double fatigue = fatiguePlace.getOrDefault(uuid, 1.0);
        fatigue = Math.max(1.0, fatigue - minutesElapsed * config.getFatigueDecay("place"));
        fatigue += config.getFatigueGain("place");
        fatiguePlace.put(uuid, fatigue);

        lastPlaceTimestamps.put(uuid, now);

        double buffered = blockPlaceBuffer.getOrDefault(uuid, 0.0);
        buffered += 1.0;
        blockPlaceBuffer.put(uuid, buffered);

        int baseThreshold = config.getBaseThreshold("place");
        int threshold = (int) Math.ceil(baseThreshold * fatigue);

        if (buffered >= threshold) {
            givePoints(player, 1, "BlockPlace (Fatigue: " + String.format("%.2f", fatigue) + ")");
            blockPlaceBuffer.put(uuid, 0.0);
        }

        if (config.isDebug()) {
            debugProgress(uuid, player, buffered, threshold, "BlockPlace");
        }
    }

    @EventHandler
    public void onEnderDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;

        int points = config.getEndBossPoints();
        Vector dragonLoc = event.getEntity().getLocation().toVector();

        for (Player p : event.getEntity().getWorld().getPlayers()) {
            double distance = p.getLocation().toVector().distance(dragonLoc);
            if (distance <= 150) {
                givePoints(p, points, "EnderDragon kill (in range)");
            }
        }
    }

    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        String key = event.getAdvancement().getKey().toString();

        if (key.startsWith("minecraft:recipes/")) {
            if (config.isDebug()) {
                logger.debug("Ignored recipe advancement: {} ({})", key, player.getName());
            }
            return;
        }

        int points = config.getAdvancementPoints(key);
        if (points <= 0) {
            if (config.isDebug()) {
                logger.debug("Advancement '{}' gives 0 points – skipping ({})", key, player.getName());
            }
            return;
        }

        pointsAPI.addPoints(player.getUniqueId(), points);

        String displayName = LegacyComponentSerializer.legacySection().serialize(
                event.getAdvancement().displayName()
        );

        // Deutsch für Spieler
        player.sendMessage("§a✔ Du hast das Advancement §e" + displayName + "§a erreicht!");
        player.sendMessage("§b+ " + points + " Rangpunkte verdient.");
        player.sendMessage("§7Bleib dran – wer mehr erreicht, steigt schneller auf!");

        if (config.isDebug()) {
            int total = pointsAPI.getPoints(player.getUniqueId());
            logger.debug("{} earned {} points for advancement '{}' (Total: {})",
                    player.getName(), points, key, total);
        }
    }

    private void givePoints(Player player, int points, String reason) {
        if (points <= 0) return;

        UUID uuid = player.getUniqueId();
        pointsAPI.addPoints(uuid, points); // void-Aufruf

        if (config.isDebug()) {
            int total = pointsAPI.getPoints(uuid);
            logger.debug("{} received {} points for {} (Total: {})",
                    player.getName(), points, reason, total);
        }
    }

    private void debugProgress(UUID uuid, Player player, double buffered, int threshold, String action) {
        int barLength = 10;
        double progress = Math.min(1.0, buffered / threshold);
        int filled = (int) (barLength * progress);
        String bar = "▓".repeat(filled) + "░".repeat(barLength - filled);
        double percent = 100.0 * progress;
        logger.debug("[Debug] {} → {} progress: ({:.0f} / {}) {} {:.0f}%",
                player.getName(), action, buffered, threshold, bar, percent);
    }
}
