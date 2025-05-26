package org.cursework.cursework;

import org.cursework.controller.StorageController;
import org.cursework.service.StorageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StorageController.class)
@Import(StorageControllerTest.MockedServiceConfig.class)
class StorageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StorageService fileStorageService;

    @Test
    void testCreateBucket() throws Exception {
        String bucket = "testBucket";

        mockMvc.perform(post("/api/create/{bucket}", bucket)
                        .header("X-API-Key", Utils.key))
                .andExpect(status().isOk());

        verify(fileStorageService, times(1)).createBucket(bucket);
    }

    @Test
    void testDeleteBucket_ExceptionHandled() throws Exception {
        String bucket = "testBucket";

        doThrow(new RuntimeException("Simulated error")).when(fileStorageService).deleteBucket(bucket);

        mockMvc.perform(delete("/api/delete/{bucket}", bucket)
                        .header("X-API-Key", Utils.key))
                .andExpect(status().isOk());

        verify(fileStorageService, times(1)).deleteBucket(bucket);
    }

    @TestConfiguration
    static class MockedServiceConfig {
        @Bean
        public StorageService fileStorageService() {
            return Mockito.mock(StorageService.class);
        }
    }
}
