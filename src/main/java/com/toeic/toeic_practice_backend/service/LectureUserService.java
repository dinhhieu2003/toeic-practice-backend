package com.toeic.toeic_practice_backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.response.lecture.LectureCardResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.lecture.UpdateLecturePercentResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Lecture;
import com.toeic.toeic_practice_backend.domain.entity.Topic;
import com.toeic.toeic_practice_backend.domain.entity.User;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
// Chuc nang cap nhat %
// Chuc nang lay lecture card co percent
// Chuc nang lay lecture cua nguoi dung

// test: isCompleted test, update percent lecture -> done
// test: Chuc nang lay all lecture card co percent, viet api -> done
// test: Chuc nang lay lecture card cua nguoi dung, viet api
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
	
	public List<LectureCardResponse> getLearningProgress() {
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
		List<LectureCardResponse> lectureCards = new ArrayList<>();
		for(Lecture lecture: lectures) {
			LectureCardResponse lectureCard = new LectureCardResponse();
			lectureCard.setId(lecture.getId());
			lectureCard.setName(lecture.getName());
			lectureCard.setTopic(lecture.getTopic()
					.stream()
					.map(Topic::getName)
					.collect(Collectors.toList()));
			lectureCard.setPercent(learningProgressMap.get(lecture.getId()));
			lectureCards.add(lectureCard);
		}
		return lectureCards;
	}
	
	public PaginationResponse<List<LectureCardResponse>> getAllLectures(Pageable pageable, Map<String, Boolean> filterParams, String search) {
		String email = authService.getCurrentEmail();
		if(email == null ) {
			throw new AppException(ErrorCode.USER_NOT_FOUND);
		}
		HashMap<String, Integer> learningProgressMap = userService.getUserLearningProgress(email);
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
		
		return PaginationResponse.<List<LectureCardResponse>>builder()
	            .meta(lecturePage.getMeta())
	            .result(lectureCards)
	            .build();
	}
}
