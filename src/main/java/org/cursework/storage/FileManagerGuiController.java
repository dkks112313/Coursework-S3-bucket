package org.cursework.storage;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FileManagerGuiController {

    @GetMapping("/uploader")
    public String uploader() {
        return "uploader";
    }

    @GetMapping("/list-files")
    public String listFiles(Model model) throws IOException {
        var fileStorage = new FileStorageService();
        Path currentRelativePath = new File(fileStorage.getStorageDirectory()).toPath();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentRelativePath)) {
            List<String> fileNames = new ArrayList<>();
            Map<String, Double> fileSize = new HashMap<>();

            for (Path path : stream) {
                fileNames.add(path.getFileName().toString());
                fileSize.put(path.getFileName().toString(), (double) Files.size(path) / 1024 / 1024);
            }

            model.addAttribute("files", fileNames);
            model.addAttribute("filesSize", fileSize);
        }
        return "list_files";
    }
}
