package org.cursework.controller;

import org.cursework.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class StorageController {

    @Autowired
    private StorageService fileStorageService;
    private static final Logger log = Logger.getLogger(StorageController.class.getName());

    @PostMapping("/create/{bucket}")
    public boolean createBucket(@PathVariable String bucket) {
        try {
            //fileStorageService.saveFile(file);
            throw new IOException("");
            //return true;
        } catch (IOException e) {
            log.log(Level.SEVERE, "Exception during upload", e);
        }
        return false;
    }

    @DeleteMapping("/delete/{bucket}")
    public void deleteBucket(@PathVariable String bucket) {
        log.log(Level.INFO, "[NORMAL] Delete with /delete");
        try {
            //fileStorageService.deleteFile(bucket);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Problem with deleting files", e);
        }
    }

}