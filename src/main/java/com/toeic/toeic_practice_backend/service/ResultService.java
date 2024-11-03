package com.toeic.toeic_practice_backend.service;

import org.springframework.stereotype.Service;

import com.toeic.toeic_practice_backend.domain.entity.Result;
import com.toeic.toeic_practice_backend.repository.ResultRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResultService {
	private final ResultRepository resultRepository;
	public Result saveResult(Result result) {
		return resultRepository.save(result);
	}
}
