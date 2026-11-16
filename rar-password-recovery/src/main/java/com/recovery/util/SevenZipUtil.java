package com.recovery.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SevenZipUtil {
    private SevenZipUtil() {
    }

    public static Path findDefaultPath() {
        String[] candidates = {
            "C:\\Program Files\\7-Zip\\7z.exe",
            "C:\\Program Files (x86)\\7-Zip\\7z.exe"
        };

        for (String candidate : candidates) {
            Path path = Paths.get(candidate);
            if (Files.isRegularFile(path)) {
                return path;
            }
        }
        return null;
    }
}
