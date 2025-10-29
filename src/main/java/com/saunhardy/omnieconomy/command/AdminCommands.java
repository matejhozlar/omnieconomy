package com.saunhardy.omnieconomy.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.saunhardy.omnieconomy.Config;
import com.saunhardy.omnieconomy.core.Economy;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.text.NumberFormat;

public class AdminCommands {

    private static Component msg(String prefix, String text, ChatFormatting color) {
        return Component.literal(prefix + " " + text).withStyle(color);
    }

    private static String fmt(int amount) {
        return NumberFormat.getInstance().format(amount);
    }

    /**
     * Creates a clickable command component
     * @param command The command to display and suggest
     * @param description The description to show
     * @param color The color for the command text
     */
    private static Component createClickableCommand(String command, String description, ChatFormatting color) {
        return Component.literal("  " + command)
                .withStyle(Style.EMPTY
                        .withColor(color)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.literal("Click to insert command\n")
                                        .withStyle(ChatFormatting.GREEN)
                                        .append(Component.literal(description).withStyle(ChatFormatting.GRAY))
                        ))
                )
                .append(Component.literal(" - " + description).withStyle(ChatFormatting.GRAY));
    }

    private static void sendHelpMessage(CommandSourceStack source, boolean isAdmin) {
        source.sendSuccess(() ->
                        Component.literal("═══════════════════════════════════════")
                                .withStyle(ChatFormatting.GOLD),
                false
        );
        source.sendSuccess(() ->
                        Component.literal("         OmniEconomy Commands")
                                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD),
                false
        );
        source.sendSuccess(() ->
                        Component.literal("═══════════════════════════════════════")
                                .withStyle(ChatFormatting.GOLD),
                false
        );

        source.sendSuccess(() ->
                        Component.literal("\n▶ Player Commands:").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                false
        );

        if (Config.ENABLE_MONEY_COMMAND.get()) {
            source.sendSuccess(() ->
                            createClickableCommand("/money", "Check your balance", ChatFormatting.AQUA),
                    false
            );
        }

        if (Config.ENABLE_PAY_COMMAND.get()) {
            source.sendSuccess(() ->
                            createClickableCommand("/pay <player> <amount>", "Send money to another player", ChatFormatting.AQUA),
                    false
            );
        }

        if (Config.ENABLE_BALTOP_COMMAND.get()) {
            source.sendSuccess(() ->
                            createClickableCommand("/baltop", "View the richest players", ChatFormatting.AQUA),
                    false
            );
        }

        if (Config.ENABLE_DAILY_REWARDS.get()) {
            source.sendSuccess(() ->
                            createClickableCommand("/daily", "Claim your daily reward", ChatFormatting.AQUA),
                    false
            );
        }

        if (Config.ENABLE_DEPOSIT_COMMAND.get()) {
            source.sendSuccess(() ->
                            createClickableCommand("/deposit", "Deposit all bills from inventory", ChatFormatting.AQUA),
                    false
            );
        }

        if (Config.ENABLE_WITHDRAW_COMMAND.get()) {
            source.sendSuccess(() ->
                            createClickableCommand("/withdraw <amount>", "Withdraw money as bills", ChatFormatting.AQUA),
                    false
            );
            source.sendSuccess(() ->
                            createClickableCommand("/withdraw <denom> <count>", "Withdraw specific bills", ChatFormatting.AQUA),
                    false
            );
            source.sendSuccess(() ->
                            createClickableCommand("/withdraw <denom1>:<count1> <denom2>:<count2>", "Withdraw bundle of bills", ChatFormatting.AQUA),
                    false
            );
        }

        if (Config.ENABLE_LOTTERY.get()) {
            source.sendSuccess(() ->
                            Component.literal("\n▶ Lottery Commands:").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD),
                    false
            );

            if (isAdmin) {
                source.sendSuccess(() ->
                                createClickableCommand("/lottery start", "Start a lottery (OP only)", ChatFormatting.AQUA),
                        false
                );
            }

            source.sendSuccess(() ->
                            createClickableCommand("/lottery join <amount>", "Join active lottery", ChatFormatting.AQUA),
                    false
            );
            source.sendSuccess(() ->
                            createClickableCommand("/lottery status", "Check lottery status", ChatFormatting.AQUA),
                    false
            );
        }

        if (isAdmin) {
            source.sendSuccess(() ->
                            Component.literal("\n▶ Admin Commands:").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                    false
            );

            source.sendSuccess(() ->
                            createClickableCommand("/omniecon admin give <player> <amount>", "Give money to a player", ChatFormatting.YELLOW),
                    false
            );

            source.sendSuccess(() ->
                            createClickableCommand("/omniecon admin take <player> <amount>", "Take money from a player", ChatFormatting.YELLOW),
                    false
            );

            source.sendSuccess(() ->
                            createClickableCommand("/omniecon admin set <player> <amount>", "Set a player's balance", ChatFormatting.YELLOW),
                    false
            );

            source.sendSuccess(() ->
                            createClickableCommand("/omniecon admin balance <player>", "Check a player's balance", ChatFormatting.YELLOW),
                    false
            );

            source.sendSuccess(() ->
                            createClickableCommand("/omniecon admin resetdaily <player>", "Reset daily reward cooldown", ChatFormatting.YELLOW),
                    false
            );

            source.sendSuccess(() ->
                            createClickableCommand("/omniecon admin resetplaytime <player>", "Reset playtime earnings cap", ChatFormatting.YELLOW),
                    false
            );

            if (Config.ENABLE_TELEMETRY.get()) {
                source.sendSuccess(() ->
                                Component.literal("\n▶ Telemetry Commands:").withStyle(ChatFormatting.BLUE, ChatFormatting.BOLD),
                        false
                );

                source.sendSuccess(() ->
                                createClickableCommand("/omni telemetry status", "View telemetry status", ChatFormatting.YELLOW),
                        false
                );

                source.sendSuccess(() ->
                                createClickableCommand("/omni telemetry heartbeat", "Send manual heartbeat", ChatFormatting.YELLOW),
                        false
                );
            }
        }

        source.sendSuccess(() ->
                        Component.literal("═══════════════════════════════════════")
                                .withStyle(ChatFormatting.GOLD),
                false
        );

        source.sendSuccess(() ->
                        Component.literal("Currency Symbol: ")
                                .withStyle(ChatFormatting.GRAY)
                                .append(Component.literal(Config.CURRENCY_SYMBOL.get()).withStyle(ChatFormatting.GREEN)),
                false
        );

        if (!isAdmin) {
            source.sendSuccess(() ->
                            Component.literal("💡 Tip: ")
                                    .withStyle(ChatFormatting.YELLOW)
                                    .append(Component.literal("Admins see additional commands").withStyle(ChatFormatting.GRAY)),
                    false
            );
        }

        source.sendSuccess(() ->
                        Component.literal("✨ Tip: ")
                                .withStyle(ChatFormatting.AQUA)
                                .append(Component.literal("Click any command to insert it into chat!").withStyle(ChatFormatting.GRAY)),
                false
        );

        source.sendSuccess(() ->
                        Component.literal("═══════════════════════════════════════")
                                .withStyle(ChatFormatting.GOLD),
                false
        );
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        var d = event.getDispatcher();

        d.register(
                Commands.literal("omniecon")
                        .then(Commands.literal("help")
                                .executes(ctx -> {
                                    boolean isAdmin = ctx.getSource().hasPermission(4);
                                    sendHelpMessage(ctx.getSource(), isAdmin);
                                    return 1;
                                })
                        )

                        // Admin commands
                        .then(Commands.literal("admin")
                                .requires(src -> src.hasPermission(4))

                                .then(Commands.literal("give")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> {
                                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                                            int newBalance = Economy.deposit(
                                                                    ctx.getSource().getServer(),
                                                                    target.getUUID(),
                                                                    amount
                                                            );

                                                            ctx.getSource().sendSuccess(() ->
                                                                            msg("✅", "Gave " + Config.CURRENCY_SYMBOL.get() + fmt(amount) +
                                                                                            " to " + target.getName().getString() +
                                                                                            ". New balance: " + Config.CURRENCY_SYMBOL.get() + fmt(newBalance),
                                                                                    ChatFormatting.GREEN),
                                                                    true
                                                            );

                                                            target.sendSystemMessage(
                                                                    msg("💰", "You received " + Config.CURRENCY_SYMBOL.get() + fmt(amount) +
                                                                            " from an administrator.", ChatFormatting.GOLD)
                                                            );

                                                            return 1;
                                                        })
                                                )
                                        )
                                )

                                .then(Commands.literal("take")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> {
                                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                                            boolean success = Economy.withdraw(
                                                                    ctx.getSource().getServer(),
                                                                    target.getUUID(),
                                                                    amount
                                                            );

                                                            if (success) {
                                                                int newBalance = Economy.getBalance(
                                                                        ctx.getSource().getServer(),
                                                                        target.getUUID()
                                                                );

                                                                ctx.getSource().sendSuccess(() ->
                                                                                msg("✅", "Took " + Config.CURRENCY_SYMBOL.get() + fmt(amount) +
                                                                                                " from " + target.getName().getString() +
                                                                                                ". New balance: " + Config.CURRENCY_SYMBOL.get() + fmt(newBalance),
                                                                                        ChatFormatting.GREEN),
                                                                        true
                                                                );

                                                                target.sendSystemMessage(
                                                                        msg("⚠", Config.CURRENCY_SYMBOL.get() + fmt(amount) +
                                                                                        " was removed from your account by an administrator.",
                                                                                ChatFormatting.YELLOW)
                                                                );
                                                            } else {
                                                                ctx.getSource().sendFailure(
                                                                        msg("[ERROR]", "Player doesn't have enough funds.", ChatFormatting.RED)
                                                                );
                                                            }

                                                            return success ? 1 : 0;
                                                        })
                                                )
                                        )
                                )

                                .then(Commands.literal("set")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                        .executes(ctx -> {
                                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                                            int newBalance = Economy.setBalance(
                                                                    ctx.getSource().getServer(),
                                                                    target.getUUID(),
                                                                    amount
                                                            );

                                                            ctx.getSource().sendSuccess(() ->
                                                                            msg("✅", "Set " + target.getName().getString() + "'s balance to " +
                                                                                            Config.CURRENCY_SYMBOL.get() + fmt(newBalance),
                                                                                    ChatFormatting.GREEN),
                                                                    true
                                                            );

                                                            target.sendSystemMessage(
                                                                    msg("💰", "Your balance has been set to " +
                                                                            Config.CURRENCY_SYMBOL.get() + fmt(newBalance) +
                                                                            " by an administrator.", ChatFormatting.GOLD)
                                                            );

                                                            return 1;
                                                        })
                                                )
                                        )
                                )

                                .then(Commands.literal("balance")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                    int balance = Economy.getBalance(
                                                            ctx.getSource().getServer(),
                                                            target.getUUID()
                                                    );

                                                    ctx.getSource().sendSuccess(() ->
                                                                    msg("💰", target.getName().getString() + "'s balance: " +
                                                                                    Config.CURRENCY_SYMBOL.get() + fmt(balance),
                                                                            ChatFormatting.GREEN),
                                                            false
                                                    );

                                                    return 1;
                                                })
                                        )
                                )

                                .then(Commands.literal("resetdaily")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");

                                                    var data = com.saunhardy.omnieconomy.data.OmniEconomySavedData.get(
                                                            ctx.getSource().getServer()
                                                    );
                                                    data.setLastDailyClaimEpochDay(target.getUUID(), 0);

                                                    ctx.getSource().sendSuccess(() ->
                                                                    msg("✅", "Reset daily reward for " + target.getName().getString(),
                                                                            ChatFormatting.GREEN),
                                                            true
                                                    );

                                                    target.sendSystemMessage(
                                                            msg("🔄", "Your daily reward has been reset by an administrator.",
                                                                    ChatFormatting.GOLD)
                                                    );

                                                    return 1;
                                                })
                                        )
                                )

                                .then(Commands.literal("resetplaytime")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");

                                                    Economy.resetPlaytimeEarnedToday(
                                                            ctx.getSource().getServer(),
                                                            target.getUUID()
                                                    );

                                                    ctx.getSource().sendSuccess(() ->
                                                                    msg("✅", "Reset playtime earnings for " + target.getName().getString(),
                                                                            ChatFormatting.GREEN),
                                                            true
                                                    );

                                                    target.sendSystemMessage(
                                                            msg("🔄", "Your playtime earnings cap has been reset by an administrator.",
                                                                    ChatFormatting.GOLD)
                                                    );

                                                    return 1;
                                                })
                                        )
                                )
                        )
        );
    }
}