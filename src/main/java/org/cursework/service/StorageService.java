package org.cursework.service;

import org.cursework.storage.FileDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

@Service
public class StorageService {

    @Value("${path.storage}")
    private String storageDirectory;

    public String getStorageDirectory() {
        return Path.of(storageDirectory, "data").toString();
    }

    public void saveFileInChunks(MultipartFile fileToSave) throws IOException {
        if (fileToSave == null) {
            throw new NullPointerException("fileToSave is null");
        }

        String fileName = fileToSave.getOriginalFilename();
        String absoluteDir = FileDirectory.createDirectory(getStorageDirectory(), fileName);
        Path targetDirectory = Paths.get(absoluteDir).normalize();

        if (!targetDirectory.startsWith(Paths.get(absoluteDir).normalize())) {
            throw new SecurityException("Unsupported filename!");
        }

        final int CHUNK_SIZE = 1024 * 1024; // 1MB
        byte[] buffer = new byte[CHUNK_SIZE];
        int bytesRead;
        int chunkIndex = 0;

        try (InputStream inputStream = fileToSave.getInputStream()) {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String chunkFileName = String.format("%s.part%d", fileName, chunkIndex++);
                Path chunkPath = targetDirectory.resolve(chunkFileName);

                try (OutputStream outputStream = Files.newOutputStream(chunkPath, StandardOpenOption.CREATE)) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    public void saveFile(MultipartFile fileToSave) throws IOException {
        String fileName = fileToSave.getOriginalFilename();

        if (fileToSave == null) {
            throw new NullPointerException("fileToSave is null");
        }

        String absolute = FileDirectory.createDirectory(storageDirectory+File.separator+"data", fileName);

        Path mail = Paths.get(absolute, fileName);
        Path targetPath = mail.normalize();

        if (!targetPath.startsWith(Paths.get(absolute, fileName).normalize())) {
            throw new SecurityException("Unsupported filename!");
        }

        Files.copy(fileToSave.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public File getDownloadFileChunks(String fileName) throws Exception {
        if (fileName == null) {
            throw new NullPointerException("fileName is null");
        }

        Path chunkDir = Path.of(storageDirectory, "data", fileName);
        if (!Files.exists(chunkDir) || !Files.isDirectory(chunkDir)) {
            throw new FileNotFoundException("No chunk directory for: " + fileName);
        }

        Path assembledPath = Files.createTempFile("assembled-", "-" + fileName);
        try (OutputStream outputStream = Files.newOutputStream(assembledPath)) {
            int index = 0;
            while (true) {
                Path chunkPath = chunkDir.resolve(fileName + ".part" + index);
                if (!Files.exists(chunkPath)) break;

                Files.copy(chunkPath, outputStream);
                index++;
            }

            if (index == 0) {
                throw new FileNotFoundException("No chunks found for: " + fileName);
            }
        }

        return assembledPath.toFile();
    }

    public File getDownloadFile(String fileName) throws Exception {
        if (fileName == null) {
            throw new NullPointerException("fileName is null");
        }

        var fileToDownload = new File(Path.of(getStorageDirectory(), fileName, fileName).toString());
        System.out.println(fileToDownload.getAbsolutePath());

        if (!Objects.equals(fileToDownload.getParent(), Path.of(getStorageDirectory(), fileName).toString())) {
            throw new SecurityException("Unsupported filename!");
        }

        if (!fileToDownload.exists()) {
            throw new FileNotFoundException("No file named: " + fileName);
        }

        return fileToDownload;
    }

    public static void deleteDirectory(File directory) {
        File[] contents = directory.listFiles();
        if (contents != null) {
            for (File file : contents) {
                deleteDirectory(file);
            }
        }
        directory.delete();
    }

    public void deleteFile(String fileName) throws Exception {
        if (fileName == null) {
            throw new NullPointerException("fileName is null");
        }

        var fileToDelete = Path.of(getStorageDirectory(), fileName);
        if (!Files.exists(fileToDelete)) {
            throw new FileNotFoundException("No file named: " + fileName);
        }

        Files.walkFileTree(fileToDelete, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}
