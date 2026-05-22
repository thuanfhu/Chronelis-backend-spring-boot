package com.devloopsx.chronelis.configuration;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class AzureBlobConfiguration {

  @Value("${azure.storage.account-name}")
  private String accountName;

  @Value("${azure.storage.account-key}")
  private String accountKey;

  @Value("${azure.storage.container-name}")
  private String containerName;

  @Value("${azure.storage.endpoint}")
  private String endpoint;

  @Bean
  public BlobServiceClient blobServiceClient() {
    String resolvedAccountName = requireNonBlank(accountName, "AZURE_STORAGE_ACCOUNT_NAME");
    String resolvedAccountKey = requireNonBlank(accountKey, "AZURE_STORAGE_ACCOUNT_KEY");
    String resolvedEndpoint = requireNonBlank(endpoint, "AZURE_STORAGE_ENDPOINT");

    StorageSharedKeyCredential credential =
        new StorageSharedKeyCredential(resolvedAccountName, resolvedAccountKey);
    return new BlobServiceClientBuilder()
        .endpoint(resolvedEndpoint)
        .credential(credential)
        .buildClient();
  }

  @Bean
  public BlobContainerClient blobContainerClient(BlobServiceClient blobServiceClient) {
    String resolvedContainerName = requireNonBlank(containerName, "AZURE_STORAGE_CONTAINER_NAME");
    return blobServiceClient.getBlobContainerClient(resolvedContainerName);
  }

  private String requireNonBlank(String value, String envKey) {
    if (!StringUtils.hasText(value)) {
      throw new IllegalStateException("Missing required Azure Blob configuration: " + envKey);
    }
    return value.trim();
  }
}
