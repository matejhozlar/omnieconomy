package com.saunhardy.omnieconomy;

import com.saunhardy.omnieconomy.util.BackupUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.nio.file.Path;

public class BackupHooks {

    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save event) {
        if (!Config.ENABLE_BACKUPS.get()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(Level.OVERWORLD)) return;

        int max = Config.MAX_BACKUP_FILES.get();

        Path dataDir = level.getServer().getWorldPath(LevelResource.ROOT).resolve("data");

        Path savedDataFile = dataDir.resolve("omnieconomy.dat");

        Path backupsRoot = dataDir.resolve("backups");

        BackupUtils.writeRollingBackup(savedDataFile, backupsRoot, "omnieconomy", max);
    }
}
