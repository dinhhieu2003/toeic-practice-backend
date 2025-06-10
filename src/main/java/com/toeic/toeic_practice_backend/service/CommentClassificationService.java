package com.toeic.toeic_practice_backend.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeic.toeic_practice_backend.domain.dto.request.comment.CreateCommentRequest;
import com.toeic.toeic_practice_backend.domain.dto.request.comment.DeleteCommentRequest;
import com.toeic.toeic_practice_backend.domain.dto.response.ApiResponse;
import com.toeic.toeic_practice_backend.domain.dto.response.comment.CommentClassificationResponse;
import com.toeic.toeic_practice_backend.domain.entity.Comment;
import com.toeic.toeic_practice_backend.domain.entity.Notification;
import com.toeic.toeic_practice_backend.exception.AppException;
import com.toeic.toeic_practice_backend.repository.CommentRepository;
import com.toeic.toeic_practice_backend.utils.constants.CommentTargetType;
import com.toeic.toeic_practice_backend.utils.constants.DeleteReasonTagComment;
import com.toeic.toeic_practice_backend.utils.constants.ErrorCode;
import com.toeic.toeic_practice_backend.utils.constants.NotificationType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentClassificationService {
	private final RestTemplate restTemplate;
	private final CommentRepository commentRepository;
	private final TestService testService;
	private final LectureService lectureService;
	private final NotificationService notificationService;
	@Async
	public void checkComment(Comment comment) {
	    Map<String, String> requestBody = new HashMap<>();
	    requestBody.put("text", comment.getContent());

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    String url = "https://vietnamese-toxic-classifier-app.azurewebsites.net/api/v1/classify";

	    HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

	    try {
	        ResponseEntity<String> response = restTemplate.exchange(
	                url,
	                HttpMethod.POST,
	                requestEntity,
	                String.class
	        );

	        if (response.getStatusCode().is2xxSuccessful()) {
	            String responseBody = response.getBody();
	            System.out.println("Raw response: " + responseBody);

	            ObjectMapper mapper = new ObjectMapper();

	            ApiResponse<CommentClassificationResponse> apiResponse = mapper.readValue(
	                responseBody,
	                new TypeReference<ApiResponse<CommentClassificationResponse>>() {}
	            );

	            CommentClassificationResponse data = apiResponse.getData();

	            float probInsult = data.getClassificationProbabilities().getInsult();
	            float probHateSpeech = data.getClassificationProbabilities().getHateSpeech();
	            float probObscene = data.getClassificationProbabilities().getObscene();
	            float probSevereToxicity = data.getClassificationProbabilities().getSevereToxicity();
	            float probSpam = data.getClassificationProbabilities().getSpam();
	            float probThreat = data.getClassificationProbabilities().getThreat();

	            comment.setProbInsult(probInsult);
	            comment.setProbHateSpeech(probHateSpeech);
	            comment.setProbObscene(probObscene);
	            comment.setProbSevereToxicity(probSevereToxicity);
	            comment.setProbSpam(probSpam);
	            comment.setProbThreat(probThreat);
	            commentRepository.save(comment);

	            if (probInsult > 0.5 || probHateSpeech > 0.5 || probObscene > 0.5 ||
	                    probSevereToxicity > 0.5 || probSpam > 0.5 || probThreat > 0.5) {

	                DeleteReasonTagComment tag = DeleteReasonTagComment.VIOLATE_COMMUNITY_STANDARDS;
	                log.warn("Comment is toxic, deleting. id={}", comment.getId());

	                StringBuilder reasonDetails = new StringBuilder();
	                if (probInsult > 0.5) reasonDetails.append("Comment sỉ vả | ");
	                if (probHateSpeech > 0.5) reasonDetails.append("Comment chỉ trích | ");
	                if (probObscene > 0.5) reasonDetails.append("Comment tục tĩu | ");
	                if (probSevereToxicity > 0.5) reasonDetails.append("Comment độc hại | ");
	                if (probSpam > 0.5) reasonDetails.append("Comment spam | ");
	                if (probThreat > 0.5) reasonDetails.append("Comment đe dọa | ");

	                String reason = reasonDetails.toString();

	                DeleteCommentRequest deleteCommentInfo = new DeleteCommentRequest(tag, reason);
	                deleteComment(comment.getId(), deleteCommentInfo);
	                log.info("Auto delete success");
	                
	                log.info("Build notification delete comment...");
	                Notification notification = buildTempNotification(comment, reason);
	                notificationService.createNotification(notification);
	                log.info("Notify success");
	            }
	        }
	    } catch (Exception e) {
	        log.error("Error when calling toxic classifier API or processing response", e);
	    }
	}
	
	public void deleteComment(String commentId, DeleteCommentRequest request) {
	    Comment comment = commentRepository.findById(commentId)
	            .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
	    
	    comment.setActive(false);
	    comment.setDeleted(true);
	    comment.setDeleteReasonTag(request.getReasonTag());
	    comment.setDeleteReason(request.getReason());
	    
	    commentRepository.save(comment);
	}
	
	private Notification buildTempNotification(Comment createdComment, String reason) {
		log.info("Building notification...");
		String userId = createdComment.getUserId();
    	String relatedId = createdComment.getTargetId();
    	String message = "Nothing";
    	boolean isRead = false;
    	NotificationType notificationType = null;
    	String deepLink = "";
    	if(createdComment.getTargetType() == CommentTargetType.TEST) {
    		String testName = testService.getTestName(relatedId);
    		message = "Comment của bạn trong đề thi: " + testName + 
    				" bị xóa do vi phạm tiêu chuẩn cộng đồng: ["
    				+ reason + "]";
    		notificationType = NotificationType.COMMENT_DELETED_TEST;
    		deepLink += "/test/"+ relatedId;
    	}
    	if(createdComment.getTargetType() == CommentTargetType.LECTURE) {
    		String lectureName = lectureService.getLectureName(relatedId);
    		message = "Comment của bạn trong bài học: " + lectureName + 
    				" bị xóa do vi phạm tiêu chuẩn cộng đồng: ["
    				+ reason + "]";
    		notificationType = NotificationType.COMMENT_DELETED_LECTURE;
    		deepLink += "/lecture/" + lectureName + "___" + relatedId;
    	}
    	Notification notification = Notification.builder()
    			.userId(userId)
    			.type(notificationType)
    			.message(message)
    			.relatedId(relatedId)
    			.isRead(isRead)
    			.deepLink(deepLink)
    			.build();
    	return notification;
	}
}
