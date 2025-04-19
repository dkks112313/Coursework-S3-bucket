package org.cursework.service;

import org.cursework.storage.FileDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
    private String storageDirectory;

    public String getStorageDirectory() {
        return storageDirectory;
    }

    public void saveFile(MultipartFile fileToSave) throws IOException {
        String fileName = fileToSave.getOriginalFilename();

        if (fileToSave == null) {
            throw new NullPointerException("fileToSave is null");
        }

        String absolute = FileDirectory.createDirectory(storageDirectory, fileName);

        Path mail = Paths.get(absolute, fileName);
        Path targetPath = mail.normalize();

        if (!targetPath.startsWith(Paths.get(absolute, fileName).normalize())) {
            throw new SecurityException("Unsupported filename!");
        }

        Files.copy(fileToSave.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public File getDownloadFile(String fileName) throws Exception {
        if (fileName == null) {
            throw new NullPointerException("fileName is null");
        }

        var fileToDownload = new File(Path.of(storageDirectory, fileName, fileName).toString());
        System.out.println(fileToDownload.getAbsolutePath());

        if (!Objects.equals(fileToDownload.getParent(), Path.of(storageDirectory, fileName).toString())) {
            throw new SecurityException("Unsupported filename!");
        }

        if (!fileToDownload.exists()) {
            throw new FileNotFoundException("No file named: " + fileName);
        }

        return fileToDownload;
    }
}
