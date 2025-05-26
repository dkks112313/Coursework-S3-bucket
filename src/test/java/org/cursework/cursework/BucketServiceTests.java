package org.cursework.cursework;

import org.cursework.service.BucketService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BucketServiceTests {

    @InjectMocks
    private BucketService bucketService;

    private AutoCloseable closeable;

    private Path tempStorageDir;

    @BeforeEach
    void setUp() throws IOException {
        closeable = MockitoAnnotations.openMocks(this);

        tempStorageDir = Files.createTempDirectory("storage-test");
        bucketService.storageDirectory = tempStorageDir.toString();

        Path bucketPath = tempStorageDir.resolve("data").resolve("testBucket");
        Files.createDirectories(bucketPath);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        deleteDirectoryRecursively(tempStorageDir);
    }

    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException ignored) {}
                    });
        }
    }

    @Test
    void testPerformOperationForBucket_setsBucketName() {
        bucketService.performOperationForBucket("testBucket");
        assertEquals("testBucket", bucketService.getBucketName());
        assertNotNull(bucketService.getBucket());
        assertEquals("testBucket", bucketService.getBucket().getName());
    }

    @Test
    void testCheckBucketIsExist_returnsTrueWhenExists() {
        bucketService.performOperationForBucket("testBucket");
        boolean exists = bucketService.checkBucketIsExist();
        assertTrue(exists);
    }

    @Test
    void testCheckBucketIsExist_returnsFalseWhenNotExists() {
        bucketService.performOperationForBucket("nonexistentBucket");
        boolean exists = bucketService.checkBucketIsExist();
        assertFalse(exists);
    }

    @Test
    void testGetListFileObjects_returnsEmptyListIfNoFiles() {
        bucketService.performOperationForBucket("testBucket");
        List<String> files = bucketService.getListFileObjects();
        assertNotNull(files);
        assertTrue(files.isEmpty());
    }

    @Test
    void testGetListFileObjects_returnsDirectoryNames() throws IOException {
        bucketService.performOperationForBucket("testBucket");
        Path bucketPath = Paths.get(bucketService.getStorageDataDirectory(), "testBucket");
        Files.createDirectory(bucketPath.resolve("file1"));
        Files.createDirectory(bucketPath.resolve("file2"));

        List<String> files = bucketService.getListFileObjects();
        assertTrue(files.contains("file1"));
        assertTrue(files.contains("file2"));
    }

    @Test
    void testSaveFileObject_uploadsFileSuccessfully() throws IOException {
        bucketService.performOperationForBucket("testBucket");

        byte[] content = "Hello World".getBytes();
        MockMultipartFile multipartFile = new MockMultipartFile("file", "testFile.txt", "text/plain", content);

        bucketService.saveFileObject(multipartFile);

        Path fileDir = Paths.get(bucketService.getStorageDataDirectory(), "testBucket", "testFile.txt");
        assertTrue(Files.exists(fileDir));
        assertTrue(Files.isDirectory(fileDir));

        Path partsDir = fileDir.resolve("parts");
        assertTrue(Files.exists(partsDir));
        assertTrue(Files.isDirectory(partsDir));

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(partsDir)) {
            assertTrue(stream.iterator().hasNext());
        }
    }

    @Test
    void testSaveFileObject_throwsExceptionWhenBucketNotExist() {
        bucketService.performOperationForBucket("nonexistentBucket");

        MockMultipartFile multipartFile = new MockMultipartFile("file", "file.txt", "text/plain", new byte[0]);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bucketService.saveFileObject(multipartFile);
        });

        assertTrue(exception.getMessage().contains("Bucket does not exist"));
    }

    @Test
    void testGetDownloadFileObject_returnsFile() throws Exception {
        bucketService.performOperationForBucket("testBucket");

        Path fileDir = Paths.get(bucketService.getStorageDataDirectory(), "testBucket", "testFile");
        Path partsDir = fileDir.resolve("parts");
        Files.createDirectories(partsDir);

        Path part0 = partsDir.resolve("part.0");
        Files.write(part0, "content".getBytes());

        File downloadFile = bucketService.getDownloadFileObject("testFile");
        assertNotNull(downloadFile);
        assertTrue(downloadFile.exists());
        assertTrue(downloadFile.length() > 0);
    }

    @Test
    void testGetDownloadFileObject_throwsFileNotFound() {
        bucketService.performOperationForBucket("testBucket");

        Exception exception = assertThrows(FileNotFoundException.class, () -> {
            bucketService.getDownloadFileObject("missingFile");
        });

        assertTrue(exception.getMessage().contains("Parts directory not found"));
    }

    @Test
    void testDeleteFileObject_deletesFile() throws Exception {
        bucketService.performOperationForBucket("testBucket");

        Path fileDir = Paths.get(bucketService.getStorageDataDirectory(), "testBucket", "fileToDelete");
        Files.createDirectories(fileDir);

        Path partFile = fileDir.resolve("dummy.txt");
        Files.write(partFile, "dummy".getBytes());

        bucketService.deleteFileObject("fileToDelete");

        assertFalse(Files.exists(fileDir));
    }

    @Test
    void testDeleteFileObject_throwsFileNotFound() {
        bucketService.performOperationForBucket("testBucket");

        Exception exception = assertThrows(FileNotFoundException.class, () -> {
            bucketService.deleteFileObject("nonexistentFile");
        });

        assertTrue(exception.getMessage().contains("No file named"));
    }

    @Test
    void testGetStats_returnsValidStats() {
        Map<String, Object> stats = bucketService.getStats();

        assertNotNull(stats);
        assertTrue(stats.containsKey("activeOperations"));
        assertTrue(stats.containsKey("maxMemoryMB"));
        assertTrue(stats.containsKey("allocatedMemoryMB"));
        assertTrue(stats.containsKey("freeMemoryMB"));
        assertTrue(stats.containsKey("usedMemoryMB"));
        assertTrue(stats.containsKey("memoryUsagePercent"));

        Number percent = (Number) stats.get("memoryUsagePercent");
        assertTrue(percent.doubleValue() >= 0 && percent.doubleValue() <= 100);
    }
}