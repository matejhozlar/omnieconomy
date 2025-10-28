package com.saunhardy.omnieconomy.command;

import com.saunhardy.omnieconomy.Config;
import com.saunhardy.omnieconomy.telemetry.TelemetryManager;
import com.saunhardy.omnieconomy.telemetry.ServerIdentifier;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class TelemetryCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("omni")
                        .then(Commands.literal("telemetry")
                                .then(Commands.literal("status")
                                        .requires(src -> src.hasPermission(2))
                                        .executes(ctx -> {
                                            boolean enabled = Config.ENABLE_TELEMETRY.get();
                                            var server = ctx.getSource().getServer();
                                            var serverId = ServerIdentifier.getOrCreateServerId(server);

                                            ctx.getSource().sendSuccess(() ->
                                                    Component.literal("Telemetry Status").withStyle(ChatFormatting.GOLD), false);
                                            ctx.getSource().sendSuccess(() ->
                                                    Component.literal("Enabled: " + (enabled ? "Yes" : "No"))
                                                            .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED), false);
                                            ctx.getSource().sendSuccess(() ->
                                                    Component.literal("Server ID: " + serverId).withStyle(ChatFormatting.GRAY), false);
                                            ctx.getSource().sendSuccess(() ->
                                                    Component.literal("Interval: " + 1 + " hours")
                                                            .withStyle(ChatFormatting.GRAY), false);

                                            return 1;
                                        })
                                )
                                .then(Commands.literal("heartbeat")
                                        .requires(src -> src.hasPermission(2))
                                        .executes(ctx -> {
                                            if (!Config.ENABLE_TELEMETRY.get()) {
                                                ctx.getSource().sendFailure(
                                                        Component.literal("Telemetry is disabled in config"));
                                                return 0;
                                            }

                                            ctx.getSource().sendSuccess(() ->
                                                    Component.literal("Sending heartbeat...").withStyle(ChatFormatting.YELLOW), false);

                                            TelemetryManager.forceHeartbeat(ctx.getSource().getServer());

                                            return 1;
                                        })
                                )
                        )
        );
    }
}