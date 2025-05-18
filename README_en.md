# SMPRankPoints

A Minecraft Paper plugin for automatically awarding global rank points for specific actions like block breaking, placing, completing advancements, and killing the Ender Dragon.  
Points are written directly to the global rank database using the [RangAPI](https://github.com/samhuwsluz/RangAPI) plugin.

---

## 🔧 Requirements

- Minecraft Server: [Paper](https://papermc.io/) 1.21+
- Java 17 or higher (recommended: Java 21)
- [RangAPI Plugin](https://github.com/samhuwsluz/RangAPI) in the `plugins/` folder

---

## 📦 Installation

1. Build the plugin using Maven:

   ```bash
   mvn clean package
   ```

   Or download the precompiled `SMPRankPoints-x.x.jar` from the `target/` folder.

2. Place `SMPRankPoints.jar` into your server's `plugins/` directory.

3. Make sure that **`RangAPI.jar` is also present** in the plugins folder.

4. Start the server.

---

## ⚙️ Configuration

Two config files are created automatically on first start:

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

**Explanation:**
- `debug`: Logs point transactions to the console
- `block-activity`: Points for breaking or placing blocks
- `endboss-kill`: Points for killing the Ender Dragon
- `defaults.advancement-points`: Default points per advancement

### `advancements.yml`

This file lists all available Minecraft advancements and allows per-advancement point configuration.

---

## 💡 Features

- ✔️ Automatically awards points for:
  - Advancements
  - Block placement (after X blocks)
  - Block breaking (after X blocks)
  - Ender Dragon kill (within range)
- 🔄 `/apreload` command to reload configuration
- 🧾 `/points` command to display current points
- 🔐 Admin permission to view other players' points
- 🔌 Seamless integration with RangAPI

---

## 📜 Commands

| Command               | Description                             | Permission                |
|----------------------|-----------------------------------------|---------------------------|
| `/points`            | Show your own point total               | `smprankpoints.points`    |
| `/points <player>`   | Show points of another player           | `smprankpoints.admin`     |
| `/apreload`          | Reload the configuration files          | `smprankpoints.admin`     |

---

## 🔗 Dependencies

This plugin uses:

- [RangAPI](https://github.com/samhuwsluz/RangAPI) for global point storage
- [PaperMC API](https://papermc.io) for the server backend

---

## 🧑‍💻 Development

**Maven dependency for RangAPI:**

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

## 🙋‍♂️ Author

- 🧑‍🏫 Timy Liniger (KSR Minecraft SMP Project)
- 📧 Contact via GitHub Issues or at [ksrminecraft.ch](https://ksrminecraft.ch)

---

## 📄 License

This plugin is open source. You are free to use and adapt it.  
Please credit the original source when redistributing.