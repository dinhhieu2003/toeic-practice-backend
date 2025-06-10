package com.toeic.toeic_practice_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.toeic.toeic_practice_backend.domain.dto.response.recommendation.RecommendationResponse;

import lombok.extern.slf4j.Slf4j;


/**
 * Service for integrating with the Python-based Recommender Service
 */
@Service
@Slf4j
public class RecommendationIntegrationService {
    private final RestTemplate restTemplate;
    private final String recommenderServiceUrl;
    private final String internalApiKey;

    @Autowired
    public RecommendationIntegrationService(
            RestTemplate restTemplate,
            @Value("${recommender.service.url}") String recommenderServiceUrl,
            @Value("${recommender.internal.api-key}") String internalApiKey) {
        this.restTemplate = restTemplate;
        this.recommenderServiceUrl = recommenderServiceUrl;
        this.internalApiKey = internalApiKey;
    }

    
    @Cacheable(value = "recommendationCache", key = "#userId")
    public RecommendationResponse getRecommendations(String userId) {
        log.info("Fetching recommendations for user: {}", userId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-API-Key", internalApiKey);
            
            String url = recommenderServiceUrl + "/recommendations/"+ userId;
         
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<RecommendationResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    RecommendationResponse.class
                );
            // Check response status
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Successfully retrieved recommendations for user: {}", userId);
                return response.getBody();
            } else {
                log.warn("Received unsuccessful response from recommender service: {}", response.getStatusCode());
                return null;
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("User {} not found in recommender service", userId);
            } else {
                log.error("HTTP error when calling recommender service: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            }
            return null;
        } catch (RestClientException e) {
            log.error("Error connecting to recommender service: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error retrieving recommendations: {}", e.getMessage(), e);
            return null;
        }
    }   
    
    @CachePut(value = "recommendationCache", key = "#userId")
	public RecommendationResponse refreshRecommendations(String userId) {
	    return getRecommendationsNoCache(userId);
	}
    
    private RecommendationResponse getRecommendationsNoCache(String userId) {
        log.info("Fetching recommendations for user: {}", userId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Internal-API-Key", internalApiKey);
            
            String url = recommenderServiceUrl + "/recommendations/"+ userId;
         
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<RecommendationResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    RecommendationResponse.class
                );
            // Check response status
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Successfully retrieved recommendations for user: {}", userId);
                return response.getBody();
            } else {
                log.warn("Received unsuccessful response from recommender service: {}", response.getStatusCode());
                return null;
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("User {} not found in recommender service", userId);
            } else {
                log.error("HTTP error when calling recommender service: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            }
            return null;
        } catch (RestClientException e) {
            log.error("Error connecting to recommender service: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error retrieving recommendations: {}", e.getMessage(), e);
            return null;
        }
    }   
} 