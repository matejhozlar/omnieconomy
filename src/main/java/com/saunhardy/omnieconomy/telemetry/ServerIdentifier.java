package com.saunhardy.omnieconomy.telemetry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ServerIdentifier {
    private static final String FILE_NAME = "server_id.dat";
    private static final String DATA_FOLDER = "omnieconomy";

    private static UUID cachedServerId = null;

    public static UUID getOrCreateServerId(MinecraftServer server) {
        if (cachedServerId != null) {
            return cachedServerId;
        }

        Path worldPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
        Path dataPath = worldPath.resolve("data").resolve(DATA_FOLDER);

        try {
            Files.createDirectories(dataPath);
        } catch (IOException ignored) {}

        Path idFile = dataPath.resolve(FILE_NAME);

        if (Files.exists(idFile)) {
            try {
                CompoundTag tag = NbtIo.readCompressed(idFile, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                if (tag.contains("serverId")) {
                    String idString = tag.getString("serverId");
                    cachedServerId = UUID.fromString(idString);
                    return cachedServerId;
                }
            } catch (Exception ignored) {
            }
        }

        cachedServerId = UUID.randomUUID();

        try {
            CompoundTag tag = new CompoundTag();
            tag.putString("serverId", cachedServerId.toString());
            tag.putLong("createdAt", System.currentTimeMillis());
            NbtIo.writeCompressed(tag, idFile);
        } catch (IOException ignored) {}

        return cachedServerId;
    }
}