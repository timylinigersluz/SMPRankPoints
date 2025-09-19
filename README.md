# SMPRankPoints

Ein **Minecraft Paper Plugin** zur **automatisierten Vergabe von Rangpunkten** für Aktionen wie Blockabbau, Blockplatzierung, Advancement-Abschlüsse und das Besiegen des Enderdrachens.  
Die Punkte werden **direkt in die globale MySQL-Datenbank** geschrieben – über die [RankPointsAPI](https://github.com/timylinigersluz/RankPointsAPI).  
Diese Punkte dienen als Grundlage für Ränge (z. B. Iron, Bronze, Gold …), die im Cluster über das RankProxyPlugin verwaltet und angezeigt werden.

---

## 🔧 Voraussetzungen

- Minecraft Server: [Paper](https://papermc.io/) 1.21+
- Java 17 oder höher (empfohlen: Java 21)
- MySQL-Datenbank (erreichbar für den SMP-Server)
- [RankProxyPlugin](https://github.com/timylinigersluz/RankProxyPlugin), um Ränge und `/rankinfo` bereitzustellen

---

## 📦 Installation

1. Baue das Plugin mit Maven:

   ```bash
   mvn clean package
   ```

   oder lade die vorkompilierte `SMPRankPoints-x.x.jar` aus dem `target/`-Ordner.

2. Lege die Datei `SMPRankPoints.jar` in den `plugins/`-Ordner deines SMP-Servers.

3. Stelle sicher, dass auch die `RankPointsAPI`-Bibliothek im Projekt verfügbar ist (z. B. via Proxy oder Dependency).

4. Starte den Server neu.  
   → Die Konfigurationsdateien (`config.yml`, `advancements.yml`, `hardness.yml`) werden automatisch erstellt.

---

## ⚙️ Konfiguration

Beim ersten Start werden **drei Konfigurationsdateien** erstellt:

### 1. `config.yml`

```yaml
# =========================================================
# Konfiguration für SMPRankPoints
# Punktevergabe für Advancements, Blockabbau/-platzieren und Endboss-Kills
# =========================================================

debug: true

log:
  level: INFO     # ERROR, WARN, INFO, DEBUG, TRACE

mysql:
  host: "your-host.com:3306/database"
  user: "your_mysql_user"
  password: "your_mysql_password"

block-activity:
  break:
    every: 10
    fatigue:
      gain-per-action: 0.05
      decay-per-minute: 1.0

  place:
    every: 5
    fatigue:
      gain-per-action: 0.05
      decay-per-minute: 1.0

points:
  endboss-kill:
    ender_dragon: 10

defaults:
  advancement-points: 1

staff:
  give-points: true
```

**Erläuterung der wichtigsten Einstellungen:**

- `debug`: Zusätzliche Debug-Ausgaben für Entwickler/Admins.
- `log.level`: Steuerung der Logtiefe (INFO = normal, DEBUG = detailliert).
- `mysql`: Zugangsdaten für die globale Rang-Datenbank.
- `block-activity.break` / `place`: Definiert, wie viele Blöcke für einen Punkt nötig sind.
    - `fatigue.gain-per-action`: Wie stark Ermüdung pro Aktion steigt.
    - `fatigue.decay-per-minute`: Wie schnell Ermüdung wieder abnimmt.
- `points.endboss-kill.ender_dragon`: Punkte für Spieler im Umkreis, wenn der Enderdrache stirbt.
- `defaults.advancement-points`: Standardpunkte pro Advancement, wenn nichts anderes gesetzt ist.
- `staff.give-points`: Steuert, ob Staff Punkte sammeln darf (`false` = Staff wird ausgeschlossen, gesteuert durch die zentrale Staffliste im Proxy).

---

### 2. `advancements.yml`

- Enthält **alle erkannten Minecraft-Advancements** (außer Rezepte, die automatisch ignoriert werden).
- Für jedes Advancement kannst du **individuelle Punktewerte** festlegen.
- Beispiel:
  ```yaml
  minecraft:adventure/kill_a_mob: 2
  minecraft:end/kill_dragon: 25
  minecraft:story/mine_stone: 0
  ```

---

### 3. `hardness.yml`

- Definiert **Härteklassen für Blöcke** und deren Multiplikatoren.
- Damit kannst du einstellen, dass z. B. **Erze** beim Abbau doppelt zählen, während **Erde** nur einfach zählt.
- Beispiel:
  ```yaml
  multipliers:
    STONE: 1.0
    ORE: 2.0
  blocks:
    STONE: [STONE, COBBLESTONE]
    ORE: [IRON_ORE, GOLD_ORE, DIAMOND_ORE]
  ```

---

## 🚀 Anwendungsfälle (Use Cases)

- **Mining:** Spieler baut Steine und Erze ab → Punkte werden vergeben, Fatigue bremst Massenspam.
- **Bauen:** Spieler platziert viele Blöcke → Punkte, aber Ermüdung verhindert AFK-Spam durch Autoskripte.
- **Abenteuer:** Advancements wie „Diamanten finden“ geben Punkte.
- **Endgame:** Drachen-Kill gibt allen in der Nähe Punkte.
- **Fairness für Staff:** Mit `staff.give-points: false` sammelt Staff keine Punkte und bleibt neutral.

---

## 📜 Befehle

| Befehl                      | Beschreibung                                | Permission                           |
|------------------------------|---------------------------------------------|--------------------------------------|
| `/srpreload`                 | Lädt die Konfigurationsdateien neu          | `smprankpoints.admin`                |
| `/resetadvancements <player>`| Setzt alle Advancements für einen Spieler zurück | `smprankpoints.resetadvancements` |

> **Kein `/points` mehr in SMPRankPoints!**  
> Punkteanzeige erfolgt über das Proxy-Plugin mit `/rankinfo`.

---

## 💡 Zusammenspiel mit RankProxyPlugin

- SMPRankPoints vergibt **nur Punkte**.
- RankProxyPlugin (auf dem Proxy) übernimmt:
    - Automatische **Beförderungen** basierend auf Punkten.
    - Anzeigen der Punkte/Ränge mit `/rankinfo`.
    - Verwaltung der zentralen Staffliste.

---

## 🧑‍💻 Entwicklerinfos

Das Plugin nutzt die **RankPointsAPI**.  
Beispiel:

```java
api.addPoints(UUID uuid, int delta); // Punkte hinzufügen
api.setPoints(UUID uuid, int points); // Punkte setzen
int current = api.getPoints(UUID uuid); // Punktestand abfragen
```

Die `PointsAPI` kümmert sich um:
- DB-Verbindung (MySQL)
- Tabellenanlage (`points`, `stafflist`)
- Staff-Ausschluss, falls gewünscht

---

## 👤 Autor & Lizenz

- Autor: **Timy Liniger** (KSR Minecraft SMP Projekt)
- Lizenz: **Open Source** (freie Nutzung & Anpassung, mit Quellenangabe)  
