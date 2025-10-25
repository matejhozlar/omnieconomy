package com.saunhardy.omnieconomy.reward;

import com.saunhardy.omnieconomy.Config;
import com.saunhardy.omnieconomy.core.Economy;
import com.saunhardy.omnieconomy.integration.AFKIntegration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlaytimeRewardsManager {
    private static final Map<UUID, Long> lastCheckMs = new ConcurrentHashMap<>();
    private static volatile long lastResetEpochDay = Long.MIN_VALUE;

    private PlaytimeRewardsManager() {}

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        lastCheckMs.put(event.getEntity().getUUID(), System.currentTimeMillis());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        lastCheckMs.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        var server = event.getServer();

        if (!Config.ENABLE_PLAYTIME_REWARDS.get()) return;

        maybeResetDaily(server);

        final long nowMs = System.currentTimeMillis();
        final int amountPerInterval = Config.PLAYTIME_REWARD_AMOUNT.get();
        final long intervalMs = Math.max(1, (long) Config.PLAYTIME_REWARD_INTERVAL_SECONDS.get()) * 1000L;

        server.getPlayerList().getPlayers().forEach(player -> {
            var id = player.getUUID();

            if (Config.PLAYTIME_AFK_INTEGRATION.get() && AFKIntegration.isAfk(player)) {
                lastCheckMs.put(id, nowMs);
                return;
            }

            long last = lastCheckMs.getOrDefault(id, nowMs);
            long elapsed = nowMs - last;

            if (elapsed < intervalMs) {
                lastCheckMs.putIfAbsent(id, nowMs);
                return;
            }

            long intervals = elapsed / intervalMs;
            if (intervals <= 0) return;

            int proposedCredit = Math.toIntExact(intervals * (long) amountPerInterval);
            int credited;
            if (proposedCredit > 0) {
                credited = Economy.addPlaytimeReward(server, id, proposedCredit);

                if (credited > 0 && Config.NOTIFY_ON_PLAYTIME_REWARD.get()) {
                    String fmt = Config.PLAYTIME_REWARD_MESSAGE_FORMAT.get();
                    String text;
                    try {
                        text = String.format(fmt, Config.CURRENCY_SYMBOL.get(), credited);
                    } catch (IllegalFormatException ex) {
                        text = "You received " + Config.CURRENCY_SYMBOL.get() + credited + " for being active!";
                    }
                    player.sendSystemMessage(Component.literal(text).withStyle(ChatFormatting.GOLD));
                }
            }

            long advancedMs = intervals * intervalMs;
            lastCheckMs.put(id, last + advancedMs);
        });
    }

    private static void maybeResetDaily(MinecraftServer server) {
        long today = LocalDate.now(ZoneOffset.UTC).toEpochDay();
        if (today == lastResetEpochDay) return;
        lastResetEpochDay = today;
        server.getPlayerList().getPlayers().forEach(player -> Economy.resetPlaytimeEarnedToday(server, player.getUUID()));
    }

}