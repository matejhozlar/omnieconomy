package com.saunhardy.omnieconomy.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OmniEconomySavedData extends SavedData {

    private final Map<UUID, Integer> balances = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playtimeEarnedToday = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastDailyClaimEpochDay = new ConcurrentHashMap<>();

    public OmniEconomySavedData() {}

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        CompoundTag balancesTag = new CompoundTag();
        for (var e : balances.entrySet()) {
            balancesTag.putInt(e.getKey().toString(), e.getValue());
        }
        tag.put("balances", balancesTag);

        CompoundTag playtimeTag = new CompoundTag();
        for (var e : playtimeEarnedToday.entrySet()) {
            playtimeTag.putInt(e.getKey().toString(), e.getValue());
        }
        tag.put("playtimeEarnedToday", playtimeTag);

        CompoundTag dailyTag = new CompoundTag();
        for (var e : lastDailyClaimEpochDay.entrySet()) {
            dailyTag.putLong(e.getKey().toString(), e.getValue());
        }
        tag.put("lastDailyClaimEpochDay", dailyTag);

        return tag;
    }

    public static OmniEconomySavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        OmniEconomySavedData d = new OmniEconomySavedData();

        if (tag.contains("balances")) {
            CompoundTag t = tag.getCompound("balances");
            for (String key : t.getAllKeys()) {
                try {
                    d.balances.put(UUID.fromString(key), t.getInt(key));
                } catch (Exception ignored) {}
            }
        }

        if (tag.contains("playtimeEarnedToday")) {
            CompoundTag t = tag.getCompound("playtimeEarnedToday");
            for (String key : t.getAllKeys()) {
                try {
                    d.playtimeEarnedToday.put(UUID.fromString(key), t.getInt(key));
                } catch (Exception ignored) {}
            }
        }

        if (tag.contains("lastDailyClaimEpochDay")) {
            CompoundTag t = tag.getCompound("lastDailyClaimEpochDay");
            for (String key : t.getAllKeys()) {
                try {
                    d.lastDailyClaimEpochDay.put(UUID.fromString(key), t.getLong(key));
                } catch (Exception ignored) {}
            }
        }

        return d;
    }


    public int getBalance(UUID id) {
        return balances.getOrDefault(id, 0);
    }

    public void setBalance(UUID id, int value) {
        if (value == 0) balances.remove(id);
        else balances.put(id, value);
        setDirty();
    }

    public Map<UUID, Integer> getBalancesView() {
        return balances;
    }

    public int getPlaytimeEarnedToday(UUID id) {
        return playtimeEarnedToday.getOrDefault(id, 0);
    }

    public void setPlaytimeEarnedToday(UUID id, int value) {
        if (value <= 0) playtimeEarnedToday.remove(id);
        else playtimeEarnedToday.put(id, value);
        setDirty();
    }

    public long getLastDailyEpochDay(UUID id) {
        return lastDailyClaimEpochDay.getOrDefault(id, Long.MIN_VALUE);
    }

    public void setLastDailyClaimEpochDay(UUID id, long epochDay) {
        lastDailyClaimEpochDay.put(id, epochDay);
        setDirty();
    }


    public static OmniEconomySavedData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        SavedData.Factory<OmniEconomySavedData> factory =
                new SavedData.Factory<>(OmniEconomySavedData::new, OmniEconomySavedData::load);
        return overworld.getDataStorage().computeIfAbsent(factory, "omnieconomy");
    }
}
