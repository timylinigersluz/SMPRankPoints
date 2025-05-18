package ch.ksrminecraft.sMPRankPoints.listeners;

import ch.ksrminecraft.rangAPI.DBAPI;
import ch.ksrminecraft.sMPRankPoints.utils.ConfigManager;
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

/**
 * Listener-Klasse, die Spieleraktionen überwacht und
 * basierend auf bestimmten Events Punkte über DBAPI vergibt.
 */
public class PlayerActionListener implements Listener {

    private final DBAPI dbAPI;
    private final ConfigManager config;

    // Zähler pro Spieler für platzierte bzw. abgebauter Blöcke
    private final Map<UUID, Integer> breakCount = new HashMap<>();
    private final Map<UUID, Integer> placeCount = new HashMap<>();

    /**
     * Konstruktor.
     *
     * @param dbAPI   Zugriff auf die Rangpunkte-Datenbank
     * @param config  Zugriff auf die Plugin-Konfiguration
     */
    public PlayerActionListener(DBAPI dbAPI, ConfigManager config) {
        this.dbAPI = dbAPI;
        this.config = config;
    }

    /**
     * Vergibt Punkte an einen Spieler und gibt optional Debug-Ausgabe in der Konsole aus.
     *
     * @param player Der betroffene Spieler
     * @param points Die zu vergebenden Punkte
     * @param reason Der Grund (für die Debugausgabe)
     */
    private void givePoints(Player player, int points, String reason) {
        if (points <= 0) return;

        dbAPI.addPoints(player, points);
        int total = dbAPI.getPoints(player);

        if (config.isDebug()) {
            System.out.println("[SMPRankPoints] " + player.getName() + " erhielt " + points + " Punkte für " + reason + " (Gesamt: " + total + ")");
        }
    }

    /**
     * Event: Spieler schließt ein Advancement ab.
     * Die Punkte für dieses Advancement werden aus der Konfiguration gelesen.
     */
    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        String key = event.getAdvancement().getKey().toString();
        int points = config.getAdvancementPoints(key);
        givePoints(event.getPlayer(), points, "Advancement: " + key);
    }

    /**
     * Event: Spieler baut einen Block ab.
     * Punkte werden alle X Blöcke vergeben, basierend auf der config.yml.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int count = breakCount.getOrDefault(uuid, 0) + 1;
        breakCount.put(uuid, count);

        int every = config.getBlockBreakEvery();
        int reward = config.getBlockBreakPoints();

        if (every > 0 && count % every == 0) {
            givePoints(player, reward, "BlockBreak: " + count + " Blöcke");
        }
    }

    /**
     * Event: Spieler platziert einen Block.
     * Punkte werden alle X platzierte Blöcke vergeben.
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        int count = placeCount.getOrDefault(uuid, 0) + 1;
        placeCount.put(uuid, count);

        int every = config.getBlockPlaceEvery();
        int reward = config.getBlockPlacePoints();

        if (every > 0 && count % every == 0) {
            givePoints(player, reward, "BlockPlace: " + count + " Blöcke");
        }
    }

    /**
     * Event: Enderdrache stirbt.
     * Es werden nur Spieler in einem Umkreis von 150 Blöcken belohnt.
     */
    @EventHandler
    public void onEnderDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;

        int points = config.getEndBossPoints();
        Vector dragonLoc = event.getEntity().getLocation().toVector();

        for (Player p : event.getEntity().getWorld().getPlayers()) {
            // Nur Spieler in derselben Welt und in Reichweite erhalten Punkte
            if (!p.getWorld().equals(event.getEntity().getWorld())) continue;

            double distance = p.getLocation().toVector().distance(dragonLoc);
            if (distance <= 150) {
                givePoints(p, points, "EnderDragon Kill (in Reichweite)");
            }
        }
    }
}
