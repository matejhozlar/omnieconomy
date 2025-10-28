package com.saunhardy.omnieconomy.telemetry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ServerIdentifier {
    private static final Logger LOGGER = LoggerFactory.getLogger("OmniEconomy-Telemetry");
    private static final String FILE_NAME = "omnieconomy_server_id.dat";

    private static UUID cachedServerId = null;

    public static UUID getOrCreateServerId(MinecraftServer server) {
        if (cachedServerId != null) {
            return cachedServerId;
        }

        Path worldPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
        Path idFile = worldPath.resolve(FILE_NAME);

        if (Files.exists(idFile)) {
            try {
                CompoundTag tag = NbtIo.readCompressed(idFile, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                if (tag.contains("serverId")) {
                    String idString = tag.getString("serverId");
                    cachedServerId = UUID.fromString(idString);
                    LOGGER.debug("Loaded server ID: {}", cachedServerId);
                    return cachedServerId;
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to load server ID, generating new one", e);
            }
        }

        cachedServerId = UUID.randomUUID();
        LOGGER.info("Generated new server ID: {}", cachedServerId);

        try {
            CompoundTag tag = new CompoundTag();
            tag.putString("serverId", cachedServerId.toString());
            tag.putLong("createdAt", System.currentTimeMillis());
            NbtIo.writeCompressed(tag, idFile);
        } catch (IOException e) {
            LOGGER.error("Failed to save server ID", e);
        }

        return cachedServerId;
    }

    public static void clearCache() {
        cachedServerId = null;
    }
}