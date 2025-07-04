package com.toeic.toeic_practice_backend.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.toeic.toeic_practice_backend.domain.dto.request.question.UpdateQuestionRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.question.UpdateQuestionResourceResponse;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.domain.entity.Question.Resource;
import com.toeic.toeic_practice_backend.domain.entity.Result;
import com.toeic.toeic_practice_backend.domain.entity.Topic;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.QuestionRepository;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(rollbackFor = {Exception.class})
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final TopicService topicService;
    private final ResultService resultService;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;
    @Value("${azure.url-resources}")
    private String urlResource;
    private static final double SIMILARITY_THRESHOLD = 0.9;
    
    @CacheEvict(value = "fullTests", allEntries = true)
    public Question updateQuestion(UpdateQuestionRequest updateQuestionRequest) {
    	log.info("Start: Function update question");
    	Question existingQuestion = questionRepository
    			.findById(updateQuestionRequest.getId())
    			.orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
    	boolean needsDeactivation = false;
        boolean needsStatUpdate = false;
        
        // check needsDeactive       
        if (!isSingleStringSimilar(existingQuestion.getContent(), updateQuestionRequest.getContent())
        		|| !isSingleStringSimilar(existingQuestion.getCorrectAnswer(), updateQuestionRequest.getCorrectAnswer())
        		|| !isListSimilar(existingQuestion.getAnswers(), updateQuestionRequest.getAnswers())) {
            needsDeactivation = true;
        }
        
        existingQuestion.setContent(updateQuestionRequest.getContent());
        existingQuestion.setCorrectAnswer(updateQuestionRequest.getCorrectAnswer());
        existingQuestion.setAnswers(updateQuestionRequest.getAnswers());
        
        List<String> newTopicIds = updateQuestionRequest.getListTopicIds();
        List<String> currentTopicIds = existingQuestion.getTopic().stream()
                                            .map(Topic::getId)
                                            .collect(Collectors.toList());

        if (!currentTopicIds.equals(newTopicIds)) {
            List<Topic> updatedTopics = topicService.getTopicByIds(newTopicIds);
            existingQuestion.setTopic(updatedTopics);
            needsStatUpdate = true;
        }
        
        existingQuestion.setDifficulty(updateQuestionRequest.getDifficulty());
        existingQuestion.setTranscript(updateQuestionRequest.getTranscript());
        existingQuestion.setExplanation(updateQuestionRequest.getExplanation());
        
        Question updatedQuestion = questionRepository.save(existingQuestion);
        
        
        if (needsDeactivation) {
            deactivateResults(updatedQuestion.getTestId());
            setNeedUpdateStatForUsers(updatedQuestion.getTestId());
        } else if (needsStatUpdate) {
        	setNeedUpdateStatForUsers(updatedQuestion.getTestId());
        }

        return updatedQuestion;
    }
    
    private void setNeedUpdateStatForUsers(String testId) {
    	List<Result> results = resultService.getByTestId(testId);
    	Set<String> userIds = results.stream()
                .map(Result::getUserId)
                .collect(Collectors.toSet());
    	
    	List<String> userIdList = new ArrayList<>(userIds);
    	List<User> users = userService.getAllUserInIds(userIdList);
    	
    	// set needUpdateStat for each user
    	for (User user : users) {
            if (!user.getNeedUpdateStats().contains(testId)) {
                user.getNeedUpdateStats().add(testId);
            }
        }
    	
    	userService.saveAllUsers(users);
    }
    
    private void deactivateResults(String testId) {
    	List<Result> results = resultService.getByTestId(testId);
    	results.forEach(result -> result.setActive(false));
    	resultService.saveAllResult(results);
    }
    
    // check similarity between old and new content
    private boolean isSingleStringSimilar(String oldContent, String newContent) {
        if (oldContent == null && newContent == null) 
        	return true;
        if (oldContent == null || newContent == null) 
        	return false;
        JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();
        double score = similarity.apply(oldContent, newContent);
        return score >= SIMILARITY_THRESHOLD;
    }
    
    // check similarity between old list and new list
    private boolean isListSimilar(List<String> oldList, List<String> newList) {
    	if (oldList == null || newList == null) {
            return oldList == newList;
        }
        
        if (oldList.size() != newList.size()) {
            return false;
        }

        JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();
        for (int i = 0; i < oldList.size(); i++) {
            double score = similarity.apply(oldList.get(i), newList.get(i));
            if (score < SIMILARITY_THRESHOLD) {
                return false;
            }
        }
        return true;
    }
    
    public List<Question> getQuestionTopicsForTestInfo(String testId) {
    	Query query = new Query(Criteria.where("testId").is(testId));
        query.fields().include("id").include("topic").include("partNum");
        return mongoTemplate.find(query, Question.class);
    }
    
    public PaginationResponse<List<Question>> getAllQuestionForPractice(Pageable pageable, Map<String, String> filterParams) {
        List<Criteria> criteriaList = new ArrayList<>();
        
        criteriaList.add(Criteria.where("type").ne("subquestion"));

        filterParams.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                switch (key) {
                    case "DIFFICULTY":
                        criteriaList.add(Criteria.where("difficulty").is(value));
                        break;
                    case "PARTNUM":
                        criteriaList.add(Criteria.where("partNum").is(Integer.parseInt(value)));
                        break;
                    case "TOPIC":
                        criteriaList.add(Criteria.where("topic.name").is(value));
                        break;
                    default:
                        break;
                }
            }
        });

        Criteria finalCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));

        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(finalCriteria));

        int pageSize = pageable.getPageSize();
        operations.add(Aggregation.sample(pageSize));

        Aggregation aggregation = Aggregation.newAggregation(operations);
        List<Question> questions = mongoTemplate.aggregate(aggregation, "questions", Question.class).getMappedResults();

        long totalItems = questions.size();

        Page<Question> questionPage = new PageImpl<>(questions, pageable, totalItems);

        return PaginationUtils.buildPaginationResponse(pageable, questionPage);
    }

    
    public PaginationResponse<List<Question>> getAllQuestionsInTestByTestId(
    		String testId, Pageable pageable) {
    	Page<Question> questionPage = questionRepository.findByTestId(testId, pageable);
    	return PaginationUtils.buildPaginationResponse(pageable, questionPage);
    }
    
    public void importQuestions(MultipartFile file, String testId) throws IOException {
    	Workbook workbook = null;
        workbook = new XSSFWorkbook(file.getInputStream());

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String partNum = sheet.getSheetName();
            Iterator<Row> rows = sheet.iterator();
            rows.next(); // Skip header row

            Question currentGroup = null;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                try {
                    Question question = parseRowByPart(currentRow, partNum);
                    if (question != null) {
                        question.setTestId(testId);
                        question.setPartNum(Integer.parseInt(partNum));
                        question.setActive(true);

                        if ("group".equalsIgnoreCase(question.getType())) {
                            currentGroup = question;
                            questionRepository.save(currentGroup);
                        } else if (currentGroup != null && "subquestion".equalsIgnoreCase(question.getType())) {
                            question.setParentId(currentGroup.getId());
                            questionRepository.save(question);
                            currentGroup.getSubQuestions().add(question);
                            currentGroup.setQuestionNum(question.getQuestionNum());
                            questionRepository.save(currentGroup);
                        } else {
                            currentGroup = null;
                            questionRepository.save(question);
                        }
                    }
                } catch (Exception e) {
                    // Get row number for error message
                    int rowNum = currentRow.getRowNum() + 1; // +1 because row index is 0-based
                    log.error("Error importing row {} in sheet {}: {}", rowNum, partNum, e.getMessage());
                    
                    // Close workbook before throwing exception
                    if (workbook != null) {
                        workbook.close();
                    }
                    
                    // Throw formatted AppException with row number
                    String formattedMessage = String.format(ErrorCode.EXCEL_IMPORT_ERROR.getMessage(), rowNum);
                    AppException appException = new AppException(ErrorCode.EXCEL_IMPORT_ERROR, formattedMessage);
                    throw appException;
                }
            }
        }
        if (workbook != null) {
            workbook.close();
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
        Question question = new Question();
        question.setType(getCellValue(row.getCell(0)));
        question.setQuestionNum((int) getSafeNumericCellValue(row.getCell(1)));

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
        question.setDifficulty((int) getSafeNumericCellValue(row.getCell(11)));

        return question;
    }

    private Question parsePart2(Row row) {
        Question question = new Question();
        question.setType(getCellValue(row.getCell(0)));
        question.setQuestionNum((int) getSafeNumericCellValue(row.getCell(1)));
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
        question.setDifficulty((int) getSafeNumericCellValue(row.getCell(10)));
        return question;
    }

    private Question parsePart3(Row row) {
        Question question = new Question();
        question.setType(getCellValue(row.getCell(0)));
        if (!"group".equalsIgnoreCase(question.getType())) {
            question.setQuestionNum((int) getSafeNumericCellValue(row.getCell(1)));
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
        question.setDifficulty((int) getSafeNumericCellValue(row.getCell(12)));

        return question;
    }

    private Question parsePart5(Row row) {
        Question question = new Question();
        question.setType(getCellValue(row.getCell(0)));
        question.setQuestionNum((int) getSafeNumericCellValue(row.getCell(1)));
        question.setContent(getCellValue(row.getCell(2)));

        // Extract options
        List<String> answers = new ArrayList<>();
        for (int i = 3; i <= 6; i++) {
            answers.add(getCellValue(row.getCell(i)));
        }
        question.setAnswers(answers);
        question.setCorrectAnswer(getCellValue(row.getCell(7)));
        question.setExplanation(getCellValue(row.getCell(8)));
        question.setDifficulty((int) getSafeNumericCellValue(row.getCell(9)));

        return question;
       
    }

    private Question parsePart6(Row row) {
	    Question question = new Question();
	    question.setType(getCellValue(row.getCell(0)));
	    if (!"group".equalsIgnoreCase(question.getType())) {
	        question.setQuestionNum((int) getSafeNumericCellValue(row.getCell(1)));
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
	    question.setDifficulty((int) getSafeNumericCellValue(row.getCell(14)));
	
	    return question;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        try {
            // Try to get string value directly
            return cell.getStringCellValue();
        } catch (IllegalStateException e) {
            // If cell contains a numeric value or other type, convert it to string
            switch (cell.getCellType()) {
                case NUMERIC:
                    // Handle numeric values
                    double numValue = cell.getNumericCellValue();
                    // Check if it's an integer to format appropriately
                    if (numValue == Math.floor(numValue)) {
                        return String.valueOf((int) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    try {
                        return cell.getStringCellValue();
                    } catch (Exception ex) {
                        try {
                            return String.valueOf(cell.getNumericCellValue());
                        } catch (Exception exc) {
                            return cell.getCellFormula();
                        }
                    }
                case BLANK:
                    return "";
                default:
                    log.warn("Unexpected cell type: {}", cell.getCellType());
                    return "";
            }
        }
    }

    private double getSafeNumericCellValue(Cell cell) {
        if (cell == null) {
            return 0;
        }
        try {
            // Try to get numeric value directly
            return cell.getNumericCellValue();
        } catch (IllegalStateException e) {
            // If cell contains a string, try to parse it as a number
            try {
                String stringValue = cell.getStringCellValue();
                if (stringValue == null || stringValue.trim().isEmpty()) {
                    return 0;
                }
                return Double.parseDouble(stringValue.trim());
            } catch (Exception ex) {
                // If parsing fails, log the error and return default value
                log.error("Error parsing numeric value from cell: {}", ex.getMessage());
                throw new AppException(ErrorCode.CEL_IMPORT_ERROR, ex.getMessage());
            }
        }
    }

    public List<Question> getQuestionByTestId(String testId, String listPart) {
    	
    	List<Integer> listPartInt = listPart.chars()
    			.mapToObj(c -> Character.getNumericValue(c))
    			.collect(Collectors.toList());
    	
    	List<Question> questions = questionRepository.findByTestIdAndTypeIsNotSubquestion(testId, listPartInt, Sort.by(Sort.Direction.ASC, "questionNum"));
    	return questions;
    }
    
    public List<Question> getQuestionByIds(List<String> listQuestionId) {
    	return questionRepository.findByIdIn(listQuestionId);
    }
    
    public List<Question> getQuestionByIdsOptimized(List<String> listQuestionId) {
    	return questionRepository.findByIdInWithTopicIds(listQuestionId);
    }
    
    public Question getQuestionById(String questionId) {
    	Question question = questionRepository.findById(questionId)
    			.orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
    	return question;
    }
    
    public Question saveQuestion(Question question) {
    	return questionRepository.save(question);
    }
    
    public Question addTopicToQuestion(List<String> topicIds, String questionId) {
    	List<Topic> listTopicNeedAdd = topicService.getTopicByIds(topicIds);
    	Question question = getQuestionById(questionId);
    	question.setTopic(listTopicNeedAdd);
    	Question updatedQuestion = saveQuestion(question);
    	return updatedQuestion;
    }
    
    public UpdateQuestionResourceResponse updateResourceQuestion(List<Resource> res, String questionId) {
    	Question question = questionRepository
    			.findById(questionId)
    			.orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
    	question.setResources(res);
    	UpdateQuestionResourceResponse updateResourceQuestionResponse
    		= new UpdateQuestionResourceResponse();
    	updateResourceQuestionResponse.setQuestionId(questionId);
    	updateResourceQuestionResponse.setResources(res);
    	questionRepository.save(question);
    	return updateResourceQuestionResponse;
    }
}