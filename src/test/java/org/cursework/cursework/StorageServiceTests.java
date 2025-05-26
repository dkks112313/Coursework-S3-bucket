package org.cursework.cursework;

import org.cursework.service.StorageService;
import org.cursework.storage.FileDirectory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StorageServiceTests {

    private StorageService storageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        storageService = new StorageService();
        storageService.storageDirectory = tempDir.toString();
    }

    @Test
    void testGetStorageDirectory() {
        String expected = Path.of(tempDir.toString(), "data").toString();
        assertEquals(expected, storageService.getStorageDirectory());
    }

    @Test
    void testCreateBucket_Success() {
        File dataDir = new File(storageService.getStorageDirectory());
        assertTrue(dataDir.mkdirs());

        String bucketName = "bucket1";

        try (MockedStatic<FileDirectory> mocked = Mockito.mockStatic(FileDirectory.class)) {
            mocked.when(() -> FileDirectory.createDirectory(storageService.getStorageDirectory(), bucketName))
                    .then(invocation -> {
                        File bucketDir = new File(storageService.getStorageDirectory(), bucketName);
                        assertTrue(bucketDir.mkdir());
                        return null;
                    });

            boolean result = storageService.createBucket(bucketName);
            assertTrue(result);

            mocked.verify(() -> FileDirectory.createDirectory(storageService.getStorageDirectory(), bucketName));
        }
    }

    @Test
    void testCreateBucket_AlreadyExists_Throws() {
        File dataDir = new File(storageService.getStorageDirectory());
        assertTrue(dataDir.mkdirs());

        String bucketName = "bucketExists";

        File existingBucket = new File(storageService.getStorageDirectory(), bucketName);
        assertTrue(existingBucket.mkdir());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> storageService.createBucket(bucketName));

        assertEquals("Bucket name already exists", ex.getMessage());
    }

    @Test
    void testDeleteBucket_BucketDoesNotExist_Throws() {
        File dataDir = new File(storageService.getStorageDirectory());
        assertTrue(dataDir.mkdirs());

        String bucketName = "nonexistent";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> storageService.deleteBucket(bucketName));

        assertEquals("Bucket does not exist", ex.getMessage());
    }

    @Test
    void testDeleteBucket_EmptyBucket_DeletesSuccessfully() {
        File dataDir = new File(storageService.getStorageDirectory());
        assertTrue(dataDir.mkdirs());

        String bucketName = "emptyBucket";

        File bucketDir = new File(storageService.getStorageDirectory(), bucketName);
        assertTrue(bucketDir.mkdir());

        assertEquals(0, bucketDir.listFiles().length);

        storageService.deleteBucket(bucketName);

        assertFalse(bucketDir.exists());
    }

    @Test
    void testDeleteBucket_NonEmptyBucket_NotDeleted() throws IOException {
        File dataDir = new File(storageService.getStorageDirectory());
        assertTrue(dataDir.mkdirs());

        String bucketName = "nonEmptyBucket";

        File bucketDir = new File(storageService.getStorageDirectory(), bucketName);
        assertTrue(bucketDir.mkdir());

        File dummyFile = new File(bucketDir, "file.txt");
        assertTrue(dummyFile.createNewFile());

        storageService.deleteBucket(bucketName);

        assertTrue(bucketDir.exists());
    }
}
