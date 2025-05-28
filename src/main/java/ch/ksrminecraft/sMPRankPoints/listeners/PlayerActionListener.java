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
    private final Map<UUID, Double> blockPlaceBuffer = new HashMap<>();

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

        double basePoints = config.getBlockBreakPoints();
        double hardnessMultiplier = config.getHardnessMultiplier(blockType);
        double gained = basePoints * fatigueFactor * hardnessMultiplier;

        double buffered = blockBreakBuffer.getOrDefault(uuid, 0.0);
        double total = buffered + gained;
        int toAward = (int) total;
        double newBuffer = total - toAward;
        blockBreakBuffer.put(uuid, newBuffer);

        if (toAward > 0 && config.isDebug()) {
            System.out.println("[SMPRankPoints] " + player.getName() + " + " + toAward + " point(s) for breaking " + blockType);
        }

        if (toAward > 0) {
            givePoints(player, toAward, "BlockBreak: " + blockType);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Material blockType = event.getBlock().getType();

        double basePoints = config.getBlockPlacePoints();
        double hardnessMultiplier = config.getHardnessMultiplier(blockType);
        double gainedPoints = basePoints * hardnessMultiplier;

        double total = blockPlaceBuffer.getOrDefault(uuid, 0.0) + gainedPoints;
        int toAward = (int) total;
        blockPlaceBuffer.put(uuid, total - toAward);

        if (toAward > 0 && config.isDebug()) {
            System.out.println("[SMPRankPoints] " + player.getName() + " + " + toAward + " point(s) for placing " + blockType);
        }

        if (toAward > 0) {
            givePoints(player, toAward, "BlockPlace: " + blockType);
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
