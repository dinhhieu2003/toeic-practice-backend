package com.toeic.toeic_practice_backend.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.toeic.toeic_practice_backend.service.AzureBlobService;
import com.toeic.toeic_practice_backend.service.TestService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tests")
@RequiredArgsConstructor
public class TestController {
	private final TestService testService;
	private final AzureBlobService azureBlobService;
	
	@PostMapping("/upload")
    public ResponseEntity<?> uploadFiles(@RequestParam("files") List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please select images to upload.");
        }

        try {
            // Upload multiple files and get their URLs
            List<String> urls = azureBlobService.uploadFiles(files);
            return ResponseEntity.ok(urls);  // Return the list of URLs
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload images: " + e.getMessage());
        }
    }
	
	@DeleteMapping("/delete")
    public ResponseEntity<?> deleteImages(@RequestBody List<String> urls) {
        for(String url: urls) {
        	boolean canDelete = azureBlobService.deleteFileByUrl(url);

            if (canDelete) {
            	System.out.println("Image deleted successfully.");
            } else {
                System.out.println("Not found url: " + url);
            }
        }
        return ResponseEntity.ok(null);
    }
	
}
