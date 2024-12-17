package com.toeic.toeic_practice_backend.service;

import java.util.ArrayList;
import java.util.Comparator;
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
import org.springframework.transaction.annotation.Transactional;

import com.toeic.toeic_practice_backend.domain.dto.request.test.SubmitTestRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.test.TestUdateRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.test.UpdateTestStatus;
import com.toeic.toeic_practice_backend.domain.dto.request.test.SubmitTestRequest.AnswerPair;
import com.toeic.toeic_practice_backend.domain.dto.request.test.TestCreationRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.FullTestResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.GetTestCardResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.MultipleChoiceQuestion;
import com.toeic.toeic_practice_backend.domain.dto.response.test.TestInfoResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.TestResultIdResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.TestInfoResponse.ResultOverview;
import com.toeic.toeic_practice_backend.domain.dto.response.test.TestInfoResponse.TopicOverview;
import com.toeic.toeic_practice_backend.domain.entity.Category;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.domain.entity.Result;
import com.toeic.toeic_practice_backend.domain.entity.Result.UserAnswer;
import com.toeic.toeic_practice_backend.domain.entity.User.OverallStat;
import com.toeic.toeic_practice_backend.domain.entity.User.SkillStat;
import com.toeic.toeic_practice_backend.domain.entity.User.TopicStat;
import com.toeic.toeic_practice_backend.domain.entity.Test;
import com.toeic.toeic_practice_backend.domain.entity.Topic;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.mapper.QuestionMapper;
import com.toeic.toeic_practice_backend.repository.CategoryRepository;
import com.toeic.toeic_practice_backend.repository.TestRepository;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;
import com.toeic.toeic_practice_backend.utils.constants.ScoreBoard;
import com.toeic.toeic_practice_backend.utils.security.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestService {
	private final TestRepository testRepository;
	private final CategoryRepository categoryRepository;
	private final QuestionService questionService;
	private final QuestionMapper questionMapper;
	private final ResultService resultService;
	private final TopicService topicService;
	private final UserService userService;
	
	public Test addTest(TestCreationRequest testCreationRequest) {
		Optional<Test> testOptional = 
				testRepository.findByName(testCreationRequest.getName());
		Test testResponse = new Test();
		if(testOptional.isEmpty()) {
			Category category = categoryRepository
					.findById(testCreationRequest.getCategoryId())
					.orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
			Test newTest = new Test();
			newTest.setName(testCreationRequest.getName());
			newTest.setActive(true);
			newTest.setTotalQuestion(testCreationRequest.getTotalQuestion());
			newTest.setTotalScore(testCreationRequest.getTotalScore());
			newTest.setTotalUserAttempt(testCreationRequest.getTotalUserAttempt());
			newTest.setLimitTime(testCreationRequest.getLimitTime());
			newTest.setCategory(category);
			testResponse = testRepository.save(newTest);
		} else {
			throw new AppException(ErrorCode.TEST_ALREADY_EXISTS);
		}
		return testResponse;
	}
	
	public Test updateTest(TestUdateRequest testUpdateRequest, String testId) {
		Optional<Test> existingTest = testRepository.findByName(testUpdateRequest.getName());
		Test testResponse = new Test();
		if(existingTest.isEmpty() || existingTest.get().getId().equals(testId)) {
			Optional<Test> testOptional = 
					testRepository.findById(testId);
			if(!testOptional.isEmpty()) {
				Test updatedTest = testOptional.get();
				updatedTest.setName(testUpdateRequest.getName());
				updatedTest.setTotalQuestion(testUpdateRequest.getTotalQuestion());
				updatedTest.setTotalScore(testUpdateRequest.getTotalScore());
				updatedTest.setLimitTime(testUpdateRequest.getLimitTime());
				testResponse = testRepository.save(updatedTest);
			} else {
				throw new AppException(ErrorCode.TEST_NOT_FOUND);
			}
		} else {
			throw new AppException(ErrorCode.TEST_ALREADY_EXISTS);
		}
		
		return testResponse;
	}
	
	public Test updateTest(UpdateTestStatus updateTestStatus, String testId) {
		Test existingTest = testRepository.findById(testId)
				.orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));
		existingTest.setActive(updateTestStatus.isActive());
		Test newTest = testRepository.save(existingTest);
		return newTest;
	}
	
	public TestInfoResponse getTestInfo(String testId, String userId) {
	    // Record start time for the entire method
	    long startTime = System.nanoTime();

	    Test test = new Test();
	    TestInfoResponse testInfoInfoResponse = new TestInfoResponse();

	    // Time for getting questions
	    long startQuestionsTime = System.nanoTime();
	    List<Question> listQuestion = questionService.getQuestionForTestInfo(testId);
	    long endQuestionsTime = System.nanoTime();
	    long questionsQueryTime = endQuestionsTime - startQuestionsTime;
	    System.out.println("Time to fetch questions: " + questionsQueryTime / 1000000 + " ms");

	    HashMap<Integer, Set<String>> topicNamesByPartNumMap = new HashMap<>();
	    for (Question question : listQuestion) {
	        int partNum = question.getPartNum();
	        List<String> topicNames = question.getTopic()
	                .stream()
	                .map(Topic::getName)
	                .collect(Collectors.toList());
	        topicNamesByPartNumMap.putIfAbsent(partNum, new HashSet<>());
	        topicNamesByPartNumMap.get(partNum).addAll(topicNames);
	    }

	    // Time for processing topics
	    long startTopicsTime = System.nanoTime();
	    List<TopicOverview> topicsOverview = new ArrayList<>();
	    for (Map.Entry<Integer, Set<String>> entry : topicNamesByPartNumMap.entrySet()) {
	        int partNum = entry.getKey();
	        Set<String> topicNames = entry.getValue();
	        TopicOverview topicOverview = new TopicOverview();
	        topicOverview.setPartNum(partNum);
	        topicOverview.setTopicNames(new ArrayList<>(topicNames));
	        topicsOverview.add(topicOverview);
	    }
	    long endTopicsTime = System.nanoTime();
	    long topicsProcessingTime = endTopicsTime - startTopicsTime;
	    System.out.println("Time to process topics: " + topicsProcessingTime / 1000000 + " ms");

	    // Time for fetching test details
	    long startTestTime = System.nanoTime();
	    test = testRepository.findById(testId)
	            .orElseThrow(() -> new AppException(ErrorCode.TEST_NOT_FOUND));
	    long endTestTime = System.nanoTime();
	    long testQueryTime = endTestTime - startTestTime;
	    System.out.println("Time to fetch test details: " + testQueryTime / 1000000 + " ms");

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
	        // Time for fetching results
	        long startResultTime = System.nanoTime();
	        List<Result> listResult = resultService.getByTestIdAndUserId(testId, userId);
	        long endResultTime = System.nanoTime();
	        long resultQueryTime = endResultTime - startResultTime;
	        System.out.println("Time to fetch results: " + resultQueryTime / 1000000 + " ms");

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

	    // Record end time for the entire method
	    long endTime = System.nanoTime();
	    long totalMethodTime = endTime - startTime;
	    System.out.println("Total time for getTestInfo: " + totalMethodTime / 1000000 + " ms");

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
		PaginationResponse<List<Test>> response = new PaginationResponse<List<Test>>();
		Meta meta = new Meta();
		meta.setCurrent(pageable.getPageNumber()+1);
		meta.setPageSize(pageable.getPageSize());
		meta.setTotalItems(testPage.getTotalElements());
		meta.setTotalPages(testPage.getTotalPages());
		List<Test> result = testPage.getContent();
		response.setMeta(meta);
		response.setResult(result);
		return response;
	}
	
	public PaginationResponse<List<Question>> getAllQuestionsInTestByTestId(
			String testId, Pageable pageable) {
		return questionService.getAllQuestionsInTestByTestId(testId, pageable);
	}
	
	public PaginationResponse<List<Test>> getTestsByCategoryId(
			String categoryId, Pageable pageable) {
		Page<Test> testPage = testRepository.findByCategory_Id(categoryId, pageable);
		PaginationResponse<List<Test>> response = new PaginationResponse<List<Test>>();
		Meta meta = new Meta();
		meta.setCurrent(pageable.getPageNumber()+1);
		meta.setPageSize(pageable.getPageSize());
		meta.setTotalItems(testPage.getTotalElements());
		meta.setTotalPages(testPage.getTotalPages());
		List<Test> result = testPage.getContent();
		response.setMeta(meta);
		response.setResult(result);
		return response;
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
	
	private void updateTestUserAttempt(String testId) {
		Test test = getTestById(testId);
		test.setTotalUserAttempt(test.getTotalUserAttempt() + 1);
		testRepository.save(test);
	}
	
	@Transactional(rollbackFor = {Exception.class})
	public TestResultIdResponse submitTest(SubmitTestRequest submitTestRequest) {
		// Update userAttempt for test
		updateTestUserAttempt(submitTestRequest.getTestId());
		// Get user id for saving
		String email = SecurityUtils.getCurrentUserLogin()
				.orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
		System.out.println(email);
		User currentUser = userService.getUserByEmail(email)
				.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		
		// initial TopicStat
		List<TopicStat> topicStats = currentUser.getTopicStats();
		List<TopicStat> newTopicStats = new ArrayList<>();
		HashMap<String, TopicStat> topicStatMap = new HashMap<>();
		for(TopicStat topicStat : topicStats) {
			topicStatMap.put(topicStat.getTopic().getId(), topicStat);
		}
		// initial SkillStat
		List<SkillStat> skillStats = currentUser.getSkillStats();
		List<SkillStat> newSkillStats = new ArrayList<>();
		HashMap<String, SkillStat> skillStatMap = new HashMap<>();
		for(SkillStat skillStat : skillStats) {
			skillStatMap.put(skillStat.getSkill(), skillStat);
		}
		
		boolean isFulltest = submitTestRequest.getType().equals("fulltest");
		List<AnswerPair> answerPairs = submitTestRequest.getUserAnswer();

		// Extract list question id for query
		List<String> listQuestionId = new ArrayList<>();
		for(AnswerPair answerPair: answerPairs) {
			listQuestionId.add(answerPair.getQuestionId());
		}
		List<Question> questions = questionService.getQuestionByIds(listQuestionId);

		List<Topic> topics = topicService.getAllTopics();
		// hashmap for comparing answer
		HashMap<String, String> correctAnswerMap = new HashMap<>();
		HashMap<String, Integer> partNumMap = new HashMap<>();
		HashMap<String, Question> questionMap = new HashMap<>();
		HashMap<String, List<String>> topicIdMap = new HashMap<>();
		HashMap<String, List<Topic>> topicQuestionMap = new HashMap<>();
		for(Question question: questions) {
			correctAnswerMap.put(question.getId(), question.getCorrectAnswer());
			partNumMap.put(question.getId(), question.getPartNum());
			questionMap.put(question.getId(), question);
			topicIdMap.put(question.getId(), question.getTopic().stream()
					.map(Topic::getId)
					.collect(Collectors.toList()));
			topicQuestionMap.put(question.getId(), question.getTopic().stream()
					.collect(Collectors.toList()));
		}
		HashMap<String, Topic> topicMap = new HashMap<>();
		for(Topic topic: topics) {
			topicMap.put(topic.getId(), topic);
		}
		
		// Score board
		ScoreBoard scoreBoard = ScoreBoard.getInstance();
		// Member variable in result
		int totalReadingCorrect = 0;
		int totalListeningCorrect = 0;
		int totalCorrectAnswer = 0;
		int totalInCorrectAnswer = 0;
		int totalSkipAnswer = 0;
		List<UserAnswer> userAnswers = new ArrayList<>();
		for(AnswerPair answerPair: answerPairs) {
			String correctAnswer = correctAnswerMap.get(answerPair.getQuestionId());
			List<String> listTopicIds = topicIdMap.get(answerPair.getQuestionId());
			List<Topic> listTopics = topicQuestionMap.get(answerPair.getQuestionId());
			Question currentQuestion = questionMap.get(answerPair.getQuestionId());
 			boolean isCorrect = false;
			boolean isSkip = false;
			String testSkill = "";
			int partNum = partNumMap.get(answerPair.getQuestionId());
			if(partNum < 5) {
				testSkill = "listening";
			} else if(partNum > 4) {
				testSkill = "reading";
			}
			if(answerPair.getUserAnswer().equals(correctAnswer)) {
				isCorrect = true;
				totalCorrectAnswer++;
				if(testSkill.equals("reading")) {
					totalReadingCorrect++;
				} else if(testSkill.equals("listening")) {
					totalListeningCorrect++;
				}
				
			} else if(!answerPair.getUserAnswer().isEmpty()) {
				totalInCorrectAnswer++;
			} else {
				isSkip = true;
				totalSkipAnswer++;
			}
			
			// update SkillStat
			skillStatMap.putIfAbsent(testSkill, new SkillStat());
			SkillStat skillStat = skillStatMap.get(testSkill);
			if(isCorrect) {
				skillStat.setTotalCorrect(skillStat.getTotalCorrect() + 1);
			} else if(!isSkip) {
				skillStat.setTotalIncorrect(skillStat.getTotalIncorrect() + 1);
			}
			skillStat.setTotalTime(skillStat.getTotalTime() + answerPair.getTimeSpent());
			skillStat.setSkill(testSkill);
			skillStatMap.put(testSkill, skillStat);
			
			// update TopicStat
			for(String topicId : listTopicIds) {
				topicStatMap.putIfAbsent(topicId, new TopicStat());
				TopicStat stat = topicStatMap.get(topicId);
				stat.setTopic(topicMap.get(topicId));
				if(isCorrect) {
					stat.setTotalCorrect(stat.getTotalCorrect() + 1);
				} else if(!isSkip) {
					stat.setTotalIncorrect(stat.getTotalIncorrect() +  1);
				}
				
				int currentTimeCount = stat.getTimeCount();
				double avgTime = (stat.getAverageTime() * currentTimeCount + answerPair.getTimeSpent()) /(currentTimeCount + 1);
				stat.setAverageTime(avgTime);
				stat.setTimeCount(currentTimeCount + 1);
				stat.setTotalTime(stat.getTotalTime() + answerPair.getTimeSpent());
				topicStatMap.put(topicId, stat);
			}
			
			UserAnswer userAnswer = new UserAnswer();
			userAnswer.setQuestionId(answerPair.getQuestionId());
			userAnswer.setParentId(currentQuestion.getParentId());
			userAnswer.setListTopics(listTopics);
			userAnswer.setCorrect(isCorrect);
			userAnswer.setUserAnswer(answerPair.getUserAnswer());
			userAnswer.setSolution("Hãy học course này");
			userAnswer.setTimeSpent(answerPair.getTimeSpent());
			userAnswer.setQuestionNum(currentQuestion.getQuestionNum());
			userAnswer.setPartNum(currentQuestion.getPartNum());
			userAnswer.setType(currentQuestion.getType());
			userAnswer.setContent(currentQuestion.getContent());
			userAnswer.setDifficulty(currentQuestion.getDifficulty());
			userAnswer.setResources(currentQuestion.getResources());
			userAnswer.setTranscript(currentQuestion.getTranscript());
			userAnswer.setExplanation(currentQuestion.getExplanation());
			userAnswer.setAnswers(currentQuestion.getAnswers());
			userAnswer.setCorrectAnswer(currentQuestion.getCorrectAnswer());
			userAnswers.add(userAnswer);
		}
		
		// Filter subquestions
		List<UserAnswer> subUserAnswers = userAnswers.stream()
		    .filter(userAnswer -> "subquestion".equals(userAnswer.getType()))
		    .collect(Collectors.toList());
		
		// Get all group question IDs (parentIds of subquestions)
		Set<String> groupQuestionIds = subUserAnswers.stream()
		    .map(UserAnswer::getParentId)
		    .collect(Collectors.toSet());
		
		// Query all group questions by their IDs
		List<Question> groupQuestions = questionService.getQuestionByIds(new ArrayList<>(groupQuestionIds));
		
		// Map group question IDs to their corresponding questions
		HashMap<String, Question> groupQuestionMap = new HashMap<>();
		for (Question groupQuestion : groupQuestions) {
		    groupQuestionMap.put(groupQuestion.getId(), groupQuestion);
		}
		
		// Map parentId to their subquestions
		HashMap<String, List<UserAnswer>> subUserAnswerMap = new HashMap<>();
		for (UserAnswer subUserAnswer : subUserAnswers) {
		    subUserAnswerMap
		        .computeIfAbsent(subUserAnswer.getParentId(), k -> new ArrayList<>())
		        .add(subUserAnswer);
		}
		
		// Initialize UserAnswer groups with subquestions
		List<UserAnswer> groupedUserAnswers = new ArrayList<>();
		for (Question groupQuestion : groupQuestions) {
		    UserAnswer groupUserAnswer = new UserAnswer();
		    groupUserAnswer.setQuestionId(groupQuestion.getId());
		    groupUserAnswer.setParentId(null); // Group itself has no parent
		    groupUserAnswer.setType("group");
		    groupUserAnswer.setQuestionNum(groupQuestion.getQuestionNum());
		    groupUserAnswer.setPartNum(groupQuestion.getPartNum());
		    groupUserAnswer.setListTopics(groupQuestion.getTopic());
		    groupUserAnswer.setContent(groupQuestion.getContent());
		    groupUserAnswer.setDifficulty(groupQuestion.getDifficulty());
		    groupUserAnswer.setResources(groupQuestion.getResources());
		    groupUserAnswer.setTranscript(groupQuestion.getTranscript());
		    groupUserAnswer.setExplanation(groupQuestion.getExplanation());
		    groupUserAnswer.setAnswers(groupQuestion.getAnswers());
		    groupUserAnswer.setCorrectAnswer(groupQuestion.getCorrectAnswer());
		    groupUserAnswer.setSubUserAnswer(subUserAnswerMap.getOrDefault(groupQuestion.getId(), new ArrayList<>()));

		    groupedUserAnswers.add(groupUserAnswer);
		}
		
		// Add grouped user answers (with subquestions)
		userAnswers.addAll(groupedUserAnswers);
		
		// Sort by questionNum
		userAnswers.sort(Comparator.comparingInt(UserAnswer::getQuestionNum));

		Result result = Result.builder().testId(submitTestRequest.getTestId())
				.userId(currentUser.getId())
				.totalTime(submitTestRequest.getTotalSeconds())
				.totalReadingScore(scoreBoard.getReadingScore(totalReadingCorrect))
				.totalListeningScore(scoreBoard.getListeningScore(totalListeningCorrect))
				.totalCorrectAnswer(totalCorrectAnswer)
				.totalIncorrectAnswer(totalInCorrectAnswer)
				.totalSkipAnswer(totalSkipAnswer)
				.type(submitTestRequest.getType())
				.parts(submitTestRequest.getParts())
				.userAnswers(userAnswers)
				.build();
		result.setActive(true);
		Result newResult = resultService.saveResult(result);
		TestResultIdResponse testResultIdResponse = new TestResultIdResponse();
		
		// update overall stat
		OverallStat overallStat = currentUser.getOverallStat();
		if(overallStat == null) {
			overallStat = new OverallStat();
		}
		// listening, reading score
		int currentListeningCount = overallStat.getListeningScoreCount();
		int currentReadingCount = overallStat.getReadingScoreCount();
		int currentAvgListeningScore = overallStat.getAverageListeningScore();
		int currentAvgReadingScore = overallStat.getAverageReadingScore();
		int listeningScore = scoreBoard.getListeningScore(totalListeningCorrect);
		int readingScore = scoreBoard.getReadingScore(totalReadingCorrect);
		int newAvgListeningScore = 
				((currentAvgListeningScore * currentListeningCount) + listeningScore) / (currentListeningCount + 1);
		int newAvgReadingScore = 
				((currentAvgReadingScore * currentReadingCount) + readingScore) / (currentReadingCount + 1);
		overallStat.setAverageListeningScore(newAvgListeningScore);
		overallStat.setAverageReadingScore(newAvgReadingScore);
		overallStat.setListeningScoreCount(currentListeningCount + 1);
		overallStat.setReadingScoreCount(currentReadingCount + 1);
		// time
		int currentTimeCount = overallStat.getTimeCount();
		double currentAvgTime = overallStat.getAverageTime();
		double newAvgTime = 
				((currentAvgTime * currentTimeCount) + submitTestRequest.getTotalSeconds()) / (currentTimeCount + 1);
		overallStat.setTimeCount(currentTimeCount + 1);
		overallStat.setAverageTime(newAvgTime);
		
		// total
		int totalScore = listeningScore + readingScore;
		int currentTotalScoreCount = overallStat.getTotalScoreCount();
		int currentAvgTotalScore = overallStat.getAverageTotalScore();
		int newAvgTotalScore =
				((currentAvgTotalScore * currentTotalScoreCount) + totalScore) / (currentTotalScoreCount + 1);
		overallStat.setAverageTotalScore(newAvgTotalScore);
		overallStat.setTotalScoreCount(currentTotalScoreCount + 1);
		// highest
		if(totalScore > overallStat.getHighestScore()) {
			overallStat.setHighestScore(totalScore);
		}
		currentUser.setOverallStat(overallStat);
		
		// save topic stat
		topicStatMap.forEach((key, topicStat) -> {
			newTopicStats.add(topicStat);
		});
		currentUser.setTopicStats(newTopicStats);
		
		// save skill stat
		skillStatMap.forEach((key, skillStat) -> {
			newSkillStats.add(skillStat);
		});
		currentUser.setSkillStats(newSkillStats);
		userService.saveUser(currentUser);
		
		testResultIdResponse.setResultId(newResult.getId());
		
		return testResultIdResponse;
	}
	
//	public void reCalculateStats(User currentUser, List<String> listTestIds) {
//		// Lay duoc toan bo result theo userId va testId
//		String userId = currentUser.getId();
//		List<Result> listResult = resultService.getByUserIdAndListTestId(userId, listTestIds);
//		// Lay het questionId ra
//		List<String> listQuestionId = new ArrayList<>();
//		for(Result result : listResult) {
//			
//		}
//	}
}
