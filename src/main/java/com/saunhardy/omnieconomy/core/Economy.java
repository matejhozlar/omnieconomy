package com.saunhardy.omnieconomy.core;

import com.saunhardy.omnieconomy.Config;
import com.saunhardy.omnieconomy.data.OmniEconomySavedData;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class Economy {
    private Economy() {}

    public static int getBalance(MinecraftServer server, UUID playerId) {
        return OmniEconomySavedData.get(server).getBalance(playerId);
    }

    public static int setBalance(MinecraftServer server, UUID playerId, int newBalance) {
        int clamped = clampBalance(newBalance);
        OmniEconomySavedData.get(server).setBalance(playerId, clamped);
        return clamped;
    }

    public static int deposit(MinecraftServer server, UUID playerId, int amount) {
        if (amount <= 0) return getBalance(server, playerId);
        OmniEconomySavedData data = OmniEconomySavedData.get(server);
        int cur = data.getBalance(playerId);
        int next = clampBalance(cur + amount);
        data.setBalance(playerId, next);
        return next;
    }

    public static boolean withdraw(MinecraftServer server, UUID playerId, int amount) {
        if (amount <= 0) return true;
        OmniEconomySavedData data = OmniEconomySavedData.get(server);
        int cur = data.getBalance(playerId);
        int next = cur - amount;

        if (next < 0) {
            return false;
        }
        next = clampBalance(next);
        data.setBalance(playerId, next);
        return true;
    }

    public enum PayResult {
        SUCCESS,
        INSUFFICIENT_FUNDS,
        RECEIVER_CLAMPED,
        SELF_PAYMENT,
        INVALID_AMOUNT
    }

    public static PayResult pay(MinecraftServer server, UUID from, UUID to, int amount) {
        if (amount <= 0) return PayResult.INVALID_AMOUNT;
        if (from.equals(to)) return PayResult.SELF_PAYMENT;

        OmniEconomySavedData data = OmniEconomySavedData.get(server);

        int fromBal = data.getBalance(from);
        int toBal = data.getBalance(to);

        if (fromBal < amount) {
            return PayResult.INSUFFICIENT_FUNDS;
        }

        int newFrom = clampBalance(fromBal - amount);
        int newTo = clampBalance(toBal + amount);

        if (newTo < toBal) {
            return PayResult.RECEIVER_CLAMPED;
        }

        data.setBalance(from, newFrom);
        data.setBalance(to, newTo);
        return PayResult.SUCCESS;
    }

    public static boolean canClaimDaily(MinecraftServer server, UUID playerId, long todayEpochDay) {
        long last = OmniEconomySavedData.get(server).getLastDailyEpochDay(playerId);
        return todayEpochDay > last;
    }

    public static boolean claimDaily(MinecraftServer server, UUID playerId, long todayEpochDay, int amount) {
        if (!Config.ENABLE_DAILY_REWARDS.get()) return false;
        if (amount <= 0) return false;
        if (!canClaimDaily(server, playerId, todayEpochDay)) return false;

        OmniEconomySavedData data = OmniEconomySavedData.get(server);
        data.setLastDailyClaimEpochDay(playerId, todayEpochDay);
        deposit(server, playerId, amount);
        return true;
    }

    public static int addPlaytimeReward(MinecraftServer server, UUID playerId, int proposedAmount) {
        if (proposedAmount <= 0) return 0;
        int dailyCap = Config.PLAYTIME_DAILY_CAP.get();
        OmniEconomySavedData data = OmniEconomySavedData.get(server);

        if (dailyCap <= 0) {
            deposit(server, playerId, proposedAmount);
            return proposedAmount;
        }

        int earned = data.getPlaytimeEarnedToday(playerId);
        if (earned >= dailyCap) return 0;

        int remaining = dailyCap - earned;
        int credit = Math.min(remaining, proposedAmount);

        if (credit > 0) {
            deposit(server, playerId, credit);
            data.setPlaytimeEarnedToday(playerId, earned + credit);
        }
        return credit;
    }

    public static void resetPlaytimeEarnedToday(MinecraftServer server, UUID playerId) {
        OmniEconomySavedData.get(server).setPlaytimeEarnedToday(playerId, 0);
    }

    public static List<Map.Entry<UUID, Integer>> getBaltop(MinecraftServer server, int topN) {
        Map<UUID, Integer> map = OmniEconomySavedData.get(server).getBalancesView();
        return map.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(Math.max(1, topN))
                .collect(Collectors.toList());
    }

    private static int clampBalance(int value) {
        int max = Config.MAX_BALANCE.get();
        return Math.min(value, max);
    }
}
