package org.cursework.controller;

import org.cursework.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class StorageController {

    @Autowired
    private StorageService fileStorageService;
    private static final Logger log = Logger.getLogger(StorageController.class.getName());

    @PostMapping("/create/{bucket}")
    public void createBucket(@PathVariable String bucket) {
        fileStorageService.createBucket(bucket);
    }

    @DeleteMapping("/delete/{bucket}")
    public void deleteBucket(@PathVariable String bucket) {
        log.log(Level.INFO, "[NORMAL] Delete with /delete");
        try {
            fileStorageService.deleteBucket(bucket);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Problem with deleting files", e);
        }
    }

}