package org.cursework.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class StorageService {

    @Value("${path.storage}")
    public String storageDirectory;

    public String getStorageDirectory() {
        return storageDirectory;
    }

    public void saveFile(MultipartFile fileToSave) throws IOException {
        String fileName = fileToSave.getOriginalFilename();

        if (fileToSave == null) {
            throw new NullPointerException("fileToSave is null");
        }

        Path targetPath = Paths.get(storageDirectory).resolve(fileName).normalize();
        if (!targetPath.startsWith(Paths.get(storageDirectory).normalize())) {
            throw new SecurityException("Unsupported filename!");
        }

        Files.copy(fileToSave.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public File getDownloadFile(String fileName) throws Exception {
        if (fileName == null) {
            throw new NullPointerException("fileName is null");
        }
        var fileToDownload = new File(storageDirectory + File.separator + fileName);
        if (!Objects.equals(fileToDownload.getParent(), storageDirectory)) {
            throw new SecurityException("Unsupported filename!");
        }
        if (!fileToDownload.exists()) {
            throw new FileNotFoundException("No file named: " + fileName);
        }
        return fileToDownload;
    }
}
