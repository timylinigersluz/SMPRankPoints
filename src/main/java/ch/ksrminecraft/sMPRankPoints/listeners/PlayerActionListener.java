package ch.ksrminecraft.sMPRankPoints.listeners;

import ch.ksrminecraft.RankPointsAPI.PointsAPI;
import ch.ksrminecraft.sMPRankPoints.utils.ConfigManager;
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

    private final Map<UUID, Long> lastBreakTimestamps = new HashMap<>();
    private final Map<UUID, Double> blockBreakBuffer = new HashMap<>();
    private final Map<UUID, Integer> blockBreakCount = new HashMap<>();
    private final Map<UUID, Double> blockPlaceBuffer = new HashMap<>();
    private final Map<UUID, Integer> blockPlaceCount = new HashMap<>();

    public PlayerActionListener(PointsAPI pointsAPI, ConfigManager config) {
        this.pointsAPI = pointsAPI;
        this.config = config;
    }

    private void givePoints(Player player, int points, String reason) {
        if (points <= 0) return;

        UUID uuid = player.getUniqueId();
        boolean success = pointsAPI.addPoints(uuid, points);

        if (success && config.isDebug()) {
            int total = pointsAPI.getPoints(uuid);
            System.out.println("[SMPRankPoints] " + player.getName() + " received " + points + " points for " + reason + " (Total: " + total + ")");
        } else if (!success && config.isDebug()) {
            System.out.println("[SMPRankPoints] No points awarded to " + player.getName() + " – possibly staff");
        }
    }

    private int getDynamicBlockThreshold(int count) {
        return 10 + (int) Math.pow(count / 20.0, 2); // quadratic scaling
    }

    private void debugProgress(UUID uuid, Player player, double buffered, int threshold, String action) {
        int barLength = 10;
        double progress = Math.min(1.0, buffered / threshold);
        int filled = (int) (barLength * progress);
        String bar = "▓".repeat(filled) + "░".repeat(barLength - filled);
        double percent = 100.0 * progress;
        System.out.printf("[SMPRankPoints] [Debug] %s → %s progress: (%.0f / %d) %s %.0f%%%n",
                player.getName(), action, buffered, threshold, bar, percent);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Material blockType = event.getBlock().getType();

        long now = System.currentTimeMillis();
        long lastBreak = lastBreakTimestamps.getOrDefault(uuid, 0L);
        lastBreakTimestamps.put(uuid, now);

        double minutesElapsed = (now - lastBreak) / 60000.0;
        double fatigueDecay = config.getFatigueDecay();
        double fatigueFactor = Math.max(0.05, 1.0 - fatigueDecay * minutesElapsed);

        double hardnessMultiplier = config.getHardnessMultiplier(blockType);

        int count = blockBreakCount.getOrDefault(uuid, 0);
        int threshold = getDynamicBlockThreshold(count);
        count++;
        blockBreakCount.put(uuid, count);

        double buffered = blockBreakBuffer.getOrDefault(uuid, 0.0);
        buffered += fatigueFactor * hardnessMultiplier;

        if (buffered >= threshold) {
            givePoints(player, 1, "BlockBreak (" + count + " blocks, threshold: " + threshold + ")");
            blockBreakBuffer.put(uuid, 0.0);
            blockBreakCount.put(uuid, 0);
        } else {
            blockBreakBuffer.put(uuid, buffered);
        }

        if (config.isDebug()) {
            debugProgress(uuid, player, buffered, threshold, "BlockBreak");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        int count = blockPlaceCount.getOrDefault(uuid, 0);
        int threshold = getDynamicBlockThreshold(count);
        count++;
        blockPlaceCount.put(uuid, count);

        double buffered = blockPlaceBuffer.getOrDefault(uuid, 0.0);
        buffered += 1.0;

        if (buffered >= threshold) {
            givePoints(player, 1, "BlockPlace (" + count + " blocks, threshold: " + threshold + ")");
            blockPlaceBuffer.put(uuid, 0.0);
            blockPlaceCount.put(uuid, 0);
        } else {
            blockPlaceBuffer.put(uuid, buffered);
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
            if (!p.getWorld().equals(event.getEntity().getWorld())) continue;

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
        int points = config.getAdvancementPoints(key);

        if (points > 0) {
            boolean success = pointsAPI.addPoints(player.getUniqueId(), points);
            if (success) {
                int total = pointsAPI.getPoints(player.getUniqueId());
                if (config.isDebug()) {
                    System.out.println("[SMPRankPoints] " + player.getName() +
                            " earned " + points + " points for Advancement '" + key + "' (Total: " + total + ")");
                }
            } else if (config.isDebug()) {
                System.out.println("[SMPRankPoints] No points awarded for Advancement '" + key +
                        "' – possibly staff (" + player.getName() + ")");
            }
        } else if (config.isDebug()) {
            System.out.println("[SMPRankPoints] Advancement '" + key + "' gives 0 points → Ignored (" + player.getName() + ")");
        }
    }
}
