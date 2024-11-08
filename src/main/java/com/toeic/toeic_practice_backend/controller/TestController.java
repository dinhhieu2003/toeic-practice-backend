package com.toeic.toeic_practice_backend.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.toeic.toeic_practice_backend.domain.dto.request.test.SubmitTestRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.test.TestCreationRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.FullTestResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.TestResultIdResponse;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.domain.entity.Test;
import com.toeic.toeic_practice_backend.service.AzureBlobService;
import com.toeic.toeic_practice_backend.service.QuestionService;
import com.toeic.toeic_practice_backend.service.TestService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tests")
@RequiredArgsConstructor
public class TestController {
	private final TestService testService;
	private final AzureBlobService azureBlobService;
	private final QuestionService questionService;
	
	@PostMapping("{testId}/import")
    public ResponseEntity<?> importQuestions(@RequestParam("file") MultipartFile file, @PathVariable String testId) {
        try {
            questionService.importQuestions(file, testId);
            return ResponseEntity.ok(null);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to import questions: " + e.getMessage());
        }
    }
	
	@PostMapping("/resources/upload")
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
	
	@DeleteMapping("/resources/delete")
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
	
	@PostMapping("")
	public ResponseEntity<Test> addTest(@RequestBody TestCreationRequest test) {
		return ResponseEntity.status(HttpStatus.CREATED).body(testService.addTest(test));
	}
	
	@GetMapping("")
	public ResponseEntity<PaginationResponse<List<Test>>> getAllTest(
			@RequestParam(defaultValue = "1") String current,
			@RequestParam(defaultValue = "5") String pageSize) {
		int currentInt = Integer.parseInt(current)-1;
		int pageSizeInt = Integer.parseInt(pageSize);
		Pageable pageable = PageRequest.of(currentInt, pageSizeInt);
		return ResponseEntity.ok(testService.getAllTest(pageable));
	}
	
	@GetMapping("/{testId}/full-test")
	public ResponseEntity<FullTestResponse> getQuestionFullTest(@PathVariable String testId) {
		return ResponseEntity.ok(testService.getQuestionTest(testId, "1234567"));
	}
	
	@GetMapping("/{testId}/practice")
	public ResponseEntity<FullTestResponse> getQuestionByParts(@PathVariable String testId, @RequestParam String parts) {
		return ResponseEntity.ok(testService.getQuestionTest(testId, parts));
	}
	
	@PostMapping("/submit")
//	dùng map để chấm bài => lưu vào result => get result => convert từ test sang kiểu khác phù hợp cho frontend(có userAns và isCorrect)
	public ResponseEntity<TestResultIdResponse> submitTest(@RequestBody SubmitTestRequest submitTestRequest) {
		return ResponseEntity.ok(testService.submitTest(submitTestRequest));
	}
	
	@GetMapping("/{testId}/questions")
	public ResponseEntity<PaginationResponse<List<Question>>> getAllQuestionsInTestByTestId(
			@PathVariable String testId, 
			@RequestParam(defaultValue = "1") String current,
			@RequestParam(defaultValue = "5") String pageSize) {
		int currentInt = Integer.parseInt(current)-1;
		int pageSizeInt = Integer.parseInt(pageSize);
		Pageable pageable = PageRequest.of(currentInt, pageSizeInt);
		return ResponseEntity.ok(testService.getAllQuestionsInTestByTestId(testId, pageable));
	}
}
