package com.saunhardy.omnieconomy.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

public final class BackupUtils {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private BackupUtils() {}

    public static void writeRollingBackup(Path sourceFile, Path backupsRoot, String prefix, int maxToKeep) {
        if (sourceFile == null || !Files.exists(sourceFile)) return;
        try {
            Files.createDirectories(backupsRoot.resolve(prefix));
            String ext = getExtension(sourceFile.getFileName().toString());
            String ts = LocalDateTime.now().format(TS);

            Path dst = backupsRoot.resolve(prefix).resolve(prefix + "-" + ts + ext);
            Files.copy(sourceFile, dst, StandardCopyOption.REPLACE_EXISTING);

            prune(backupsRoot.resolve(prefix), prefix, ext, maxToKeep);
        } catch (IOException ignored) {}
    }

    private static void prune(Path dir, String prefix, String ext, int maxToKeep) throws IOException {
        if (maxToKeep <= 0) {
            try (Stream<Path> s = Files.list(dir).filter(p -> nameMatches(p, prefix, ext))) {
                s.forEach(BackupUtils::safeDelete);
            }
            return;
        }
        try (Stream<Path> s = Files.list(dir).filter(p -> nameMatches(p, prefix, ext))) {
            var sorted = s.sorted(Comparator.comparing(BackupUtils::modifiedTimeSafe).reversed()).toList();
            for (int i = maxToKeep; i < sorted.size(); i++) safeDelete(sorted.get(i));
        }
    }

    private static boolean nameMatches(Path p, String prefix, String ext) {
        String n = p.getFileName().toString();
        return n.startsWith(prefix + "-") && n.endsWith(ext);
    }

    private static FileTime modifiedTimeSafe(Path p) {
        try { return Files.getLastModifiedTime(p); }
        catch (IOException e) { return FileTime.fromMillis(0); }
    }

    private static void safeDelete(Path p) {
        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
    }

    private static String getExtension(String name) {
        int i = name.lastIndexOf('.');
        return (i >= 0) ? name.substring(i) : "";
    }
}
