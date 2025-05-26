package org.cursework.cursework;

import org.cursework.controller.gui.StorageGuiController;
import org.cursework.service.BucketService;
import org.cursework.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StorageGuiController.class)
@Import(TestConfig.class)
class StorageGuiControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StorageService fileStorage;

    @Autowired
    private BucketService fileBucket;

    @Test
    void testUploaderView() throws Exception {
        mockMvc.perform(get("/uploader")
                        .header("X-API-Key", ReadKey.key))
                .andExpect(status().isOk())
                .andExpect(view().name("uploader"));
    }

    @Test
    void testListFiles() throws Exception {
        String bucket = "myBucket";

        doNothing().when(fileBucket).performOperationForBucket(bucket);
        when(fileBucket.getListFileObjects()).thenReturn(List.of("file1.txt", "file 2.txt"));

        mockMvc.perform(get("/" + bucket + "/list-files")
                        .header("X-API-Key", ReadKey.key))
                .andExpect(status().isOk())
                .andExpect(view().name("list_files"))
                .andExpect(model().attribute("bucket", bucket))
                .andExpect(model().attributeExists("files"));
    }
}
