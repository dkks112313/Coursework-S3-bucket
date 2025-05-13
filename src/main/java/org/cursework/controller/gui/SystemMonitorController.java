package org.cursework.controller.gui;

import org.cursework.service.BucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/system")
public class SystemMonitorController {
    private static final Logger LOGGER = Logger.getLogger(SystemMonitorController.class.getName());

    @Autowired
    private BucketService bucketService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long allocatedMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = allocatedMemory - freeMemory;

        stats.put("maxMemoryMB", maxMemory);
        stats.put("allocatedMemoryMB", allocatedMemory);
        stats.put("freeMemoryMB", freeMemory);
        stats.put("usedMemoryMB", usedMemory);
        stats.put("memoryUsagePercent", (double) usedMemory / maxMemory * 100);

        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        stats.put("tempDirPath", tempDir.getAbsolutePath());
        stats.put("tempDirTotalSpaceGB", tempDir.getTotalSpace() / (1024 * 1024 * 1024.0));
        stats.put("tempDirFreeSpaceGB", tempDir.getFreeSpace() / (1024 * 1024 * 1024.0));
        stats.put("tempDirUsableSpaceGB", tempDir.getUsableSpace() / (1024 * 1024 * 1024.0));

        try {
            AtomicLong tempFilesCount = new AtomicLong(0);
            AtomicLong tempFilesSize = new AtomicLong(0);

            Files.list(Paths.get(tempDir.getAbsolutePath()))
                    .filter(path -> path.getFileName().toString().startsWith("download-"))
                    .forEach(path -> {
                        tempFilesCount.incrementAndGet();
                        try {
                            tempFilesSize.addAndGet(Files.size(path));
                        } catch (Exception e) {
                            LOGGER.warning("Error getting size for " + path + ": " + e.getMessage());
                        }
                    });

            stats.put("tempDownloadFilesCount", tempFilesCount.get());
            stats.put("tempDownloadFilesSizeMB", tempFilesSize.get() / (1024 * 1024.0));

        } catch (Exception e) {
            stats.put("tempFilesError", e.getMessage());
        }

        stats.putAll(bucketService.getStats());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> forceCleanup() {
        Map<String, Object> result = new HashMap<>();
        int deletedCount = 0;
        long freedSpace = 0;

        try {
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));

            var files = Files.list(tempDir)
                    .filter(path -> path.getFileName().toString().startsWith("download-"))
                    .toList();

            for (Path file : files) {
                long size = Files.size(file);
                boolean deleted = Files.deleteIfExists(file);
                if (deleted) {
                    deletedCount++;
                    freedSpace += size;
                }
            }

            System.gc();

            result.put("success", true);
            result.put("deletedFilesCount", deletedCount);
            result.put("freedSpaceMB", freedSpace / (1024 * 1024.0));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}