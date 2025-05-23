package com.toeic.toeic_practice_backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.toeic.toeic_practice_backend.domain.dto.request.test.SubmitTestRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.test.SubmitTestRequest.AnswerPair;
import com.toeic.toeic_practice_backend.domain.dto.response.test.TestResultIdResponse;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.domain.entity.Result;
import com.toeic.toeic_practice_backend.domain.entity.Test;
import com.toeic.toeic_practice_backend.domain.entity.Topic;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.domain.entity.Result.UserAnswer;
import com.toeic.toeic_practice_backend.domain.entity.User.OverallStat;
import com.toeic.toeic_practice_backend.domain.entity.User.SkillStat;
import com.toeic.toeic_practice_backend.domain.entity.User.TopicStat;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.TestRepository;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;
import com.toeic.toeic_practice_backend.utils.constants.ScoreBoard;
import com.toeic.toeic_practice_backend.utils.security.SecurityUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TestSubmissionService {
	private final TestRepository testRepository;
	private final QuestionService questionService;
	private final ResultService resultService;
	private final TopicService topicService;
	private final UserService userService;
	private final TestService testService;
	private final CalculateStatService calculateStatService;
	
	@Transactional(rollbackFor = {Exception.class})
	public TestResultIdResponse submitTest(SubmitTestRequest submitTestRequest) {
		// Step 1: Get user logging in for updating stat
		String email = SecurityUtils.getCurrentUserLogin()
				.orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
		User currentUser = userService.getUserByEmail(email)
				.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		
		// Step 2: Update userAttempt (if type is fulltest)
		String testId = submitTestRequest.getTestId();
		String type = submitTestRequest.getType();
		if(testId != null && !testId.isEmpty() && !testId.isBlank() && type.equals("fulltest")) {
			updateTestUserAttempt(submitTestRequest.getTestId());
		}
		
		// Step 3: Prepare data for calculate stats
		PreparedData preparedData = prepareDataForSubmission(submitTestRequest, currentUser);
	
		// Step 4: Calculate score and update stats temporarily
		CalculatedSubmissionDetails submissionDetails = calculateScoresAndProcessAnswers(
                submitTestRequest.getUserAnswer(),
                preparedData,
                type
        );
		
		// Step 5: Build and save result
		Result newResult = buildAndSaveTestResult(
                submitTestRequest,
                currentUser.getId(),
                submissionDetails
        );
		
		// Step 6: Save stat if type is fulltest async
		if(type.equals("fulltest")) {
			calculateStatService.updateUserAggregateStatistics(
	                currentUser,
	                submissionDetails,
	                submitTestRequest.getTestId(),
	                submitTestRequest.getTotalSeconds()
	        );
		}
			
		// Step 7: Build and return response
		TestResultIdResponse testResultIdResponse = new TestResultIdResponse();
        testResultIdResponse.setResultId(newResult.getId());
        return testResultIdResponse;
		
	}
	
	private Result buildAndSaveTestResult(SubmitTestRequest submitTestRequest, String userId,
			CalculatedSubmissionDetails submissionDetails) {
		Result result = Result.builder()
                .testId(submitTestRequest.getTestId())
                .userId(userId)
                .totalTime(submitTestRequest.getTotalSeconds())
                .totalReadingScore(submissionDetails.readingScore)
                .totalListeningScore(submissionDetails.listeningScore)
                .totalCorrectAnswer(submissionDetails.totalCorrectAnswers)
                .totalIncorrectAnswer(submissionDetails.totalIncorrectAnswers)
                .totalSkipAnswer(submissionDetails.totalSkippedAnswers)
                .type(submitTestRequest.getType())
                .parts(submitTestRequest.getParts())
                .userAnswers(submissionDetails.userAnswers) // Danh sách UserAnswer đã được xử lý subquestions
                .build();
        result.setActive(true);
        return resultService.saveResult(result);
	}

	

	private CalculatedSubmissionDetails calculateScoresAndProcessAnswers(List<AnswerPair> answerPairs,
			PreparedData preparedData, String type) {
		Map<String, TopicStat> currentTopicStatMap = new HashMap<>(preparedData.initialTopicStatMap);
        Map<String, SkillStat> currentSkillStatMap = new HashMap<>(preparedData.initialSkillStatMap);

        int totalReadingCorrect = 0;
        int totalListeningCorrect = 0;
        int totalCorrectAnswers = 0;
        int totalIncorrectAnswers = 0;
        int totalSkippedAnswers = 0;
        List<UserAnswer> userAnswersList = new ArrayList<>();
        
        int questionNumForExcercise = 1;
        for (AnswerPair answerPair : answerPairs) {
            String questionId = answerPair.getQuestionId();
            Question currentQuestion = preparedData.questionMap.get(questionId);
            if (currentQuestion == null) continue; // Bỏ qua nếu không tìm thấy câu hỏi

            String correctAnswer = preparedData.correctAnswerMap.get(questionId);
            List<String> listTopicIds = preparedData.questionTopicIdsMap.get(questionId);
            List<Topic> listTopics = preparedData.questionFullyLoadedTopicsMap.get(questionId);

            boolean isCorrect = false;
            boolean isSkipped = false;
            String userAnswerText = answerPair.getUserAnswer();
            int partNum = preparedData.partNumMap.getOrDefault(questionId, 0); // Mặc định partNum nếu không có
            String testSkill = determineTestSkill(partNum);

            if (userAnswerText != null && userAnswerText.equals(correctAnswer)) {
                isCorrect = true;
                totalCorrectAnswers++;
                if ("reading".equals(testSkill)) {
                    totalReadingCorrect++;
                } else if ("listening".equals(testSkill)) {
                    totalListeningCorrect++;
                }
            } else if (userAnswerText != null && !userAnswerText.isEmpty()) {
                totalIncorrectAnswers++;
            } else {
                isSkipped = true;
                totalSkippedAnswers++;
            }

            // Cập nhật SkillStat
            SkillStat skillStat = currentSkillStatMap.computeIfAbsent(testSkill, k -> new SkillStat(k, 0, 0, 0));
            skillStat.updateStats(isCorrect, isSkipped, answerPair.getTimeSpent());
            
            // Cập nhật TopicStat
            if (listTopicIds != null) {
                for (String topicId : listTopicIds) {
                    Topic topic = preparedData.allTopicsMap.get(topicId);
                    if (topic != null) { // Đảm bảo topic tồn tại trong map
                        TopicStat stat = currentTopicStatMap.computeIfAbsent(topicId, k -> new TopicStat(topic, 0, 0, 0, 0, 0));
                        stat.updateStats(isCorrect, isSkipped, answerPair.getTimeSpent());
                    }
                }
            }
            if(type.equals("exercise")) {
            	currentQuestion.setQuestionNum(questionNumForExcercise);
            	questionNumForExcercise++;
            }
            userAnswersList.add(createUserAnswerEntity(currentQuestion, answerPair, listTopics, isCorrect));
        }

        ScoreBoard scoreBoard = ScoreBoard.getInstance();
        List<UserAnswer> finalUserAnswers = processAndGroupUserAnswers(userAnswersList, preparedData.questions);

        int listeningScore = scoreBoard.getListeningScore(totalListeningCorrect);
        int readingScore = scoreBoard.getReadingScore(totalReadingCorrect);

        return new CalculatedSubmissionDetails(
                totalReadingCorrect, totalListeningCorrect, totalCorrectAnswers,
                totalIncorrectAnswers, totalSkippedAnswers, finalUserAnswers,
                currentTopicStatMap, currentSkillStatMap, listeningScore, readingScore
        );
	}

	private List<UserAnswer> processAndGroupUserAnswers(List<UserAnswer> flatUserAnswers, List<Question> allFetchedQuestions) {
		// Filter subquestions
		List<UserAnswer> subUserAnswers = flatUserAnswers.stream()
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
		    UserAnswer groupUserAnswer = createGroupUserAnswerEntity(groupQuestion, subUserAnswerMap);
		    groupedUserAnswers.add(groupUserAnswer);
		}
		
		flatUserAnswers.addAll(groupedUserAnswers);
		// Sort by questionNum
		flatUserAnswers.sort(Comparator.comparingInt(UserAnswer::getQuestionNum));
		return flatUserAnswers;
		
    }

	private String determineTestSkill(int partNum) {
        if (partNum == 0) return "unknown";
        return (partNum < 5) ? "listening" : "reading";
    }

	private PreparedData prepareDataForSubmission(SubmitTestRequest submitTestRequest, User currentUser) {
		List<String> questionIds = submitTestRequest.getUserAnswer().stream()
                .map(AnswerPair::getQuestionId)
                .collect(Collectors.toList());

        List<Question> questions = questionService.getQuestionByIdsOptimized(questionIds);
        Map<String, Topic> allTopicsMap = topicService.getAllTopics().stream()
                .collect(Collectors.toMap(Topic::getId, topic -> topic));

        Map<String, String> correctAnswerMap = new HashMap<>();
        Map<String, Integer> partNumMap = new HashMap<>();
        Map<String, Question> questionMap = new HashMap<>();
        Map<String, List<String>> questionTopicIdsMap = new HashMap<>();
        Map<String, List<Topic>> questionFullyLoadedTopicsMap = new HashMap<>();

        for (Question question : questions) {
            correctAnswerMap.put(question.getId(), question.getCorrectAnswer());
            partNumMap.put(question.getId(), question.getPartNum());
            questionMap.put(question.getId(), question);

            List<String> topicIds = question.getTopic().stream()
                    .map(Topic::getId)
                    .collect(Collectors.toList());
            questionTopicIdsMap.put(question.getId(), topicIds);

            List<Topic> fullyLoadedTopics = topicIds.stream()
                    .map(allTopicsMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            questionFullyLoadedTopicsMap.put(question.getId(), fullyLoadedTopics);
        }

        Map<String, TopicStat> initialTopicStatMap = currentUser.getTopicStats().stream()
                .collect(Collectors.toMap(stat -> stat.getTopic().getId(), stat -> stat));
        Map<String, SkillStat> initialSkillStatMap = currentUser.getSkillStats().stream()
                .collect(Collectors.toMap(SkillStat::getSkill, stat -> stat));

        return new PreparedData(
                questions,
                allTopicsMap,
                correctAnswerMap,
                partNumMap,
                questionMap,
                questionTopicIdsMap,
                questionFullyLoadedTopicsMap,
                initialTopicStatMap,
                initialSkillStatMap
        );
	}

	private UserAnswer createGroupUserAnswerEntity(Question groupQuestion, HashMap<String, List<UserAnswer>> subUserAnswerMap) {
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
	    return groupUserAnswer;
	}
	
	private UserAnswer createUserAnswerEntity(Question currentQuestion, AnswerPair answerPair, List<Topic> listTopics, boolean isCorrect) {
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
		return userAnswer;
	}
	
	private void updateTestUserAttempt(String testId) {
		Test test = testService.getTestById(testId);
		test.setTotalUserAttempt(test.getTotalUserAttempt() + 1);
		testRepository.save(test);
	}
	
	
	// Helper class
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	private static class PreparedData {
        private List<Question> questions;
        private Map<String, Topic> allTopicsMap;
        private Map<String, String> correctAnswerMap;
        private Map<String, Integer> partNumMap;
        private Map<String, Question> questionMap;
        private Map<String, List<String>> questionTopicIdsMap;
        private Map<String, List<Topic>> questionFullyLoadedTopicsMap;
        private Map<String, TopicStat> initialTopicStatMap;
        private Map<String, SkillStat> initialSkillStatMap;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CalculatedSubmissionDetails{
		private int totalReadingCorrect;
		private int totalListeningCorrect;
		private int totalCorrectAnswers;
		private int totalIncorrectAnswers;
		private int totalSkippedAnswers;
		private List<UserAnswer> userAnswers; 
		private Map<String, TopicStat> updatedTopicStatMap;
		private Map<String, SkillStat> updatedSkillStatMap;
		private int listeningScore;
		private int readingScore;
	}
}
