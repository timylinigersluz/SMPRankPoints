# SMPRankPoints

Ein Minecraft Paper Plugin zur automatisierten Vergabe von Rangpunkten für Aktionen wie Blockplatzierung, Blockabbau, Advancement-Abschlüsse und das Besiegen des Enderdrachens.  
Die Punkte werden direkt in die globale Rangdatenbank geschrieben – über das [RangAPI](https://github.com/samhuwsluz/RangAPI)-Plugin.

---

## 🔧 Voraussetzungen

- Minecraft Server: [Paper](https://papermc.io/) 1.21+
- Java 17 oder höher (empfohlen: Java 21)
- [RangAPI Plugin](https://github.com/samhuwsluz/RangAPI) im `plugins/`-Ordner

---

## 📦 Installation

1. Baue das Plugin mit Maven:

   ```bash
   mvn clean package
   ```

   oder lade die vorkompilierte `SMPRankPoints-x.x.jar` aus dem `target/`-Ordner.

2. Lege die Datei `SMPRankPoints.jar` in den `plugins/`-Ordner deines Servers.

3. Stelle sicher, dass sich **auch `RangAPI.jar` im plugins-Ordner** befindet.

4. Starte den Server neu.

---

## ⚙️ Konfiguration

Beim ersten Start werden zwei Konfigurationsdateien erstellt:

### `config.yml`

```yaml
debug: true

block-activity:
  break:
    every: 10
    points: 1
  place:
    every: 5
    points: 1

points:
  endboss-kill:
    ender_dragon: 100

defaults:
  advancement-points: 1
```

**Erläuterung:**
- `debug`: Gibt Konsolen-Logs bei Punktevergabe aus
- `block-activity`: Punktesystem für platzierte/abgebaute Blöcke
- `endboss-kill`: Punkte für das Besiegen des Enderdrachens
- `defaults.advancement-points`: Standardpunkte für alle Advancements

### `advancements.yml`

Diese Datei listet automatisch alle verfügbaren Minecraft-Advancements. Du kannst für jeden Eintrag individuelle Punkte setzen.

---

## 💡 Features

- ✔️ Vergibt Punkte bei:
  - Advancements
  - Blockplatzierung (nach konfigurierter Anzahl)
  - Blockabbau (nach konfigurierter Anzahl)
  - Enderdrachen-Kill (nur in Spielerreichweite)
- 🔄 `/apreload` Befehl zum Neuladen der Konfiguration
- 🧾 `/points` Befehl zur Anzeige der aktuellen Punkte
- 🔐 Adminrechte für Abfrage fremder Spielerpunkte
- 🔌 Automatische Integration mit RangAPI

---

## 📜 Befehle

| Befehl               | Beschreibung                                 | Rechte                      |
|----------------------|----------------------------------------------|-----------------------------|
| `/points`            | Zeigt eigenen Punktestand an                 | `smprankpoints.points`      |
| `/points <spieler>`  | Zeigt Punktestand eines Spielers an          | `smprankpoints.admin`       |
| `/apreload`          | Lädt die Konfigurationsdateien neu           | `smprankpoints.admin`       |

---

## 🔗 Abhängigkeiten

Dieses Plugin verwendet:

- [RangAPI](https://github.com/samhuwsluz/RangAPI) zur Punktespeicherung
- [PaperMC API](https://papermc.io) als Server-Backend

---

## 🧑‍💻 Entwickler

**Maven-Abhängigkeit für RangAPI:**

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.Samhuwsluz</groupId>
    <artifactId>RangAPI</artifactId>
    <version>test-1</version>
    <scope>provided</scope>
  </dependency>
</dependencies>
```

---

## 🙋‍♂️ Autor

- 🧑‍🏫 Timy Liniger (KSR Minecraft SMP Projekt)
- 📧 Kontakt via GitHub Issues oder direkt über [ksrminecraft.ch](https://ksrminecraft.ch)

---

## 📄 Lizenz

Dieses Plugin ist Open Source. Du kannst es frei verwenden und anpassen.  
Wenn du es weiterverwendest, gib bitte die ursprüngliche Quelle an.