package com.HireHub.hirehub.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public class FileDownloadUtil {

    private static final Path ALLOWED_ROOT = Paths.get("photos", "candidate").toAbsolutePath().normalize();

    public Resource getFileAsResourse(String downloadDir, String fileName) throws IOException {
        if (downloadDir == null || fileName == null || downloadDir.isBlank() || fileName.isBlank()) {
            return null;
        }

        String cleanedFileName = StringUtils.cleanPath(fileName);
        if (cleanedFileName.contains("..") || cleanedFileName.contains("/") || cleanedFileName.contains("\\")) {
            return null;
        }

        Path dirPath = Paths.get(downloadDir).toAbsolutePath().normalize();

        // Ensure the target directory resides strictly inside the allowed candidate upload directory
        if (!dirPath.startsWith(ALLOWED_ROOT)) {
            return null;
        }

        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            return null;
        }

        Path targetFile = null;
        try (Stream<Path> stream = Files.list(dirPath)) {
            Optional<Path> matchedPath = stream
                    .filter(Files::isRegularFile)
                    .filter(file -> {
                        String name = file.getFileName().toString();
                        return name.equals(cleanedFileName) || name.startsWith(cleanedFileName);
                    })
                    .findFirst();

            if (matchedPath.isPresent()) {
                targetFile = matchedPath.get().toAbsolutePath().normalize();
            }
        }

        if (targetFile != null && targetFile.startsWith(dirPath) && Files.exists(targetFile)) {
            return new UrlResource(targetFile.toUri());
        }

        return null;
    }
}