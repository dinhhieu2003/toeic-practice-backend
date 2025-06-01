package com.toeic.toeic_practice_backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.response.lecture.LearningProgressResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.LectureCardResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.UpdateLecturePercentResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Lecture;
import com.toeic.toeic_practice_backend.domain.entity.Topic;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LectureUserService {
	private final LectureService lectureService;
	private final UserService userService;
	private final AuthService authService;
	
	public UpdateLecturePercentResponse updateLecturePercent(String lectureId, int percent) {
		String email = authService.getCurrentEmail();
		User currentUser = userService.getUserByEmail(email)
				.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
		HashMap<String, Integer> learningProgressMap = currentUser.getLearningProgress();
		learningProgressMap.put(lectureId, percent);
		userService.saveUser(currentUser);
		UpdateLecturePercentResponse response = new UpdateLecturePercentResponse();
		response.setLectureId(lectureId);
		response.setPercent(percent);
		return response;
	}
	
	public LearningProgressResponse getLearningProgress() {
		String email = authService.getCurrentEmail();
		if(email == null ) {
			throw new AppException(ErrorCode.USER_NOT_FOUND);
		}
		HashMap<String, Integer> learningProgressMap = userService.getUserLearningProgress(email);
		// Get list lecture id in learning progress
		List<String> lectureIds = new ArrayList<>(learningProgressMap.keySet());
		// Get lecture by ids
		List<Lecture> lectures = lectureService.getById(lectureIds);
		// With each lecture, get id, list topic name, percent (in hash map)
		List<LectureCardResponse> lectureCardsNotCompleted = new ArrayList<>();
		List<LectureCardResponse> lectureCardsCompleted = new ArrayList<>();
		for(Lecture lecture: lectures) {
			LectureCardResponse lectureCard = new LectureCardResponse();
			lectureCard.setId(lecture.getId());
			lectureCard.setName(lecture.getName());
			lectureCard.setTopic(lecture.getTopic()
					.stream()
					.map(Topic::getName)
					.collect(Collectors.toList()));
			int percent = learningProgressMap.get(lecture.getId());
			lectureCard.setPercent(percent);
			if(percent < 100) {
				lectureCardsNotCompleted.add(lectureCard);
			} else if(percent == 100){
				lectureCardsCompleted.add(lectureCard);
			}
		}
		LearningProgressResponse learningProgressResponse = new LearningProgressResponse();
		learningProgressResponse.setCompleted(lectureCardsCompleted);
		learningProgressResponse.setNotCompleted(lectureCardsNotCompleted);
		return learningProgressResponse;
	}
	
	public PaginationResponse<List<LectureCardResponse>> getAllLectures(Pageable pageable, Map<String, Boolean> filterParams, String search) {
		HashMap<String, Integer> learningProgressMap = new HashMap<>();
		String email = authService.getCurrentEmail();
		if(email != null ) {
			learningProgressMap = userService.getUserLearningProgress(email);
		}
		// Get all lectures 
		PaginationResponse<List<Lecture>> lecturePage = lectureService.getAllLectures(pageable, filterParams, search);
		List<Lecture> lectures = lecturePage.getResult();
		// With each lecture, get id, list topic name, percent (in hash map)
		List<LectureCardResponse> lectureCards = new ArrayList<>();
		for(Lecture lecture: lectures) {
			LectureCardResponse lectureCard = new LectureCardResponse();
			lectureCard.setId(lecture.getId());
			lectureCard.setName(lecture.getName());
			lectureCard.setTopic(lecture.getTopic()
					.stream()
					.map(Topic::getName)
					.collect(Collectors.toList()));
			lectureCard.setPercent(learningProgressMap.getOrDefault(lecture.getId(), 0));
			lectureCards.add(lectureCard);
		}
		
		return PaginationUtils
				.buildPaginationResponse(lecturePage.getMeta(), lectureCards);
	}
}
