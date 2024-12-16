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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.toeic.toeic_practice_backend.domain.dto.request.question.UpdateQuestionRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.question.UpdateResourceQuestionResponse;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.domain.entity.Question.Resource;
import com.toeic.toeic_practice_backend.domain.entity.Result;
import com.toeic.toeic_practice_backend.domain.entity.Topic;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.mapper.QuestionMapper;
import com.toeic.toeic_practice_backend.repository.QuestionRepository;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = {Exception.class})
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final TopicService topicService;
    private final ResultService resultService;
    private final UserService userService;
    private final QuestionMapper questionMapper;
    private final MongoTemplate mongoTemplate;
    @Value("${azure.url-resources}")
    private String urlResource;
    private static final double SIMILARITY_THRESHOLD = 0.9;
    
    public Question updateQuestion(UpdateQuestionRequest updateQuestionRequest) {
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
    
    public List<Question> getQuestionForTestInfo(String testId) {
    	Query query = new Query(Criteria.where("testId").is(testId));
        query.fields().include("id").include("topic").include("partNum");
        return mongoTemplate.find(query, Question.class);
    }
    
    public PaginationResponse<List<Question>> getAllQuestion(Pageable pageable, Map<String, String> filterParams) {
        Query query = new Query();

        // Thêm điều kiện để loại bỏ các câu hỏi có type là "subquestion"
        query.addCriteria(Criteria.where("type").ne("subquestion"));

        filterParams.forEach((key, value) -> {
            if(value != null && !value.isEmpty()) {
            	System.out.println(value);
                switch (key) {
                    case "DIFFICULTY":
                        query.addCriteria(Criteria.where("difficulty").is(value));
                        break;
                    case "PARTNUM":
                        query.addCriteria(Criteria.where("partNum").is(Integer.parseInt(value)));
                        break;
                    case "TOPIC":
                        query.addCriteria(Criteria.where("topic.name").is(value));
                        break;
                    default:
                        break;
                }
            }
        });

        String orderAscBy = filterParams.get("ORDER_ASC_BY");
        String orderDescBy = filterParams.get("ORDER_DESC_BY");

        if (orderAscBy != null && !orderAscBy.isEmpty()) {
            query.with(Sort.by(Sort.Direction.ASC, orderAscBy));
        } else if (orderDescBy != null && !orderDescBy.isEmpty()) {
            query.with(Sort.by(Sort.Direction.DESC, orderDescBy));
        }

        query.with(pageable);
        
        List<Question> questions = mongoTemplate.find(query, Question.class);

        long totalItems = mongoTemplate.count(query.skip(0).limit(0), Question.class);

        Page<Question> questionPage = new PageImpl<>(questions, pageable, totalItems);

        return PaginationResponse.<List<Question>>builder()
            .meta(
                Meta.builder()
                    .current(pageable.getPageNumber() + 1)
                    .pageSize(pageable.getPageSize())
                    .totalItems(questionPage.getTotalElements())
                    .totalPages(questionPage.getTotalPages())
                    .build()
            )
            .result(questionPage.getContent())
            .build();
    }
    
    public PaginationResponse<List<Question>> getAllQuestionsInTestByTestId(
    		String testId, Pageable pageable) {
    	Page<Question> questionPage = questionRepository.findByTestId(testId, pageable);
    	return PaginationResponse.<List<Question>>builder()
                .meta(
                    Meta.builder()
                        .current(pageable.getPageNumber() + 1)
                        .pageSize(pageable.getPageSize())
                        .totalItems(questionPage.getTotalElements())
                        .totalPages(questionPage.getTotalPages())
                        .build()
                )
                .result(questionPage.getContent())
                .build();
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
    }

    private Question parsePart2(Row row) {
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
    }

    private Question parsePart3(Row row) {
        Question question = new Question();
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
    }

    private Question parsePart5(Row row) {
        Question question = new Question();
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
       
    }

    private Question parsePart6(Row row) {
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
    
    public List<Question> getQuestionByIds(List<String> listQuestionId) {
    	return questionRepository.findByIdIn(listQuestionId);
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
    	return saveQuestion(question);
    }
    
    public UpdateResourceQuestionResponse updateResourceQuestion(List<Resource> res, String questionId) {
    	Question question = questionRepository
    			.findById(questionId)
    			.orElseThrow(() -> new AppException(ErrorCode.QUESTION_NOT_FOUND));
    	question.setResources(res);
    	UpdateResourceQuestionResponse updateResourceQuestionResponse
    		= new UpdateResourceQuestionResponse();
    	updateResourceQuestionResponse.setQuestionId(questionId);
    	updateResourceQuestionResponse.setResources(res);
    	questionRepository.save(question);
    	return updateResourceQuestionResponse;
    }
}
