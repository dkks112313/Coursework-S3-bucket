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
import java.net.URI;
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
        bucketService.performOperationForBucket(bucket);

        List<String> files = bucketService.getListFileObjects();

        return files;
    }

    @PostMapping
    public void addFileToBucket(@PathVariable String bucket, @RequestParam("fileName") MultipartFile file) {
        bucketService.performOperationForBucket(bucket);

        log.log(Level.INFO, "[NORMAL] Add file with /");
        try {
            bucketService.saveFileObject(file);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Exception during upload", e);
        }
    }

    @GetMapping("/{file}")
    public ResponseEntity<Resource> getFileFromBucket(@PathVariable String bucket, @PathVariable("file") String filename) {
        bucketService.performOperationForBucket(bucket);

        log.log(Level.INFO, "[NORMAL] Download with /download");
        try {
            var fileToDownload = bucketService.getDownloadFileObject(filename);
            String encodeFileName = new URI(null, null, filename, null)
                    .toASCIIString();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''\"" + encodeFileName + "\"")
                    .contentLength(fileToDownload.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new FileSystemResource(fileToDownload));
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error while downloading file: " + filename, e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{file}")
    public void deleteFileFromBucket(@PathVariable("bucket") String bucket, @PathVariable("file") String fileName) {
        bucketService.performOperationForBucket(bucket);

        log.log(Level.INFO, "[NORMAL] Delete with /delete");
        try {
            bucketService.deleteFileObject(fileName);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Problem with deleting files", e);
        }
    }
}
