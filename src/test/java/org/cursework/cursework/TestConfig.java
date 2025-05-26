package org.cursework.cursework;

import org.cursework.service.BucketService;
import org.cursework.service.StorageService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class TestConfig {
    @Bean
    public StorageService storageService() {
        return Mockito.mock(StorageService.class);
    }

    @Bean
    public BucketService bucketService() {
        return Mockito.mock(BucketService.class);
    }
}
