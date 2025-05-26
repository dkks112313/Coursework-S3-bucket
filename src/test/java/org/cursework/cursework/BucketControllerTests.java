package org.cursework.cursework;

import org.cursework.controller.BucketController;
import org.cursework.service.BucketService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BucketController.class)
class BucketControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BucketService bucketService;

    @TestConfiguration
    static class BucketServiceTestConfig {
        @Bean
        public BucketService bucketService() {
            return Mockito.mock(BucketService.class);
        }
    }

    @Test
    void shouldReturnListOfFiles() throws Exception {
        when(bucketService.getListFileObjects()).thenReturn(List.of("file1.txt", "file2.txt"));

        mockMvc.perform(get("/api/testBucket")
                        .header("X-API-Key", Utils.key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("file1.txt"))
                .andExpect(jsonPath("$[1]").value("file2.txt"));

        verify(bucketService, atLeastOnce()).performOperationForBucket("testBucket");
    }

    @Test
    void shouldUploadFileToBucket() throws Exception {
        MockMultipartFile file = new MockMultipartFile("fileName", "test.txt", "text/plain", "hello".getBytes());

        mockMvc.perform(multipart("/api/testBucket").file(file).with(req -> {
                            req.setMethod("POST");
                            return req;
                        })
                        .header("X-API-Key", Utils.key))
                .andExpect(status().isOk());

        verify(bucketService, atLeastOnce()).performOperationForBucket("testBucket");
        verify(bucketService).saveFileObject(file);
    }

    @Test
    void shouldDownloadFileFromBucket() throws Exception {
        File tempFile = File.createTempFile("testfile", ".txt");
        tempFile.deleteOnExit();

        when(bucketService.getDownloadFileObject("file.txt")).thenReturn(tempFile);

        mockMvc.perform(get("/api/testBucket/file.txt")
                        .header("X-API-Key", Utils.key))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("file.txt")))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));

        verify(bucketService, atLeastOnce()).performOperationForBucket("testBucket");
        verify(bucketService).getDownloadFileObject("file.txt");
    }

    @Test
    void shouldReturnNotFoundIfFileMissing() throws Exception {
        when(bucketService.getDownloadFileObject("nofile.txt")).thenThrow(new RuntimeException("File not found"));

        mockMvc.perform(get("/api/testBucket/nofile.txt")
                        .header("X-API-Key", Utils.key))
                .andExpect(status().isNotFound());

        verify(bucketService, atLeastOnce()).performOperationForBucket("testBucket");
    }

    @Test
    void shouldDeleteFileFromBucket() throws Exception {
        mockMvc.perform(delete("/api/testBucket/file.txt")
                        .header("X-API-Key", Utils.key))
                .andExpect(status().isOk());

        verify(bucketService, atLeastOnce()).performOperationForBucket("testBucket");
        verify(bucketService).deleteFileObject("file.txt");
    }
}
