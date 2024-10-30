package com.toeic.toeic_practice_backend.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.toeic.toeic_practice_backend.domain.dto.response.test.FullTestResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.MultipleChoiceQuestion;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.mapper.QuestionMapper;
import com.toeic.toeic_practice_backend.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class})
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    @Value("${azure.url-resources}")
    private String urlResource;
    
    public void importQuestions(MultipartFile file, String testId) throws IOException {
    	Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(file.getInputStream());

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String partNum = sheet.getSheetName();
                Iterator<Row> rows = sheet.iterator();
                rows.next(); // Skip header row

                Question currentGroup = null;
                while (rows.hasNext()) {
                    Row currentRow = rows.next();
                    Question question = parseRowByPart(currentRow, partNum);
                    if (question != null) {
                        question.setTestId(testId);
                        question.setPartNum(Integer.parseInt(partNum));

                        if ("group".equalsIgnoreCase(question.getType())) {
                            currentGroup = question;
                            questionRepository.save(currentGroup);
                        } else if (currentGroup != null && "subquestion".equalsIgnoreCase(question.getType())) {
                            questionRepository.save(question);
                            currentGroup.getSubQuestions().add(question);
                            currentGroup.setQuestionNum(question.getQuestionNum());
                            questionRepository.save(currentGroup);
                        } else {
                            currentGroup = null;
                            questionRepository.save(question);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
    }

    private Question parseRowByPart(Row row, String partNum) {
        switch (partNum) {
            case "1":
                return parsePart1(row);
            case "2":
                return parsePart2(row);
            case "3":
            case "4":
                return parsePart3(row);
            case "5":
                return parsePart5(row);
            case "6":
            case "7":
                return parsePart6(row);
            // Add other parts here as needed
            default:
                System.err.println("Unsupported part number: " + partNum);
                return null;
        }
    }

    private Question parsePart1(Row row) {
        try {
            Question question = new Question();
            question.setType(getCellValue(row.getCell(0)));
            question.setQuestionNum((int) getNumericCellValue(row.getCell(1)));

            // Handle resources if available
            String imageName = getCellValue(row.getCell(2));
            String audioName = getCellValue(row.getCell(3));
            List<Question.Resource> resources = new ArrayList<>();
            if (imageName != null && !imageName.isEmpty()) {
                resources.add(new Question.Resource("image", urlResource + imageName));
            }
            if (audioName != null && !audioName.isEmpty()) {
                resources.add(new Question.Resource("audio", urlResource + audioName));
            }
            question.setResources(resources);
            // Extract options
            List<String> answers = new ArrayList<>();
            for (int i = 4; i <= 7; i++) {
                answers.add(getCellValue(row.getCell(i)));
            }
            question.setAnswers(answers);
            question.setCorrectAnswer(getCellValue(row.getCell(8)));

            question.setTranscript(getCellValue(row.getCell(9)));
            question.setExplanation(getCellValue(row.getCell(10)));
            question.setDifficulty((int) getNumericCellValue(row.getCell(11)));

            return question;
        } catch (Exception e) {
            // Log error and return null to skip this row
            System.err.println("Error parsing row for Part 1: " + e.getMessage());
            return null;
        }
    }

    private Question parsePart2(Row row) {
        try {
            Question question = new Question();
            question.setType(getCellValue(row.getCell(0)));
            question.setQuestionNum((int) getNumericCellValue(row.getCell(1)));
            question.setContent(getCellValue(row.getCell(2)));

            // Handle resources if available
            String audioName = getCellValue(row.getCell(3));
            List<Question.Resource> resources = new ArrayList<>();
            if (audioName != null && !audioName.isEmpty()) {
                resources.add(new Question.Resource("audio", urlResource + audioName));
            }
            question.setResources(resources);

            // Extract options
            List<String> answers = new ArrayList<>();
            for (int i = 4; i <= 6; i++) {
                answers.add(getCellValue(row.getCell(i)));
            }
            question.setAnswers(answers);
            question.setCorrectAnswer(getCellValue(row.getCell(7)));

            question.setTranscript(getCellValue(row.getCell(8)));
            question.setExplanation(getCellValue(row.getCell(9)));
            question.setDifficulty((int) getNumericCellValue(row.getCell(10)));
            return question;
        } catch (Exception e) {
            // Log error and return null to skip this row
            System.err.println("Error parsing row for Part 2: " + e.getMessage());
            return null;
        }
    }

    private Question parsePart3(Row row) {
        Question question = new Question();
        try {
            question.setType(getCellValue(row.getCell(0)));
            if (!"group".equalsIgnoreCase(question.getType())) {
                question.setQuestionNum((int) getNumericCellValue(row.getCell(1)));
            }
            question.setContent(getCellValue(row.getCell(2)));

            // Handle resources if available
            String imageName = getCellValue(row.getCell(3));
            String audioName = getCellValue(row.getCell(4));
            List<Question.Resource> resources = new ArrayList<>();
            if (imageName != null && !imageName.isEmpty()) {
                resources.add(new Question.Resource("image", urlResource + imageName));
            }
            if (audioName != null && !audioName.isEmpty()) {
                resources.add(new Question.Resource("audio", urlResource + audioName));
            }
            question.setResources(resources);

            // Extract options
            List<String> answers = new ArrayList<>();
            for (int i = 5; i <= 8; i++) {
                answers.add(getCellValue(row.getCell(i)));
            }
            question.setAnswers(answers);
            question.setCorrectAnswer(getCellValue(row.getCell(9)));

            question.setTranscript(getCellValue(row.getCell(10)));
            question.setExplanation(getCellValue(row.getCell(11)));
            question.setDifficulty((int) getNumericCellValue(row.getCell(12)));

            return question;
        } catch (Exception e) {
            System.out.println(question.getType());
            // Log error and return null to skip this row
            System.err.println("Error parsing row for Part 3: " + e.getMessage());
            return null;
        }
    }

    private Question parsePart5(Row row) {
        Question question = new Question();
        try {
            question.setType(getCellValue(row.getCell(0)));
            question.setQuestionNum((int) getNumericCellValue(row.getCell(1)));
            question.setContent(getCellValue(row.getCell(2)));

            // Extract options
            List<String> answers = new ArrayList<>();
            for (int i = 3; i <= 6; i++) {
                answers.add(getCellValue(row.getCell(i)));
            }
            question.setAnswers(answers);
            question.setCorrectAnswer(getCellValue(row.getCell(7)));
            question.setExplanation(getCellValue(row.getCell(8)));
            question.setDifficulty((int) getNumericCellValue(row.getCell(9)));

            return question;
        } catch (Exception e) {
            // Log error and return null to skip this row
            System.err.println("Error parsing row for Part 5: " + e.getMessage());
            return null;
        }
    }

    private Question parsePart6(Row row) {
        try {
            Question question = new Question();
            question.setType(getCellValue(row.getCell(0)));
            if (!"group".equalsIgnoreCase(question.getType())) {
                question.setQuestionNum((int) getNumericCellValue(row.getCell(1)));
            }
            question.setContent(getCellValue(row.getCell(2)));

            // Handle paragraph texts and their order
            String paragraphTexts = getCellValue(row.getCell(3));
            String paragraphOrders = getCellValue(row.getCell(4));
            String imageNames = getCellValue(row.getCell(5));
            String imageOrders = getCellValue(row.getCell(6));

            Map<Integer, String> resourceOrderMap = new HashMap<>();

            // Parse paragraph texts and their order
            if (paragraphTexts != null && !paragraphTexts.isEmpty() && paragraphOrders != null && !paragraphOrders.isEmpty()) {
                String[] paragraphs = paragraphTexts.split("\\(\\*\\)");
                String[] paragraphOrderArray = paragraphOrders.split(",");
                for (int i = 0; i < paragraphs.length; i++) {
                    resourceOrderMap.put(Integer.parseInt(paragraphOrderArray[i]), "paragraph:" + paragraphs[i].trim());
                }
            }

            // Parse image names and their order
            if (imageNames != null && !imageNames.isEmpty() && imageOrders != null && !imageOrders.isEmpty()) {
                String[] images = imageNames.split(";");
                String[] imageOrderArray = imageOrders.split(",");
                for (int i = 0; i < images.length; i++) {
                    resourceOrderMap.put(Integer.parseInt(imageOrderArray[i]), "image:" + urlResource + images[i].trim());
                }
            }

            // Sort resources based on their order and create resource list
            List<Question.Resource> orderedResources = resourceOrderMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> {
                        String[] typeAndContent = entry.getValue().split(":", 2);
                        return new Question.Resource(typeAndContent[0], typeAndContent[1]);
                    })
                    .collect(Collectors.toList());

            question.setResources(orderedResources);

            // Extract options
            List<String> answers = new ArrayList<>();
            for (int i = 7; i <= 10; i++) {
                answers.add(getCellValue(row.getCell(i)));
            }
            question.setAnswers(answers);
            question.setCorrectAnswer(getCellValue(row.getCell(11)));
            question.setTranscript(getCellValue(row.getCell(12)));
            question.setExplanation(getCellValue(row.getCell(13)));
            question.setDifficulty((int) getNumericCellValue(row.getCell(14)));

            return question;
        } catch (Exception e) {
        	System.out.println(row.getRowNum());
            // Log error and return null to skip this row
            System.err.println("Error parsing row for Part 6: " + e.getMessage());
            return null;
        }
    }

    private String getCellValue(Cell cell) {
        return (cell != null) ? cell.getStringCellValue() : null;
    }

    private double getNumericCellValue(Cell cell) {
        return (cell != null) ? cell.getNumericCellValue() : 0;
    }

    public List<Question> getQuestionByTestId(String testId, String listPart) {
    	
    	List<Integer> listPartInt = listPart.chars()
    			.mapToObj(c -> Character.getNumericValue(c))
    			.collect(Collectors.toList());
    	
    	List<Question> questions = questionRepository.findByTestIdAndTypeIsNotSubquestion(testId, listPartInt);
    	return questions;
    }
}
