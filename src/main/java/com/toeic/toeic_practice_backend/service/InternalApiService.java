package com.toeic.toeic_practice_backend.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.internal.LectureCandidateDTO;
import com.toeic.toeic_practice_backend.domain.dto.internal.SkillStatInternalDTO;
import com.toeic.toeic_practice_backend.domain.dto.internal.TestAttemptStatDTO;
import com.toeic.toeic_practice_backend.domain.dto.internal.TestCandidateDTO;
import com.toeic.toeic_practice_backend.domain.dto.internal.TopicStatInternalDTO;
import com.toeic.toeic_practice_backend.domain.dto.internal.UserProfileInternalDTO;
import com.toeic.toeic_practice_backend.domain.dto.internal.UserSimilarityProfileDTO;
import com.toeic.toeic_practice_backend.domain.entity.Lecture;
import com.toeic.toeic_practice_backend.domain.entity.Question;
import com.toeic.toeic_practice_backend.domain.entity.Test;
import com.toeic.toeic_practice_backend.domain.entity.Topic;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.domain.entity.User.OverallStat;
import com.toeic.toeic_practice_backend.domain.entity.User.SkillStat;
import com.toeic.toeic_practice_backend.domain.entity.User.TestAttemptStat;
import com.toeic.toeic_practice_backend.domain.entity.User.TopicStat;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.LectureRepository;
import com.toeic.toeic_practice_backend.repository.TestRepository;
import com.toeic.toeic_practice_backend.repository.UserRepository;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalApiService {

    private final UserRepository userRepository;
    private final TestRepository testRepository;
    private final LectureRepository lectureRepository;
    private final QuestionService questionService;

    /**
     * Retrieves a comprehensive user profile with all statistics for the recommender system
     *
     * @param userId The ID of the user to retrieve
     * @return A DTO containing all user information needed by the recommender system
     * @throws AppException if the user is not found
     */
    public UserProfileInternalDTO getUserProfile(String userId) {
        log.info("Fetching user profile for recommendation, userId: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Map topic stats
        List<TopicStatInternalDTO> topicStats = user.getTopicStats().stream()
                .map(this::mapToTopicStatDTO)
                .collect(Collectors.toList());
        
        // Map skill stats
        List<SkillStatInternalDTO> skillStats = user.getSkillStats().stream()
                .map(this::mapToSkillStatDTO)
                .collect(Collectors.toList());
        
        // Map test history using the new TestAttemptStat structure
        List<TestAttemptStatDTO> testHistory = user.getTestHistory().stream()
                .map(this::mapToTestAttemptStatDTO)
                .collect(Collectors.toList());
        
        // Get overall stats
        OverallStat overallStat = user.getOverallStat();
        
        return new UserProfileInternalDTO(
                user.getId(),
                user.getTarget(),
                overallStat.getAverageListeningScore(),
                overallStat.getAverageReadingScore(),
                overallStat.getAverageTotalScore(),
                overallStat.getHighestScore(),
                topicStats,
                skillStats,
                user.getLearningProgress(),
                testHistory
        );
    }

    /**
     * Retrieves a list of all user profiles optimized for similarity calculations
     *
     * @return A list of streamlined user profiles for similarity calculations
     */
    public List<UserSimilarityProfileDTO> getAllUserProfilesForSimilarity() {
        log.info("Fetching all user profiles for similarity calculations");
        
        List<User> users = userRepository.findAllActiveUsers();
        
        return users.stream()
                .map(this::mapToUserSimilarityProfileDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of all test candidates for recommendation
     *
     * @return A list of test candidates with their difficulty and topics
     */
    public List<TestCandidateDTO> getTestCandidates() {
        log.info("Fetching all active test candidates for recommendation");
        
        List<Test> tests = testRepository.findByIsActiveTrue();
        
        return tests.stream()
                .map(this::mapToTestCandidateDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a list of all lecture candidates for recommendation
     *
     * @return A list of lecture candidates with their topics and creation date
     */
    public List<LectureCandidateDTO> getLectureCandidates() {
        log.info("Fetching all active lecture candidates for recommendation");
        
        List<Lecture> lectures = lectureRepository.findByIsActiveTrue();
        
        return lectures.stream()
                .map(this::mapToLectureCandidateDTO)
                .collect(Collectors.toList());
    }

    // Mapping helper methods
    
    private TopicStatInternalDTO mapToTopicStatDTO(TopicStat topicStat) {
        return new TopicStatInternalDTO(
                topicStat.getTopic().getName(),
                topicStat.getTotalCorrect(),
                topicStat.getTotalIncorrect()
        );
    }
    
    private SkillStatInternalDTO mapToSkillStatDTO(SkillStat skillStat) {
        return new SkillStatInternalDTO(
                skillStat.getSkill(),
                skillStat.getTotalCorrect(),
                skillStat.getTotalIncorrect()
        );
    }
    
    private TestAttemptStatDTO mapToTestAttemptStatDTO(TestAttemptStat testAttemptStat) {
        return new TestAttemptStatDTO(
                testAttemptStat.getTestId(),
                testAttemptStat.getAvgScore(),
                testAttemptStat.getAttempt()
        );
    }
    
    private UserSimilarityProfileDTO mapToUserSimilarityProfileDTO(User user) {
        OverallStat overallStat = user.getOverallStat();
        
        List<TestAttemptStatDTO> testHistory = user.getTestHistory().stream()
                .map(this::mapToTestAttemptStatDTO)
                .collect(Collectors.toList());
        
        return new UserSimilarityProfileDTO(
                user.getId(),
                user.getTarget(),
                overallStat.getAverageListeningScore(),
                overallStat.getAverageReadingScore(),
                overallStat.getAverageTotalScore(),
                testHistory,
                user.getLearningProgress()
        );
    }
    
    private TestCandidateDTO mapToTestCandidateDTO(Test test) {
    	List<Question> questionsWithTopicOnly = questionService.getQuestionTopicsForTestInfo(test.getId());
    	HashSet<Topic> topics = new HashSet<>();
    	for(Question question: questionsWithTopicOnly) {
    		topics.addAll(question.getTopic());
    	}
    	
    	List<String> topicNames = topics.stream()
    			.map(Topic::getName)
    			.collect(Collectors.toList());
        
        return new TestCandidateDTO(
                test.getId(),
                test.getName(),
                test.getDifficulty(),
                topicNames,
                test.getTotalUserAttempt()
        );
    }
    
    private LectureCandidateDTO mapToLectureCandidateDTO(Lecture lecture) {
        List<Topic> topics = lecture.getTopic();
        List<String> topicNames = topics.stream()
        		.map(Topic::getName)
        		.collect(Collectors.toList());
         
        return new LectureCandidateDTO(
                lecture.getId(),
                lecture.getName(),
                topicNames,
                lecture.getCreatedAt()
        );
    }
} 