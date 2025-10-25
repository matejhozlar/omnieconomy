package com.saunhardy.omnieconomy.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.saunhardy.omnieconomy.Config;
import com.saunhardy.omnieconomy.lottery.LotteryManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class LotteryCommands {
    public static void register(CommandDispatcher<CommandSourceStack> d, LotteryManager mgr) {
        if (!Config.ENABLE_LOTTERY.get()) return;
        d.register(
                Commands.literal("lottery")
                        .then(Commands.literal("start")
                                .requires(src -> src.hasPermission(2))
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    try {
                                        mgr.create(ctx.getSource().getServer(), p);
                                        ctx.getSource().sendSuccess(() -> text("§aLottery started."), false);
                                        return 1;
                                    } catch (Exception ex) {
                                        ctx.getSource().sendFailure(text("§c" + ex.getMessage()));
                                        return 0;
                                    }
                                })
                        )
                        .then(Commands.literal("join")
                                .then(Commands.argument("amount",
                                                IntegerArgumentType.integer(1, 1_000_000_000))
                                        .executes(ctx -> {
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                                            try {
                                                mgr.join(ctx.getSource().getServer(), p, amount);
                                                ctx.getSource().sendSuccess(() -> text("§aJoined with §e" + Config.CURRENCY_SYMBOL.get() + amount), false);
                                                return 1;
                                            } catch (Exception ex) {
                                                ctx.getSource().sendFailure(text("§c" + ex.getMessage()));
                                                return 0;
                                            }
                                        })
                                )
                        )
                        .then(Commands.literal("status")
                                .executes(ctx -> {
                                    if (!mgr.isActive()) {
                                        ctx.getSource().sendSuccess(() -> text("§7No active lottery."), false);
                                        return 1;
                                    }
                                    long ms = mgr.getMillisRemaining();
                                    int pot = mgr.getParticipants().stream().mapToInt(e -> e.amount).sum();
                                    ctx.getSource().sendSuccess(() -> text(
                                            "§6Host: §e" + mgr.getHostName()
                                                    + " §6| Time left: §e" + (ms / 1000) + "s"
                                                    + " §6| Pot: §e" + Config.CURRENCY_SYMBOL.get() + pot
                                    ), false);
                                    return 1;
                                })
                        )
        );
    }

    private static net.minecraft.network.chat.Component text(String s) {
        return net.minecraft.network.chat.Component.literal(s);
    }
}
