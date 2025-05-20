package com.toeic.toeic_practice_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.domain.entity.User.OverallStat;
import com.toeic.toeic_practice_backend.service.TestSubmissionService.CalculatedSubmissionDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalculateStatService {
	private final UserService userService;
	@Async
	public void updateUserAggregateStatistics(User currentUser, CalculatedSubmissionDetails submissionDetails,
			String testId, int totalSeconds) {
        log.info("Start update stat");
		OverallStat overallStat = currentUser.getOverallStat();
        if (overallStat == null) {
            overallStat = new OverallStat();
        }
        overallStat.updateStats(submissionDetails.getListeningScore(), submissionDetails.getReadingScore(), totalSeconds);
        currentUser.setOverallStat(overallStat);

        currentUser.setTopicStats(new ArrayList<>(submissionDetails.getUpdatedTopicStatMap().values()));

        currentUser.setSkillStats(new ArrayList<>(submissionDetails.getUpdatedSkillStatMap().values()));

        int currentAttemptScore = submissionDetails.getListeningScore() + submissionDetails.getReadingScore();
        List<User.TestAttemptStat> testHistory = currentUser.getTestHistory();
        if (testHistory == null) {
            testHistory = new ArrayList<>();
        }

        Optional<User.TestAttemptStat> existingStatOpt = testHistory.stream()
                .filter(stat -> testId.equals(stat.getTestId()))
                .findFirst();

        if (existingStatOpt.isPresent()) {
            User.TestAttemptStat existingStat = existingStatOpt.get();
            int oldAvgScore = existingStat.getAvgScore();
            int oldAttemptCount = existingStat.getAttempt();
            int newAvgScore = (oldAttemptCount > 0) ?
                              ((oldAvgScore * oldAttemptCount) + currentAttemptScore) / (oldAttemptCount + 1) :
                              currentAttemptScore;
            existingStat.setAvgScore(newAvgScore);
            existingStat.setAttempt(oldAttemptCount + 1);
        } else {
            testHistory.add(new User.TestAttemptStat(testId, currentAttemptScore, 1));
        }
        currentUser.setTestHistory(testHistory);
        userService.saveUser(currentUser);
        log.info("Update stats success");
	}
}
