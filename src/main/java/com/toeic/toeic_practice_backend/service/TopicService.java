package com.toeic.toeic_practice_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.dto.request.topic.UpdateTopicStatusRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.pagination.PaginationResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.topic.UpdateTopicStatusResponse;
import com.toeic.toeic_practice_backend.domain.entity.Topic;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.TopicRepository;
import com.toeic.toeic_practice_backend.utils.PaginationUtils;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TopicService {
	private final TopicRepository topicRepository;
	
	public Topic addTopic(Topic topic) {
		Optional<Topic> topicOptional = topicRepository.findByName(topic.getName());
		if(topicOptional.isEmpty()) {
			return topicRepository.save(topic);
		} else {
			throw new AppException(ErrorCode.TOPIC_ALREADY_EXISTS);
		}
	}
	
	public List<Topic> getAllTopics() {
		return topicRepository.findAll();
	}
	
	public PaginationResponse<List<Topic>> getTopicPage(String search, Pageable pageable) {
		Page<Topic> topicPage = null;
		if(search.isEmpty()) {
			topicPage = topicRepository.findAll(pageable);
		} else if(!search.isEmpty()) {
			topicPage = topicRepository.findByNameContaining(search, pageable);
		}
		 
		return PaginationUtils.buildPaginationResponse(pageable, topicPage);
	}
	
	public UpdateTopicStatusResponse updateTopicStatus(String topicId, UpdateTopicStatusRequest updateTopicStatusRequest) {
		Topic existedTopic = getTopicById(topicId);
		existedTopic.setActive(updateTopicStatusRequest.isActive());
		Topic newTopic = topicRepository.save(existedTopic);
		UpdateTopicStatusResponse updateTopicStatusResponse = new UpdateTopicStatusResponse();
		updateTopicStatusResponse.setName(newTopic.getName());
		updateTopicStatusResponse.setActive(newTopic.isActive());
		return updateTopicStatusResponse;
	}
	
	public Topic getTopicById(String topicId) {
		Topic topic = topicRepository.findById(topicId)
				.orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
		return topic;
	}
	
	public List<Topic> getTopicByIds(List<String> topicIds) {
		List<Topic> listTopic = topicRepository.findByIdIn(topicIds);
		return listTopic;
	}
}
