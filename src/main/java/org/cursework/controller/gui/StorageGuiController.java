package org.cursework.controller.gui;

import org.cursework.service.BucketService;
import org.cursework.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Controller
public class StorageGuiController {
    @Autowired
    private StorageService fileStorage;

    @Autowired
    private BucketService fileBucket;

    @GetMapping("/uploader")
    public String uploader() {
        return "uploader";
    }

    @GetMapping("/list-buckets")
    public String listBuckets(Model model) throws IOException {
        Path currentRelativePath = new File(fileStorage.getStorageDirectory()).toPath();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentRelativePath)) {
            List<String> fileNames = new ArrayList<>();

            for (Path path : stream) {
                String st = path.getFileName().toString();
                fileNames.add(st);
            }

            model.addAttribute("buckets", fileNames);
        }

        return "list_buckets";
    }

    @GetMapping("/{bucket}/list-files")
    public String listFiles(Model model, @PathVariable String bucket) throws IOException {
        fileBucket.performOperationForBucket(bucket);

        List<String> fileNames = fileBucket.getListFileObjects();

        List<FileEntry> files = fileNames.stream()
                .map(name -> new FileEntry(name, URLEncoder.encode(name, StandardCharsets.UTF_8)))
                .toList();

        model.addAttribute("bucket", bucket);
        model.addAttribute("files", files);

        return "list_files";
    }
}
