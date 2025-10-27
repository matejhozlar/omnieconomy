# OmniEconomy

## ⚠️ The mod is still in early development.

**A comprehensive economy system for Minecraft with deep configurability and mod integration support.**

[![CurseForge](https://img.shields.io/badge/CurseForge-Download-orange?style=flat&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/omnieconomy)
[![Modrinth](https://img.shields.io/badge/Modrinth-Download-green?style=flat&logo=modrinth)](https://modrinth.com/mod/omnieconomy)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.213+-blue?style=flat)](https://neoforged.net/)
[![Downloads](https://img.shields.io/curseforge/dt/1318335?logo=curseforge&label=Downloads&color=F16436&labelColor=2D2D2D)](https://www.curseforge.com/projects/1371365)

---

## Overview

OmniEconomy brings a fully-featured economy system to Minecraft, featuring physical currency, banking mechanics, player rewards, and seamless integration with popular mods like **Create**. Whether you're running a survival server or a complex modded economy, OmniEconomy provides the tools you need to create engaging financial gameplay.

### Key Features

- 💵 **Physical Currency System** - 8 denominations of bills ($1, $5, $10, $20, $50, $100, $500, $1000)
- 🏧 **ATM Blocks** - Deposit and withdraw currency with an intuitive GUI
- 🎮 **Player Commands** - Check balances, send money, view leaderboards
- 🎁 **Reward Systems** - Daily rewards and playtime-based income
- 👹 **Mob Drops** - Configurable currency drops from hostile mobs
- ✨ **Custom Enchantment** - Capitalist Greed enchantment increases mob drop rates
- 🎲 **Lottery System** - Server-wide lottery events with customizable rules
- 🔧 **Mod Integration** - Special support for Create mod's Stock Ticker system
- ⚙️ **Highly Configurable** - Fine-tune every aspect to fit your server's needs
- 💾 **Automatic Backups** - Optional rolling backups of economy data

---

## Getting Started

### Installation

1. Download the latest version from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/omnieconomy) or [Modrinth](https://modrinth.com/mod/omnieconomy)
2. Place the `.jar` file in your `mods` folder
3. Launch Minecraft with NeoForge
4. Configure the mod to your liking (optional)

### Requirements

- **Minecraft Version**: 1.21.1 
- **Mod Loader**: NeoForge
- **Optional Dependencies**:
    - Create (for Stock Ticker integration)
    - AFKStatus (for AFK detection in playtime rewards)

---

## Core Features

### Physical Currency

OmniEconomy introduces 8 physical bill items that can be traded, dropped, and stored like any other item:

| Denomination | Item          |
|-------------|---------------|
| $1 | `bill_1`      |  
| $5 | `bill_5`      |
| $10 | `bill_10`     |
| $20 | `bill_20`     |
| $50 | `bill_50`     |
| $100 | `bill_100`    |
| $500 | `bill_500`    |
| $1000 | `bill_1000`   |

### Banking System

#### ATM Block
Craft and place ATM blocks to manage your digital balance:

#### Using the ATM
1. **Right-click** on an ATM block to open the interface
2. Navigate using **WASD** or **Arrow Keys**
3. **Deposit**: Converts all bills in your inventory to digital currency
4. **Withdraw**:
    - **Enter Amount**: Specify exact amount (automatically optimizes bills)
    - **Choose Bills**: Manually select denomination and quantity

The ATM features a simulated PIN entry animation for immersion!

---

## Player Commands

### `/money`
Check your current balance.

**Usage:**
```
/money
```

**Output:**
```
💰 Balance: $1,234
```

---

### `/pay <player> <amount>`
Transfer money to another player.

**Usage:**
```
/pay Steve 500
```

**Features:**
- Instant transfers
- Notifications to both sender and receiver
- Prevents self-payments
- Checks for sufficient funds and max balance limits

---

### `/baltop`
View the server's richest players.

**Usage:**
```
/baltop
```

**Output:**
```
🏆 Top 10 Richest Players:
1. Steve: $10,000
2. Alex: $7,500
3. Notch: $5,000
...
```

---

### `/daily`
Claim your daily login reward.

**Usage:**
```
/daily
```

**Features:**
- Once per day claim
- Configurable reward amount
- Automatic reset at midnight UTC

---

### `/deposit`
Quickly deposit all bills from your inventory. *(Disabled by default)*

**Usage:**
```
/deposit
```

---

### `/withdraw <format>`
Withdraw money as physical bills. *(Disabled by default)*

**Formats:**

1. **Total Amount** (auto-optimized):
   ```
   /withdraw 250
   ```

2. **Specific Denomination**:
   ```
   /withdraw 50 5
   ```
   (Withdraws 5x $50 bills)

3. **Custom Bundle**:
   ```
   /withdraw 100:2 50:3 10:5
   ```
   (Withdraws 2x $100, 3x $50, 5x $10)

---

### `/lottery` *(Requires lottery enabled)*

#### Start a Lottery
```
/lottery start
```

#### Join a Lottery
```
/lottery join <amount>
```
Enter the active lottery with your bet.

#### Check Status
```
/lottery status
```
View current lottery details (time remaining, pot size, host).

**How it Works:**
- Lottery creator sets duration via config
- Players join by betting currency
- Winner selected proportionally to bet size (higher bets = higher chance)
- Winner takes entire pot
- Cooldown period between lotteries

---

## Reward Systems

### Daily Rewards
Players can claim a daily reward using `/daily`.

**Configuration:**
```toml
[dailyRewards]
    enableDailyReward = true
    amount = 100
```

### Playtime Rewards
Earn passive income for active playtime.

**Configuration:**
```toml
[playtimeRewards]
    enablePlaytimeRewards = false
    amount = 10
    intervalSeconds = 300  # Every 5 minutes
    dailyCap = 1000        # Max earnings per day
    playtimeRewardNotification = true
    messageFormat = "You received %s%d for being active!"
    afkIntegration = false  # Requires AFKStatus mod
```

**Features:**
- Configurable payout intervals
- Optional daily caps
- AFK detection (with AFKStatus mod)
- Customizable notification messages

---

## Mob Drops

Hostile mobs can drop currency bills when killed by players.

### Supported Mobs
- Zombies
- Skeletons
- Creepers
- Spiders
- Wither Skeletons
- Blazes

### Configuration
```toml
[mobFarming]
    enableMobDrops = true
    enableMobDropsLimit = true
    mobDropsLimit = 1000  # Daily cap per player
    warnOnMobDropsLimit = true
    
    # Individual mob drop chances (%)
    zombie1DollarChance = 2.0
    spider1DollarChance = 2.0
    creeper1DollarChance = 2.0
    skeleton1DollarChance = 2.0
    witherSkeleton1DollarChance = 2.0
    blaze1DollarChance = 2.0
    
    # Rare $5 bill chance (0.0-1.0)
    rareFiveDollarChance = 0.02
```

### Daily Limits
- Optional daily earning cap per player
- Prevents excessive mob farming
- Warning notification when limit reached
- Automatic reset at midnight UTC

---

## Capitalist Greed Enchantment

A custom weapon enchantment that increases currency drop rates from mobs.

### Details
- **Applicable to**: Swords and weapons
- **Max Level**: III
- **Effect**: Increases mob drop chance by configured percentage
- **Enchantment Table**: Available at higher levels

### Configuration
```toml
[enchantment]
    capitalistGreedI = 5.0    # +5% drop chance
    capitalistGreedII = 8.0   # +8% drop chance
    capitalistGreedIII = 10.0 # +10% drop chance
```

### Example
With Capitalist Greed III and a base zombie drop rate of 2%:
- Effective drop rate: 2% + 10% = **12% chance** per kill

---

## Mod Integration

### Create Mod - Stock Ticker

OmniEconomy features deep integration with Create's Stock Ticker system, enabling automated currency withdrawal for shopping lists.

#### Bank Card Item
A special item required for Stock Ticker integration.

**Usage:**
1. Create a Shopping List containing currency bills
2. Hold the **Shopping List** in your **main hand**
3. Hold the **Bank Card** in your **offhand**
4. Right-click a **Stock Ticker entity** or **Blaze Burner**
5. Bills are automatically withdrawn from your account

**Features:**
- Automatic inventory space checking
- Denomination matching
- Balance verification
- Success/failure feedback

**Tooltip Information:**
- Basic: "Required for Stock Ticker shopping list integration"
- Advanced (F3+H): Detailed usage instructions

#### How It Works
The integration uses reflection to interact with Create's Shopping List API, extracting payment requirements and dispensing the appropriate bills from your digital balance.

---

### AFKStatus Integration

When AFKStatus mod is installed, playtime rewards can respect AFK status.

**Configuration:**
```toml
afkIntegration = true
```

**Features:**
- Players marked as AFK won't earn playtime rewards
- Fallback to scoreboard detection if API unavailable
- Checks for "afk" team or scoreboard objective

---

## ⚙Configuration

OmniEconomy is designed to be highly configurable. All settings are found in `config/omnieconomy-common.toml`.

### General Settings
```toml
[general]
    currencySymbol = "$"
    commandCooldownMs = 5000  # 5 second cooldown
    baltopSize = 10
```

### Economy Settings
```toml
[economy]
    maxBalance = 2000000000  # Max account balance
```

### Command Toggles
```toml
[commands]
    enableMoneyCommand = true
    enablePayCommand = true
    enableBaltopCommand = true
    enableDepositCommand = false
    enableWithdrawCommand = false
```

### Lottery Settings
```toml
[lottery]
    enableLottery = true
    minBet = 10
    cooldownMinutes = 15
    durationSeconds = 120
    announcePlayerJoin = false
```

### Backup Settings
```toml
[persistence]
    enableBackups = false
    maxBackupFiles = 5
```

**Backup Location:** `world/data/backups/omnieconomy/`

**Format:** `omnieconomy-YYYYMMDD-HHmmss.dat`

---

## Data Storage

### Save Format
Economy data is stored in NBT format at:
```
world/data/omnieconomy.dat
```

### Stored Data
- Player balances
- Daily claim timestamps
- Playtime earnings (daily tracking)
- Mob drop daily limits

### Backup System
When enabled, the mod automatically creates timestamped backups on world save:
- Configurable retention (keep last N backups)
- Automatic pruning of old backups
- No manual intervention required

---

## Client Features

### ATM Screen
The ATM interface features:
- Simulated PIN entry with keypad animation
- Keyboard navigation (WASD/Arrow keys)
- Mouse support
- Real-time balance updates
- Status notifications with color coding
- Scrollable bill selection (mouse wheel support)

---

### Performance Notes
- Economy data is stored in memory with periodic saving
- Concurrent hashmaps for thread-safe access
- Minimal tick overhead (playtime and lottery checks)
- Backup operations are non-blocking

---

## Links

- **CurseForge**: [Download](https://www.curseforge.com/minecraft/mc-mods/omnieconomy)
- **Modrinth**: [Download](https://modrinth.com/mod/omnieconomy)
- **Issues**: Report bugs on the project's issue tracker
- **Discord**: *Soon*

---

## License

*Currently the mod has no license, All Rights reserved.*

---
## Support

If you enjoy OmniEconomy, consider:
- ⭐ Starring the project
- 📢 Sharing with friends
- 💬 Leaving a review on CurseForge/Modrinth

---

## Credits

**Developed by**: saunhardy

**Special Thanks**:
- Create mod team for the amazing mod and API
- NeoForge team for the modding framework
- The Minecraft modding community