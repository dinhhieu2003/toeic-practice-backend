package com.toeic.toeic_practice_backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.request.test.SubmitTestRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.test.SubmitTestRequest.AnswerPair;
import com.toeic.toeic_practice_backend.domain.dto.request.test.TestCreationRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.Meta;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.FullTestResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.test.MultipleChoiceQuestion;
import com.toeic.toeic_practice_backend.domain.dto.response.test.TestResultIdResponse;
import com.toeic.toeic_practice_backend.domain.entity.Category;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.domain.entity.Result;
import com.toeic.toeic_practice_backend.domain.entity.Result.UserAnswer;
import com.toeic.toeic_practice_backend.domain.entity.Test;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.domain.entity.User.TestAttempt;
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
	private final UserService userService;
	public Test addTest(TestCreationRequest testCreationRequest) {
		Optional<Test> testOptional = 
				testRepository.findByNameAndCategory_Id(testCreationRequest.getName(), 
						testCreationRequest.getCategoryId());
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
	
	public PaginationResponse<List<Test>> getTestsByFormatAndYear(
			String format, int year, Pageable pageable) {
		Page<Test> testPage = null;
		if(year == 0) {
			testPage = testRepository
					.findByFormatOnly(format, pageable);
		} else {
			testPage = testRepository
					.findByFormatAndYear(format, year, pageable);
		}
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
	
	public FullTestResponse getQuestionTest(String testId, String listPart) {
		List<Question> questions = questionService.getQuestionByTestId(testId, listPart);
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
	
	public TestResultIdResponse submitTest(SubmitTestRequest submitTestRequest) {
		String email = SecurityUtils.getCurrentUserLogin()
				.orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));
		System.out.println(email);
		User currentUser = userService.getUserByEmail(email)
				.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		
		String parts = submitTestRequest.getParts();
		if(submitTestRequest.getType().equals("fulltest")) {
			parts = "1234567";
		}
		List<Question> questions = questionService
				.getQuestionByTestId(submitTestRequest.getTestId(), parts);
		List<AnswerPair> answerPairs = submitTestRequest.getUserAnswer();
		System.out.println(answerPairs);
		HashMap<String, String> correctAnswerMap = new HashMap<>();
		HashMap<String, Integer> partNumMap = new HashMap<>();
		for(Question question: questions) {
			correctAnswerMap.put(question.getId(), question.getCorrectAnswer());
			partNumMap.put(question.getId(), question.getPartNum());
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
			boolean isCorrect = false;
			if(answerPair.getUserAnswer().equals(correctAnswer)) {
				isCorrect = true;
				totalCorrectAnswer++;
				int partNum = partNumMap.get(answerPair.getQuestionId());
				if(partNum < 5) {
					totalListeningCorrect++;
				} else {
					totalReadingCorrect++;
				}
			} else if(!answerPair.getUserAnswer().isEmpty()) {
				totalInCorrectAnswer++;
			} else {
				totalSkipAnswer++;
			}
			UserAnswer userAnswer = new UserAnswer();
			userAnswer.setQuestionId(answerPair.getQuestionId());
			userAnswer.setCorrect(isCorrect);
			userAnswer.setAnswer(answerPair.getUserAnswer());
			userAnswer.setSolution("Hãy học course này");
			userAnswer.setTimeSpent(answerPair.getTimeSpent());
			userAnswers.add(userAnswer);
		}
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
		Result newResult = resultService.saveResult(result);
		TestResultIdResponse testResultIdResponse = new TestResultIdResponse();
//		TestAttempt testAttempt = TestAttempt.builder()
//				.testId(submitTestRequest.getTestId())
//				.totalAttempt(currentUser.getTe)
		testResultIdResponse.setResultId(newResult.getId());
		return testResultIdResponse;
	}
}
