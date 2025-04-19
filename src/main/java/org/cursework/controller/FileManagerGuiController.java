package org.cursework.controller;

import org.cursework.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Controller
public class FileManagerGuiController {
    @Autowired
    private StorageService fileStorage;

    @GetMapping("/uploader")
    public String uploader() {
        return "uploader";
    }

    @GetMapping("/list-files")
    public String listFiles(Model model) throws IOException {
        Path currentRelativePath = new File(fileStorage.getStorageDirectory()).toPath();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentRelativePath)) {
            List<String> fileNames = new ArrayList<>();

            for (Path path : stream) {
                fileNames.add(path.getFileName().toString());
            }

            model.addAttribute("files", fileNames);
        }

        return "list_files";
    }
}
