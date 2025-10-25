package com.saunhardy.omnieconomy.lottery;

import com.saunhardy.omnieconomy.Config;
import com.saunhardy.omnieconomy.data.OmniEconomySavedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.time.Instant;
import java.util.*;

public class LotteryManager {

    public static class Entry {
        public final UUID uuid;
        public final String name;
        public int amount;
        public Entry(UUID uuid, String name, int amount) {
            this.uuid = uuid; this.name = name; this.amount = amount;
        }
    }

    private final RandomSource rng = RandomSource.create();

    private boolean active = false;
    private String hostName = "";
    private UUID hostUuid = null;
    private long endsAtMs = 0L;
    private final Map<UUID, Entry> participants = new LinkedHashMap<>();

    private long lastCreateAtMin = -1;

    private final java.util.function.Supplier<Boolean> enableLottery;
    private final java.util.function.Supplier<Integer> minBet;
    private final java.util.function.Supplier<Integer> cooldownMinutes;
    private final java.util.function.Supplier<Integer> durationSeconds;   // NEW

    public LotteryManager(java.util.function.Supplier<Boolean> enableLottery,
                          java.util.function.Supplier<Integer> minBet,
                          java.util.function.Supplier<Integer> cooldownMinutes,
                          java.util.function.Supplier<Integer> durationSeconds) {
        this.enableLottery = enableLottery;
        this.minBet = minBet;
        this.cooldownMinutes = cooldownMinutes;
        this.durationSeconds = durationSeconds;
    }

    public boolean isActive() { return active; }
    public long getMillisRemaining() { return Math.max(0, endsAtMs - System.currentTimeMillis()); }
    public String getHostName() { return hostName; }
    public Collection<Entry> getParticipants() { return participants.values(); }

    public void create(MinecraftServer server, ServerPlayer host) {
        if (!enableLottery.get()) throw new IllegalStateException("Lottery is disabled.");
        if (active) throw new IllegalStateException("A lottery is already running.");

        int cd = Math.max(0, cooldownMinutes.get());
        long nowMin = nowEpochMinutes();

        if (cd > 0 && lastCreateAtMin >= 0) {
            long elapsed = Math.max(0, nowMin - lastCreateAtMin);
            if (elapsed < cd) {
                long left = cd - elapsed;
                throw new IllegalStateException("A recent lottery was created. Try again in " + left + " minute(s).");
            }
        }

        int durSec = Math.max(10, durationSeconds.get());

        this.active = true;
        this.hostName = host.getGameProfile().getName();
        this.hostUuid = host.getUUID();
        this.endsAtMs = System.currentTimeMillis() + (long) durSec * 1000L;
        this.participants.clear();

        broadcast(server, "§6🎲 Lottery started by §e" + hostName + "§6!\nType §e/lottery join <amount>§6 to enter.\nEnds in §e" + durSec + "s§6.");

        lastCreateAtMin = nowMin;
    }

    public void join(MinecraftServer server, ServerPlayer player, int amount) {
        if (!active) throw new IllegalStateException("No active lottery.");
        if (amount < Math.max(0, minBet.get())) throw new IllegalArgumentException("Minimum bet is " + minBet.get() + ".");
        if (getMillisRemaining() <= 0) throw new IllegalStateException("Lottery is closing; wait for the next one.");

        OmniEconomySavedData data = OmniEconomySavedData.get(server);
        UUID id = player.getUUID();
        int bal = data.getBalance(id);
        if (bal < amount) throw new IllegalStateException("You don't have enough funds. Balance: " + bal);

        data.setBalance(id, bal - amount);

        var e = participants.get(id);
        if (e == null) e = new Entry(id, player.getGameProfile().getName(), amount);
        else e.amount += amount;
        participants.put(id, e);

        if (Config.LOTTERY_ANNOUNCE_JOIN.get()) {
            broadcast(server, "§a" + e.name + " joined with §e" + Config.CURRENCY_SYMBOL.get() + amount + "§a (their total: §e" + Config.CURRENCY_SYMBOL.get() + e.amount + "§a).");
        }
    }

    public void tick(MinecraftServer server) {
        if (!active) return;
        if (System.currentTimeMillis() < endsAtMs) return;
        resolve(server);
    }

    public void resolve(MinecraftServer server) {
        if (!active) return;
        try {
            var list = new ArrayList<>(participants.values());
            if (list.isEmpty()) { broadcast(server, "§7Lottery ended with no participants."); return; }
            if (list.size() == 1) {
                var only = list.getFirst();
                var data = OmniEconomySavedData.get(server);
                data.setBalance(only.uuid, data.getBalance(only.uuid) + only.amount);
                broadcast(server, "§cNot enough participants. Refunded §e" + only.name + " §c" + Config.CURRENCY_SYMBOL.get() + only.amount + ".");
                return;
            }
            int total = list.stream().mapToInt(e -> e.amount).sum();
            double r = rng.nextDouble() * total;
            int cum = 0;
            Entry winner = null;
            for (Entry e : list) { cum += e.amount; if (r <= cum) { winner = e; break; } }
            if (winner == null) winner = list.getLast();

            var data = OmniEconomySavedData.get(server);
            data.setBalance(winner.uuid, data.getBalance(winner.uuid) + total);
                broadcast(server, "§6🏆 Winner: §e" + winner.name + " §6wins §e" + Config.CURRENCY_SYMBOL.get() + total + "§6! GG!");
        } finally {
            active = false;
            hostName = "";
            hostUuid = null;
            endsAtMs = 0L;
            participants.clear();
        }
    }

    private static long nowEpochMinutes() {
        return Instant.now().getEpochSecond() / 60L;
    }

    private void broadcast(MinecraftServer server, String msg) {
        server.getPlayerList().broadcastSystemMessage(net.minecraft.network.chat.Component.literal(msg), false);
    }
}
