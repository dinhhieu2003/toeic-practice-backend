package com.toeic.toeic_practice_backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.request.test.UpdateTestRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.test.UpdateTestStatusRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.test.CreateTestRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.FullTestResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.MultipleChoiceQuestion;
import com.toeic.toeic_practice_backend.domain.dto.response.test.TestInfoResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.TestInfoResponse.ResultOverview;
import com.toeic.toeic_practice_backend.domain.dto.response.test.TestInfoResponse.TopicOverview;
import com.toeic.toeic_practice_backend.domain.entity.Category;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.domain.entity.Result;
import com.toeic.toeic_practice_backend.domain.entity.Test;
import com.toeic.toeic_practice_backend.domain.entity.Topic;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.mapper.QuestionMapper;
import com.toeic.toeic_practice_backend.repository.CategoryRepository;
import com.toeic.toeic_practice_backend.repository.TestRepository;
import com.toeic.toeic_practice_backend.repository.projectionInterface.TestNameOnly;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {
	private final TestRepository testRepository;
	private final CategoryRepository categoryRepository;
	private final QuestionService questionService;
	private final QuestionMapper questionMapper;
	private final ResultService resultService;
	private final UserService userService;
	private final AuthService authService;
	
	public String getTestName(String testId) {
		TestNameOnly testName = testRepository.findTestNameByTestId(testId)
				.orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));
		return testName.getName();
	}
	
	public Test addTest(CreateTestRequest testCreationRequest) {
		log.info("Start: Add new test");
		Optional<Test> testOptional = 
				testRepository.findByName(testCreationRequest.getName());
		Test testResponse = new Test();
		if(testOptional.isPresent()) {
			log.error("Error: This test is already exists");
			throw new AppException(ErrorCode.TEST_ALREADY_EXISTS);
		}
		Category category = categoryRepository
				.findById(testCreationRequest.getCategoryId())
				.orElseThrow(() -> {
					log.error("Error: Category not found when adding test");
					return new AppException(ErrorCode.CATEGORY_NOT_FOUND);
				});
		Test newTest = new Test();
		newTest.setName(testCreationRequest.getName());
		newTest.setActive(true);
		newTest.setTotalQuestion(testCreationRequest.getTotalQuestion());
		newTest.setTotalScore(testCreationRequest.getTotalScore());
		newTest.setTotalUserAttempt(testCreationRequest.getTotalUserAttempt());
		newTest.setLimitTime(testCreationRequest.getLimitTime());
		newTest.setDifficulty(testCreationRequest.getDifficulty());
		newTest.setCategory(category);
		
		testResponse = testRepository.save(newTest);
		log.info("End: Add new test success");
		return testResponse;
	}
	
	public Test updateTest(UpdateTestRequest testUpdateRequest, String testId) {
		log.info("Start: Update test with id {}", testId);
		Optional<Test> existingTest = testRepository.findByName(testUpdateRequest.getName());
		Test testResponse = new Test();
		if(existingTest.isPresent() && !existingTest.get().getId().equals(testId)) {
			log.error("Error: Test name is already exists in other test");
			throw new AppException(ErrorCode.TEST_ALREADY_EXISTS);
		}
		
		Optional<Test> testOptional = testRepository.findById(testId);
		if(testOptional.isEmpty()) {
			log.error("Error: Test not found with id {}", testId);
			throw new AppException(ErrorCode.TEST_NOT_FOUND);	
		}
		
		Test updatedTest = testOptional.get();
		updatedTest.setName(testUpdateRequest.getName());
		updatedTest.setTotalQuestion(testUpdateRequest.getTotalQuestion());
		updatedTest.setTotalScore(testUpdateRequest.getTotalScore());
		updatedTest.setLimitTime(testUpdateRequest.getLimitTime());
		updatedTest.setDifficulty(testUpdateRequest.getDifficulty());
		
		testResponse = testRepository.save(updatedTest);
		log.info("End: Update test with id {} success", testId);
		return testResponse;
	}
	
	public Test updateTest(UpdateTestStatusRequest updateTestStatus, String testId) {
		log.info("Start: Update status test with id {}", testId);
		Test existingTest = testRepository.findById(testId)
				.orElseThrow(() -> {
					log.error("Error: Test not found with id {}", testId);
					return new AppException(ErrorCode.TEST_NOT_FOUND);
				});
		existingTest.setActive(updateTestStatus.isActive());
		Test newTest = testRepository.save(existingTest);
		log.info("Start: Update status test with id {} success", testId);
		return newTest;
	}
	
	public TestInfoResponse getTestInfo(String testId) {
		String email = authService.getCurrentEmail();
	    Optional<User> userOptional = userService.getUserByEmailWithoutStat(email);
	    String userId = null;
	    if (userOptional.isPresent()) {
	    	userId = userOptional.get().getId();
	    }
	    
	    Test test = new Test();
	    TestInfoResponse testInfoInfoResponse = new TestInfoResponse();

	    List<Question> listQuestionTopics = questionService.getQuestionTopicsForTestInfo(testId);
	    System.out.println(listQuestionTopics.size());
	    HashMap<Integer, Set<String>> topicNamesByPartNumMap = new HashMap<>();
	    for (Question question : listQuestionTopics) {
	        int partNum = question.getPartNum();
	        List<String> topicNames = question.getTopic()
	                .stream()
	                .map(Topic::getName)
	                .collect(Collectors.toList());
	        topicNamesByPartNumMap.putIfAbsent(partNum, new HashSet<>());
	        topicNamesByPartNumMap.get(partNum).addAll(topicNames);
	    }

	    List<TopicOverview> topicsOverview = new ArrayList<>();
	    for (Map.Entry<Integer, Set<String>> entry : topicNamesByPartNumMap.entrySet()) {
	        int partNum = entry.getKey();
	        Set<String> topicNames = entry.getValue();
	        TopicOverview topicOverview = new TopicOverview();
	        topicOverview.setPartNum(partNum);
	        topicOverview.setTopicNames(new ArrayList<>(topicNames));
	        topicsOverview.add(topicOverview);
	    }
	 
	    test = testRepository.findById(testId)
	            .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));
	    
	    // Construct TestInfoResponse
	    testInfoInfoResponse = TestInfoResponse
	            .builder()
	            .id(testId)
	            .name(test.getName())
	            .totalUserAttempt(test.getTotalUserAttempt())
	            .totalQuestion(test.getTotalQuestion())
	            .totalScore(test.getTotalScore())
	            .limitTime(test.getLimitTime())
	            .topicsOverview(topicsOverview)
	            .build();

	    List<ResultOverview> listResultOverview = new ArrayList<>();
	    if (userId != null) {
	        List<Result> listResult = resultService.getByTestIdAndUserId(testId, userId);

	        for (Result result : listResult) {
	            int totalQuestion = result.getTotalCorrectAnswer() + result.getTotalIncorrectAnswer() + result.getTotalSkipAnswer();
	            String score = result.getTotalCorrectAnswer() + "/" + totalQuestion;
	            ResultOverview resultOverview = ResultOverview
	                    .builder()
	                    .createdAt(result.getCreatedAt())
	                    .resultId(result.getId())
	                    .result(score)
	                    .totalTime(result.getTotalTime())
	                    .totalReadingScore(result.getTotalReadingScore())
	                    .totalListeningScore(result.getTotalListeningScore())
	                    .totalCorrectAnswer(result.getTotalCorrectAnswer())
	                    .totalIncorrectAnswer(result.getTotalIncorrectAnswer())
	                    .totalSkipAnswer(result.getTotalSkipAnswer())
	                    .type(result.getType())
	                    .parts(result.getParts())
	                    .build();
	            listResultOverview.add(resultOverview);
	        }
	        testInfoInfoResponse.setResultsOverview(listResultOverview);
	    }

	    return testInfoInfoResponse;
	}

	public Test getTestById(String testId) {
		Test test = testRepository.findById(testId)
				.orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));
		return test;
	}
	
	public List<Test> getTestByIdIn(List<String> testIds) {
		return testRepository.findTestByIdIn(testIds);
	}
	
	public PaginationResponse<List<Test>> getAllTest(Pageable pageable) {
		Page<Test> testPage = testRepository.findAll(pageable);
		return PaginationUtils.buildPaginationResponse(pageable, testPage);
	}
	
	public PaginationResponse<List<Question>> getAllQuestionsInTestByTestId(
			String testId, Pageable pageable) {
		return questionService.getAllQuestionsInTestByTestId(testId, pageable);
	}
	
	public PaginationResponse<List<Test>> getTestsByCategoryId(String search,
			String categoryId, Pageable pageable) {
		log.info("Start: Function get test by category id");
		Page<Test> testPage = null;
		if(search.isEmpty()) {
			log.info("Search term is empty - finding all tests by cate_id in database");
			testPage = testRepository.findByCategory_Id(categoryId, pageable);
		} else if(!search.isEmpty()) {
			log.info("Finding all tests by cate_id by search term");
			testPage = testRepository.findByTestNameContaining(search, categoryId, pageable);
		}
		log.info("End: Function get test by category id");
		return PaginationUtils.buildPaginationResponse(pageable, testPage);
	}
	
	public Page<Test> getTestsByCategoryId(List<String> listCategoryId, Pageable pageable) {
		return testRepository.findByCategory_IdIn(listCategoryId, pageable);
	}
	
	public Page<Test> getTestsByFormatAndYear(String format, int year, Pageable pageable) {
		return testRepository.findByFormatAndYear(format, year, pageable);
	}
	
	public FullTestResponse getQuestionTest(String testId, String listPart) {
	    // Query execution
	    long queryStartTime = System.currentTimeMillis();
		List<Question> questions = questionService.getQuestionByTestId(testId, listPart);
		
		long queryEndTime = System.currentTimeMillis(); // End time for query
	    System.out.println("Query Execution Time: " + (queryEndTime - queryStartTime) + " ms");
	    
		List<MultipleChoiceQuestion> multipleChoiceQuestions = questionMapper
                .toListMultipleChoiceQuestionFromListQuestion(questions);
    	int totalQuestion = 0;
    	for(MultipleChoiceQuestion question: multipleChoiceQuestions) {
    		if(question.getType().equals("group")) {
    			totalQuestion += question.getSubQuestions().size();
    		} else if(question.getType().equals("single")) {
    			totalQuestion++;
    		}
    	}
    	FullTestResponse fullTestResponse = new FullTestResponse();
    	fullTestResponse.setListMultipleChoiceQuestions(multipleChoiceQuestions);
    	fullTestResponse.setTotalQuestion(totalQuestion);
		return fullTestResponse;
	}
}