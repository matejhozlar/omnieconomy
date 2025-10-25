package com.saunhardy.omnieconomy.integration;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.UUID;

public final class AFKIntegration {
    private static final Logger LOG = LoggerFactory.getLogger("OmniEconomy-AFK");

    private static final boolean HAS_AFK_MOD;
    private static final Method AFK_IS_METHOD;

    static {
        boolean present = ModList.get().isLoaded("afkstatus");
        HAS_AFK_MOD = present;
        Method m = null;
        if (present) {
            try {
                Class<?> cls = Class.forName("com.saunhardy.afkstatus.AFKManager");
                m = cls.getMethod("isAFK", UUID.class);
                LOG.info("AFKStatus detected; integrating with playtime rewards.");
            } catch (Throwable t) {
                LOG.warn("AFKStatus present but API lookup failed; falling back to scoreboard heuristics.");
            }
        } else {
            LOG.info("AFKStatus not present; AFK detection via scoreboard only (if enabled).");
        }
        AFK_IS_METHOD = m;
    }

    private AFKIntegration() {}

    public static boolean isAfk(ServerPlayer player) {
        if (HAS_AFK_MOD && AFK_IS_METHOD != null) {
            try {
                return (boolean) AFK_IS_METHOD.invoke(null, player.getUUID());
            } catch (Throwable ignored) {}
        }
        return isAfkByScoreboard(player);
    }

    private static boolean isAfkByScoreboard(ServerPlayer player) {
        Scoreboard sb = player.getScoreboard();

        PlayerTeam team = sb.getPlayersTeam(player.getScoreboardName());
        if (team != null && "afk".equalsIgnoreCase(team.getName())) return true;

        Objective obj = sb.getObjective("afk");
        if (obj != null) {
            ScoreAccess sc = sb.getOrCreatePlayerScore(player, obj);
            return sc.get() > 0;
        }
        return false;
    }
}
