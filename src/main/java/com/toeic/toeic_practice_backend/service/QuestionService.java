package com.toeic.toeic_practice_backend.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {
	private final QuestionRepository questionRepository;
	
	public void importQuestions(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String partNum = sheet.getSheetName();
            Iterator<Row> rows = sheet.iterator();
            rows.next(); // Skip header row

            while (rows.hasNext()) {
                Row currentRow = rows.next();
                Question question = parseRowByPart(currentRow, partNum);
                if (question != null) {
                    questionRepository.save(question);
                }
            }
        }
        workbook.close();
    }
	
	private Question parseRowByPart(Row row, String partNum) {
        switch (partNum) {
            case "1":
                return parsePart1(row);
            // Add other parts here as needed
            default:
                System.err.println("Unsupported part number: " + partNum);
                return null;
        }
    }
	
	private Question parsePart1(Row row) {
        try {
            Question question = new Question();
            question.setPartNum(1);
            question.setType(row.getCell(0).getStringCellValue());
            question.setQuestionNum((int) row.getCell(1).getNumericCellValue());

            // Handle resources if available
            String imageName = row.getCell(2).getStringCellValue();
            String audioName = row.getCell(3).getStringCellValue();
            List<Question.Resource> resources = new ArrayList<>();
            if (imageName != null && !imageName.isEmpty()) {
                resources.add(new Question.Resource("image", imageName));
            }
            if (audioName != null && !audioName.isEmpty()) {
                resources.add(new Question.Resource("audio", audioName));
            }
            question.setResources(resources);
            // Extract options
            List<String> answers = new ArrayList<>();
            for (int i = 4; i <= 7; i++) {
                answers.add(row.getCell(i).getStringCellValue());
            }
            question.setAnswers(answers);
            question.setCorrectAnswer(row.getCell(8).getStringCellValue());

            question.setTranscript(row.getCell(9).getStringCellValue());
            question.setExplanation(row.getCell(10).getStringCellValue());

            return question;
        } catch (Exception e) {
            // Log error and return null to skip this row
            System.err.println("Error parsing row for Part 1: " + e.getMessage());
            return null;
        }
    }
}
