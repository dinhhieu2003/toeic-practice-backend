package com.toeic.toeic_practice_backend.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.PublicAccessType;

@Service
public class AzureBlobService {

    private final BlobContainerClient blobContainerClient;

    public AzureBlobService(@Value("${azure.storage.connection.string}") String connectionString,
            @Value("${azure.storage.container.name}") String containerName) {
    	System.out.println("Container name: " + containerName);
    	System.out.println("ConnectionString: " + connectionString);
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        // Get or create the container
        this.blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!blobContainerClient.exists()) {
            blobContainerClient.create();
            blobContainerClient.setAccessPolicy(PublicAccessType.CONTAINER, null);  // Public container access
        }
    }

    // Upload multiple images
    public List<String> uploadFiles(List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();

        for (MultipartFile file : files) {
            // Get the Blob Client for the file
            BlobClient blobClient = blobContainerClient.getBlobClient(file.getOriginalFilename());

            // Upload the file to Azure Blob Storage
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            // Set content type
            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(file.getContentType());
            blobClient.setHttpHeaders(headers);

            // Add the file URL to the list of URLs
            urls.add(blobClient.getBlobUrl());
        }

        return urls;
    }
    
    public boolean deleteFileByUrl(String url) {
        // Extract the blob name (filename) from the URL
        String blobName = extractBlobNameFromUrl(url);
        
        // Get the Blob Client for the file
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

        // Check if the blob exists before deleting
        if (blobClient.exists()) {
            blobClient.delete();  // Deletes the blob (image)
            return true;  // Return true if deletion was successful
        }

        return false;  
    }

    // Utility method to extract blob name from the URL
    private String extractBlobNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}