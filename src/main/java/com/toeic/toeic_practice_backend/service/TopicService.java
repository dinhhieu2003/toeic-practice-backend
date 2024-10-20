package com.toeic.toeic_practice_backend.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.entity.Topic;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.TopicRepository;
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
}
