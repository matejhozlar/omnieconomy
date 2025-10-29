package com.saunhardy.omnieconomy.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.saunhardy.omnieconomy.Config;
import com.saunhardy.omnieconomy.core.Economy;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
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

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        var d = event.getDispatcher();

        d.register(
                Commands.literal("omniecon")
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