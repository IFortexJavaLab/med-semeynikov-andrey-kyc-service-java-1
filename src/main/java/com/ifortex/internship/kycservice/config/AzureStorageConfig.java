package com.ifortex.internship.kycservice.config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureStorageConfig {

    private static final String CONNECTION_STRING = "DefaultEndpointsProtocol=http;AccountName=%s;AccountKey=%s;BlobEndpoint=%s;";
    @Value("${spring.cloud.azure.storage.blob.account-name}")
    private String accountName;

    @Value("${spring.cloud.azure.storage.blob.endpoint}")
    private String endpoint;

    @Value("${spring.cloud.azure.storage.blob.account-key}")
    private String accountKey;

    @Bean
    public BlobServiceClient blobServiceClient() {
        String connectionString = String.format(CONNECTION_STRING, accountName, accountKey, endpoint);
        return new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();
    }
}