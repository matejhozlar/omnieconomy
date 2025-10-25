package com.saunhardy.omnieconomy;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> CURRENCY_SYMBOL;
    public static final ModConfigSpec.IntValue COMMAND_COOLDOWN_MS;
    public static final ModConfigSpec.IntValue BALTOP_SIZE;

    public static final ModConfigSpec.IntValue MAX_BALANCE;

    public static final ModConfigSpec.BooleanValue ENABLE_MONEY_COMMAND;
    public static final ModConfigSpec.BooleanValue ENABLE_PAY_COMMAND;
    public static final ModConfigSpec.BooleanValue ENABLE_BALTOP_COMMAND;
    public static final ModConfigSpec.BooleanValue ENABLE_DEPOSIT_COMMAND;
    public static final ModConfigSpec.BooleanValue ENABLE_WITHDRAW_COMMAND;

    public static final ModConfigSpec.BooleanValue ENABLE_DAILY_REWARDS;
    public static final ModConfigSpec.IntValue DAILY_REWARD_AMOUNT;

    public static final ModConfigSpec.BooleanValue ENABLE_PLAYTIME_REWARDS;
    public static final ModConfigSpec.IntValue PLAYTIME_REWARD_AMOUNT;
    public static final ModConfigSpec.IntValue PLAYTIME_REWARD_INTERVAL_SECONDS;
    public static final ModConfigSpec.IntValue PLAYTIME_DAILY_CAP;
    public static final ModConfigSpec.BooleanValue NOTIFY_ON_PLAYTIME_REWARD;
    public static final ModConfigSpec.ConfigValue<String> PLAYTIME_REWARD_MESSAGE_FORMAT;

    public static final ModConfigSpec.BooleanValue ENABLE_MOB_DROPS;
    public static final ModConfigSpec.BooleanValue ENABLE_MOB_DROPS_LIMIT;
    public static final ModConfigSpec.BooleanValue WARN_ON_MOB_DROPS_LIMIT;
    public static final ModConfigSpec.IntValue MOB_DROPS_LIMIT;
    public static final ModConfigSpec.DoubleValue DROP_ZOMBIE_1;
    public static final ModConfigSpec.DoubleValue DROP_SPIDER_1;
    public static final ModConfigSpec.DoubleValue DROP_CREEPER_1;
    public static final ModConfigSpec.DoubleValue DROP_SKELETON_1;
    public static final ModConfigSpec.DoubleValue DROP_WITHER_SKELETON_1;
    public static final ModConfigSpec.DoubleValue DROP_BLAZE_1;
    public static final ModConfigSpec.DoubleValue RARE_FIVE_DOLLAR_CHANCE;

    public static final ModConfigSpec.DoubleValue CAPITALIST_GREED_1;
    public static final ModConfigSpec.DoubleValue CAPITALIST_GREED_2;
    public static final ModConfigSpec.DoubleValue CAPITALIST_GREED_3;

    public static final ModConfigSpec.BooleanValue ENABLE_LOTTERY;
    public static final ModConfigSpec.IntValue LOTTERY_MIN_BET;
    public static final ModConfigSpec.IntValue LOTTERY_COOLDOWN_MINUTES;

    public static final ModConfigSpec.BooleanValue ENABLE_BACKUPS;
    public static final ModConfigSpec.IntValue MAX_BACKUP_FILES;

    static {
        BUILDER.push("general");

        CURRENCY_SYMBOL = BUILDER
                .comment("Symbol used when formatting money.")
                .define("currencySymbol", "$");

        COMMAND_COOLDOWN_MS = BUILDER
                .comment("Global cooldown (milliseconds) for economy commands like /money, /pay, etc.")
                .defineInRange("commandCooldownMs", 5000, 0, 60_000);

        BALTOP_SIZE = BUILDER
                .comment("Number of entries to show for /baltop")
                .defineInRange("baltopSize", 10, 1, 1000);

        BUILDER.pop();

        BUILDER.push("economy");

        MAX_BALANCE = BUILDER
                .comment("Maximum allowed balance for any account. Use large value to effectively disable.")
                .defineInRange("maxBalance", 2_000_000_000, 0, Integer.MAX_VALUE);

        BUILDER.pop();

        BUILDER.push("commands");

        ENABLE_MONEY_COMMAND = BUILDER
                .comment("If false, /money will NOT be registered.")
                .define("enableMoneyCommand", true);

        ENABLE_PAY_COMMAND = BUILDER
                .comment("If false, /pay will NOT be registered.")
                .define("enablePayCommand", true);

        ENABLE_BALTOP_COMMAND = BUILDER
                .comment("If false, /baltop will NOT be registered.")
                .define("enableBaltopCommand", true);

        ENABLE_DEPOSIT_COMMAND = BUILDER
                .comment("If false, /deposit will NOT be registered.")
                .define("enableDepositCommand", false);

        ENABLE_WITHDRAW_COMMAND = BUILDER
                .comment("If false, /withdraw will NOT be registered.")
                .define("enableWithdrawCommand", false);

        BUILDER.pop();

        BUILDER.push("dailyRewards");

        ENABLE_DAILY_REWARDS = BUILDER
                .comment("Enable the /daily reward feature.")
                .define("enableDailyReward", true);

        DAILY_REWARD_AMOUNT = BUILDER
                .comment("Amount granted by /daily when available.")
                .defineInRange("amount", 100, 0, 1_000_000);

        BUILDER.pop();

        BUILDER.push("playtimeRewards");

        ENABLE_PLAYTIME_REWARDS = BUILDER
                .comment("Enable automatic payouts for time spent online.")
                .define("enablePlaytimeRewards", false);

        PLAYTIME_REWARD_AMOUNT = BUILDER
                .comment("Amount paid each interval to active players.")
                .defineInRange("amount", 10, 0, 1_000_000);

        PLAYTIME_REWARD_INTERVAL_SECONDS = BUILDER
                .comment("How often players are paid (seconds). Example: 300 = every 5 minutes.")
                .defineInRange("intervalSeconds", 300, 5, 86_400);

        PLAYTIME_DAILY_CAP = BUILDER
                .comment("Daily cap (per player) for playtime payouts. Set 0 to disable.")
                .defineInRange("dailyCap", 1_000, 0, 100_000_000);

        NOTIFY_ON_PLAYTIME_REWARD = BUILDER
                .comment("Send a system message to a player every time he earns rewards.")
                .define("playtimeRewardNotification", true);

        PLAYTIME_REWARD_MESSAGE_FORMAT = BUILDER
                .comment("""
                     Format for the notification. Uses String.format with two args:
                     %s = currency symbol, %d = credited amount (integer).
                     Examples: "You received %s%d for being active!", "+%s%d"
                     """)
                .define("messageFormat", "You received %s%d for being active!");

        BUILDER.pop();

        BUILDER.push("mobFarming");

        ENABLE_MOB_DROPS = BUILDER
                .comment("Enable currency dropping from mobs.")
                .define("enableMobDrops", true);

        ENABLE_MOB_DROPS_LIMIT = BUILDER
                .comment("Enable drop limits from mobs.")
                .define("enableMobDropsLimit", true);

        WARN_ON_MOB_DROPS_LIMIT = BUILDER
                .comment("Warn the player once he reaches the mob drop limit.")
                .define("warnOnMobDropsLimit", true);

        MOB_DROPS_LIMIT = BUILDER
                .comment("Limit of currency dropped from mobs a day.")
                .defineInRange("mobDropsLimit", 1000, 0, 1_000_000);

        DROP_ZOMBIE_1 = BUILDER
                .comment("Percentage chance for $1 bill from zombies.")
                .defineInRange("zombie1DollarChance", 2.0, 0.0, 100.0);

        DROP_SPIDER_1 = BUILDER
                .comment("Percentage chance for $1 bill from spiders.")
                .defineInRange("spider1DollarChance", 2.0, 0.0, 100.0);

        DROP_CREEPER_1 = BUILDER
                .comment("Percentage chance for $1 bill from creepers.")
                .defineInRange("creeper1DollarChance", 2.0, 0.0, 100.0);

        DROP_SKELETON_1 = BUILDER
                .comment("Percentage chance for $1 bill from skeletons.")
                .defineInRange("skeleton1DollarChance", 2.0, 0.0, 100.0);

        DROP_WITHER_SKELETON_1 = BUILDER
                .comment("Percentage chance for $1 bill from wither skeletons.")
                .defineInRange("witherSkeleton1DollarChance", 2.0, 0.0, 100.0);

        DROP_BLAZE_1 = BUILDER
                .comment("Percentage chance for $1 bill from blazes.")
                .defineInRange("blaze1DollarChance", 2.0, 0.0, 100.0);

        RARE_FIVE_DOLLAR_CHANCE = BUILDER
                .comment("Rare chance to drop a $5 bills from mobs")
                .defineInRange("rareFiveDollarChance", 0.02, 0.0, 1.0);

        BUILDER.pop();

        BUILDER.push("enchantment");

        CAPITALIST_GREED_1 = BUILDER
                .comment("Percentage chance increase for Capitalist Greed I")
                .defineInRange("capitalistGreedI",5.0, 0.0, 100.0);

        CAPITALIST_GREED_2 = BUILDER
                .comment("Percentage chance increase for Capitalist Greed II")
                .defineInRange("capitalistGreedII", 8.0, 0.0, 100.0);

        CAPITALIST_GREED_3 = BUILDER
                .comment("Percentage chance increase for Capitalist Greed III")
                .defineInRange("capitalistGreedIII", 10.0, 0.0, 100.0);

        BUILDER.pop();

        BUILDER.push("lottery");

        ENABLE_LOTTERY = BUILDER
                .comment("Enable the /lottery system (server-side only).")
                .define("enableLottery", true);

        LOTTERY_MIN_BET = BUILDER
                .comment("Minimum bet amount for /lottery.")
                .defineInRange("minBet", 10, 0, 1_000_000);

        LOTTERY_COOLDOWN_MINUTES = BUILDER
                .comment("Cooldown in minutes between lottery creations.")
                .defineInRange("cooldownMinutes", 15, 0, 1440);

        BUILDER.pop();

        BUILDER.push("persistence");

        ENABLE_BACKUPS = BUILDER
                .comment("Write timestamped rolling backups when saving.")
                .define("enableBackups", false);

        MAX_BACKUP_FILES = BUILDER
                .comment("Maximum number of backup files to keep.")
                .defineInRange("maxBackupFiles", 5, 0, 1000);

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
