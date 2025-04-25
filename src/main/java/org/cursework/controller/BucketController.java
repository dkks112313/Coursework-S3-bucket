package org.cursework.controller;

import org.cursework.service.BucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/{bucket}")
public class BucketController {
    @Autowired
    private BucketService bucketService;
    private static final Logger log = Logger.getLogger(StorageController.class.getName());

    @GetMapping
    public List<String> getListFilesFromBucket(@PathVariable String bucket) {
        List<String> files = bucketService.getListFileObjects(bucket);

        return files;
    }

    @PostMapping
    public void addFileToBucket(@PathVariable String bucket, @RequestParam("fileName") MultipartFile file) {
        try {
            bucketService.saveFileObject(bucket, file);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Exception during upload", e);
        }
    }

    @GetMapping("/{file}")
    public ResponseEntity<Resource> getFileFromBucket(@PathVariable String bucket, @PathVariable("file") String filename) {
        log.log(Level.INFO, "[NORMAL] Download with /download");
        try {
            var fileToDownload = bucketService.getDownloadFileObject(bucket, filename);
            String encodeFileName = URLEncoder.encode(filename, StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodeFileName + "\"")
                    .contentLength(fileToDownload.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new FileSystemResource(fileToDownload));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{file}")
    public void deleteFileFromBucket(@PathVariable("bucket") String bucket, @PathVariable("file") String fileName) {
        log.log(Level.INFO, "[NORMAL] Delete with /delete");
        try {
            bucketService.deleteFileObject(bucket, fileName);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Problem with deleting files", e);
        }
    }
}
