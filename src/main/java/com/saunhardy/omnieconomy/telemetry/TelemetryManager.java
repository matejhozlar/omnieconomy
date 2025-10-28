package com.saunhardy.omnieconomy.telemetry;

import com.saunhardy.omnieconomy.Config;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TelemetryManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("OmniEconomy-Telemerty");
    private static boolean hasRegistered = false;
    private static long lastHeartbeatMs = 0;
    private static UUID serverId = null;
    private static boolean serverRunning = false;

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        if (!Config.ENABLE_TELEMETRY.get()) {
            return;
        }

        serverRunning = true;
        MinecraftServer server = event.getServer();

        serverId = ServerIdentifier.getOrCreateServerId(server);

        if (!hasRegistered) {
            String modVersion = getModVersion();
            String mcVersion = server.getServerVersion();

            LOGGER.info("Sending anonymous telemetry registration...");
            LOGGER.info("Server ID: {} | Mod Version: {} | MC Version: {}", serverId, modVersion, mcVersion);
            LOGGER.info("To opt-out, set 'enableTelemetry = false' in omnieconomy-common.toml");

            TelemetryClient.sendRegistration(serverId, modVersion, mcVersion)
                    .thenAccept(success -> {
                        if (success) {
                            hasRegistered = true;
                            lastHeartbeatMs = System.currentTimeMillis();
                        }
                    });
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        serverRunning = false;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!serverRunning || !Config.ENABLE_TELEMETRY.get()) {
            return;
        }

        if (event.getServer().getTickCount() % 20 != 0) {
            return;
        }

        long now = System.currentTimeMillis();
        long intervalMs = TimeUnit.HOURS.toMillis(1);

        if (now - lastHeartbeatMs >= intervalMs) {
            sendHeartbeat(event.getServer());
        }
    }

    private static void sendHeartbeat(MinecraftServer server) {
        if (serverId == null) {
            serverId = ServerIdentifier.getOrCreateServerId(server);
        }

        String modVersion = getModVersion();

        TelemetryClient.sendHeartbeat(serverId, modVersion)
                .thenAccept(success -> {
                    if (success) {
                        lastHeartbeatMs = System.currentTimeMillis();
                    }
                });
    }

    private static String getModVersion() {
        try {
            return ModList.get()
                    .getModContainerById("omnieconomy")
                    .map(container -> container.getModInfo().getVersion().toString())
                    .orElse("unknown");
        } catch (Exception exception) {
            return "unknown";
        }
    }

    public static void forceHeartbeat(MinecraftServer server) {
        if (!Config.ENABLE_TELEMETRY.get()) {
            return;
        }
        sendHeartbeat(server);
    }
}