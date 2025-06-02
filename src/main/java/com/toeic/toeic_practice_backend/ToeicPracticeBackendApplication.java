package com.toeic.toeic_practice_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableMongoAuditing
@SpringBootApplication
@EnableCaching
public class ToeicPracticeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ToeicPracticeBackendApplication.class, args);
	}

}
