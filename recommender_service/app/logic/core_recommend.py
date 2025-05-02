"""
Core recommendation logic for the recommender service.
This module contains the main algorithms for generating personalized recommendations.
"""
import logging
import os
from typing import Dict, List, Any, Optional, Tuple
from . import data_fetcher
from .similarity import find_similar_users
from .cold_start import recommend_cold_start_tests, recommend_cold_start_lectures
from ..utils.config import WEIGHT_TOPIC, WEIGHT_COLLAB, TARGET_MARGIN

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

async def generate_recommendations(user_id: str, limit: int = 5) -> Dict[str, List[Dict[str, Any]]]:
    """
    Generate personalized recommendations for a user
    
    Args:
        user_id: The ID of the user to generate recommendations for
        limit: The maximum number of recommendations to return
        
    Returns:
        A dictionary containing the recommended tests and lectures
    """
    logger.info(f"Generating recommendations for user {user_id}")
    
    # Get user profile data
    user_profile = await data_fetcher.get_user_profile(user_id)
    
    # If user has no history, use cold start recommendations
    if is_cold_start_user(user_profile):
        logger.info(f"Using cold start recommendations for user {user_id}")
        return await generate_cold_start_recommendations(user_profile, limit)
    
    # Use the hybrid recommendation approach for users with history
    return await recommend_hybrid(user_id, limit)

def is_cold_start_user(user_profile: Dict[str, Any]) -> bool:
    """
    Determine if this is a cold start user (new user with little history)
    
    Args:
        user_profile: The user profile data
        
    Returns:
        True if this is a cold start user, False otherwise
    """
    # Validate user profile structure
    if not isinstance(user_profile, dict):
        logger.warning(f"Invalid user profile type: {type(user_profile)}")
        return True
        
    test_history = user_profile.get("testHistory", [])
    if not isinstance(test_history, list):
        logger.warning(f"Invalid testHistory type: {type(test_history)}")
        test_history = []
        
    learning_progress = user_profile.get("learningProgress", {})
    if not isinstance(learning_progress, dict):
        logger.warning(f"Invalid learningProgress type: {type(learning_progress)}")
        learning_progress = {}
    
    # Consider it a cold start if the user has less than 2 tests in history
    # and has started less than 3 lectures
    return len(test_history) < 2 and len(learning_progress) < 3

async def generate_cold_start_recommendations(user_profile: Dict[str, Any], limit: int = 5) -> Dict[str, List[Dict[str, Any]]]:
    """
    Generate recommendations for new users with little history
    
    Args:
        user_profile: The user profile data
        limit: The maximum number of recommendations to generate
        
    Returns:
        A dictionary containing the recommended tests and lectures
    """
    # Get test and lecture candidates
    test_candidates = await data_fetcher.get_test_candidates()
    lecture_candidates = await data_fetcher.get_lecture_candidates()
    
    # Use cold start recommendation logic
    recommended_tests = await recommend_cold_start_tests(user_profile, test_candidates, limit)
    recommended_lectures = await recommend_cold_start_lectures(user_profile, lecture_candidates, limit)
    
    return {
        "tests": recommended_tests,
        "lectures": recommended_lectures
    }

async def recommend_hybrid(user_profile: Dict[str, Any], limit: int = 5, margin: int = TARGET_MARGIN) -> Dict[str, List[Dict[str, Any]]]:
    """
    Generate personalized recommendations using a hybrid approach:
    - Content-based filtering using topic deficiency and difficulty
    - Collaborative filtering using similar users' performance
    
    Args:
        user_id: ID of the user to generate recommendations for
        limit: Maximum number of recommendations to return
        margin: Margin above current score for recommended difficulty
        
    Returns:
        A dictionary containing the recommended tests and lectures
    """
    logger.info(f"Generating hybrid recommendations for user {user_profile['userId']}")
    
    # Fetch all necessary data
    test_candidates = await data_fetcher.get_test_candidates()
    lecture_candidates = await data_fetcher.get_lecture_candidates()
    all_users_data = await data_fetcher.get_all_user_profiles_for_similarity()
    
    # Validate essential data
    if not isinstance(user_profile, dict) or not user_profile:
        logger.error(f"Invalid or empty user profile for user {user_profile['userId']}")
        return {"tests": [], "lectures": []}
        
    if not isinstance(test_candidates, list):
        logger.warning(f"Invalid test candidates format: {type(test_candidates)}")
        test_candidates = []
        
    if not isinstance(lecture_candidates, list):
        logger.warning(f"Invalid lecture candidates format: {type(lecture_candidates)}")
        lecture_candidates = []
        
    if not isinstance(all_users_data, list):
        logger.warning(f"Invalid user profiles format: {type(all_users_data)}")
        all_users_data = []
    
    # Find similar users for collaborative filtering
    similar_users = find_similar_users(user_profile, all_users_data, n=5)
    logger.info(f"Found {len(similar_users)} similar users for {user_profile['userId']}")
    
    # Score and filter candidate tests
    scored_tests = []
    for test in test_candidates:
        if not isinstance(test, dict):
            logger.warning(f"Invalid test format: {type(test)}")
            continue
            
        # Allow tests that are slightly below the user's current score
        avg_total_score = user_profile.get("averageTotalScore", 0)
        test_difficulty = test.get("difficulty", 0)
        
        if test_difficulty >= avg_total_score - 20:
            score = score_candidate(
                user_profile, 
                test, 
                similar_users, 
                all_users_data,
                candidate_type="test", 
                margin=margin
            )
            
            # Check if test was already taken
            already_taken = False
            attempts = 0
            avg_score = 0
            
            test_history = user_profile.get("testHistory", [])
            if isinstance(test_history, list):
                test_id = test.get("testId")
                if test_id:
                    for test_record in test_history:
                        if isinstance(test_record, dict) and test_record.get("testId") == test_id:
                            already_taken = True
                            attempts = test_record.get("attempt", test_record.get("attempts", 0))
                            avg_score = test_record.get("avgScore", test_record.get("averageScore", 0))
                            break
            
            test_label = test.get("testId", "Unknown")
            if already_taken:
                test_label += f" (Làm lại - {attempts} lần)"
            
            scored_tests.append({
                "id": test_label,
                "score": score,
                "already_taken": already_taken,
                "attempts": attempts,
                "avg_score": avg_score,
                "difficulty": test.get("difficulty", 0),
                "topics": test.get("topics", []),
                "testId": test.get("testId", "")
            })
    
    # Score candidate lectures
    scored_lectures = []
    for lecture in lecture_candidates:
        if not isinstance(lecture, dict):
            logger.warning(f"Invalid lecture format: {type(lecture)}")
            continue
            
        score = score_candidate(
            user_profile, 
            lecture, 
            similar_users,
            all_users_data,
            candidate_type="lecture", 
            margin=margin
        )
        
        # Check if lecture was already started
        already_learned = False
        completion_percent = 0
        
        learning_progress = user_profile.get("learningProgress", {})
        if isinstance(learning_progress, dict):
            lecture_id = lecture.get("lectureId")
            if lecture_id and lecture_id in learning_progress:
                progress_data = learning_progress.get(lecture_id)
                if isinstance(progress_data, dict):
                    already_learned = True
                    completion_percent = progress_data.get("percent", 0)
        
        lecture_label = lecture.get("lectureId", "Unknown")
        if already_learned:
            lecture_label += f" (Học tiếp - {completion_percent}%)"
        
        scored_lectures.append({
            "id": lecture_label,
            "score": score,
            "already_learned": already_learned,
            "completion": completion_percent,
            "topics": lecture.get("topics", []),
            "lectureId": lecture.get("lectureId", "")
        })
    
    # Sort and select top recommendations
    recommended_tests = sorted(scored_tests, key=lambda x: x["score"], reverse=True)[:limit]
    recommended_lectures = sorted(scored_lectures, key=lambda x: x["score"], reverse=True)[:limit]
    
    # Format output with detailed explanation
    test_recommendations = []
    for rec in recommended_tests:
        # Create explanation for why this test was recommended
        explanation = []
        if rec["already_taken"]:
            if rec["avg_score"] < user_profile.get("target", 0):
                explanation.append(f"Bài test đã làm {rec['attempts']} lần với điểm trung bình {rec['avg_score']} (thấp hơn mục tiêu {user_profile.get('target', 0)})")
            else:
                explanation.append(f"Bài test đã làm {rec['attempts']} lần với điểm trung bình {rec['avg_score']}")
        
        topic_list = ", ".join(rec.get("topics", []))
        explanation.append(f"Liên quan đến các chủ đề: {topic_list}")
        
        diff_gap = abs(rec.get("difficulty", 0) - user_profile.get("target", 0))
        explanation.append(f"Độ khó {rec.get('difficulty', 0)}, cách mục tiêu {diff_gap} điểm")
        
        test_recommendations.append({
            "id": rec.get("id", ""),
            "score": rec.get("score", 0),
            "explanation": explanation,
            "testId": rec.get("testId", "")
        })
    
    lecture_recommendations = []
    for rec in recommended_lectures:
        # Create explanation for why this lesson was recommended
        explanation = []
        if rec.get("already_learned", False):
            explanation.append(f"Bài học đã học {rec.get('completion', 0)}%")
        
        topic_list = ", ".join(rec.get("topics", []))
        explanation.append(f"Giúp cải thiện các chủ đề: {topic_list}")
        
        lecture_recommendations.append({
            "id": rec.get("id", ""),
            "score": rec.get("score", 0),
            "explanation": explanation,
            "lectureId": rec.get("lectureId", "")
        })
    
    return {
        "userId": user_profile["userId"],
        "recommendedTests": test_recommendations,
        "recommendedLectures": lecture_recommendations
    }

def get_topic_deficiency(topic: str, topic_stats: List[Dict[str, Any]], desired_rate: float = 0.8) -> float:
    """
    Calculate topic deficiency.
    A higher deficiency means the user needs more practice in this topic.
    
    Args:
        topic: The topic to check
        topic_stats: The user's topic statistics
        desired_rate: The desired correct rate (default 0.8 or 80%)
        
    Returns:
        A deficiency score (0-1) where higher means more deficient
    """
    if not isinstance(topic_stats, list):
        logger.warning(f"Invalid topic_stats format: {type(topic_stats)}")
        return 0.5  # Default deficiency
        
    for item in topic_stats:
        if not isinstance(item, dict):
            continue
            
        if item.get("Topic").get("id") == topic:
            total_correct = item.get("totalCorrect", 0)
            total_incorrect = item.get("totalIncorrect", 0)
            total = total_correct + total_incorrect
            
            if total > 0:
                correct_rate = total_correct / total
                deficiency = max(0, desired_rate - correct_rate)
                return deficiency
            else:
                return 0.5  # Default deficiency for topics with no attempts
    
    return 0.5  # Default deficiency for unknown topics

def get_collaborative_score(
    candidate_id: str, 
    similar_users: List[Tuple[str, float]], 
    all_users_data: List[Dict[str, Any]], 
    candidate_type: str = "test"
) -> float:
    """
    Calculate collaborative score based on similar users' interactions.
    Higher score means similar users performed well with this content.
    
    Args:
        candidate_id: ID of the candidate (test or lecture)
        similar_users: List of (user_id, similarity) tuples
        all_users_data: List of all user profiles
        candidate_type: Type of candidate ('test' or 'lecture')
        
    Returns:
        Collaborative score (0-1)
    """
    if not similar_users or not candidate_id:
        return 0
        
    if not isinstance(all_users_data, list):
        logger.warning(f"Invalid all_users_data format: {type(all_users_data)}")
        return 0
    
    # Create a lookup map for more efficient access
    user_data_map = {}
    for user in all_users_data:
        if isinstance(user, dict) and "userId" in user:
            user_data_map[user["userId"]] = user
    
    score = 0
    count = 0
    for uid, similarity in similar_users:
        # Get the user profile from the lookup map
        profile = user_data_map.get(uid)
        if not profile:
            continue
        
        if candidate_type == "test":
            # Check if the user has taken this test
            test_history = profile.get("testHistory", [])
            if not isinstance(test_history, list):
                logger.debug(f"Invalid testHistory format for user {uid}: {type(test_history)}")
                continue
            
            for test_record in test_history:
                if not isinstance(test_record, dict):
                    continue
                    
                # Try both camelCase and snake_case field names
                test_id = test_record.get("testId", test_record.get("test_id", ""))
                
                if test_id == candidate_id:
                    # Extract test performance data - try multiple field name variations
                    avg_score = test_record.get("avgScore", 
                                test_record.get("averageScore",
                                test_record.get("avg_score", 0)))
                                
                    attempts = test_record.get("attempt", 
                               test_record.get("attempts",
                               test_record.get("attemptCount", 0)))
                    
                    # Normalize score (0-1 scale)
                    normalized_score = avg_score / 990  # Assume max TOEIC score
                    
                    # Better attempt handling
                    if attempts <= 2:
                        attempt_factor = 1.0  # No penalty for first two attempts
                    else:
                        # Diminishing returns curve that flattens (not linear)
                        attempt_factor = max(0.4, 1.0 - (0.15 * (attempts - 2)))
                    
                    # Mastery bonus: if high score (>85%) with multiple attempts, increase factor
                    if normalized_score > 0.85 and attempts >= 3:
                        mastery_bonus = min(0.3, 0.1 * (attempts - 2))
                        attempt_factor += mastery_bonus
                    
                    # Calculate effective score combining normalized score and attempt factor
                    effective_score = normalized_score * attempt_factor
                    
                    # Weight by similarity to target user
                    score += similarity * effective_score
                    count += 1
                    break
                    
        elif candidate_type == "lecture":
            # Check if the user has started this lecture
            learning_progress = profile.get("learningProgress", {})
            if not isinstance(learning_progress, dict):
                logger.debug(f"Invalid learningProgress format for user {uid}: {type(learning_progress)}")
                continue
            
            # Try to get progress data - could be direct key or nested structure
            progress_data = None
            if candidate_id in learning_progress:
                # Direct key access
                progress_data = learning_progress[candidate_id]
            else:
                # Could be nested in a list
                for item in learning_progress.get("items", []):
                    if isinstance(item, dict) and candidate_id in item:
                        progress_data = item[candidate_id]
                        break
            
            if progress_data:
                if isinstance(progress_data, dict):
                    # Object with percent field
                    completion = min(progress_data.get("percent", 0), 100) / 100
                elif isinstance(progress_data, (int, float)):
                    # Direct percentage value
                    completion = min(progress_data, 100) / 100
                else:
                    logger.debug(f"Invalid progress data format: {type(progress_data)}")
                    continue
                    
                score += similarity * completion
                count += 1
    
    return score / max(1, count)  # Avoid division by zero

def score_candidate(
    user_profile: Dict[str, Any], 
    candidate: Dict[str, Any], 
    similar_users: List[Tuple[str, float]],
    all_users_data: List[Dict[str, Any]],
    candidate_type: str = "test", 
    margin: int = TARGET_MARGIN,
    weight_topic: float = WEIGHT_TOPIC,
    weight_collab: float = WEIGHT_COLLAB
) -> float:
    """
    Calculate a comprehensive score for a candidate item.
    
    Args:
        user_profile: User profile data
        candidate: Candidate item (test or lecture)
        similar_users: List of similar users (user_id, similarity)
        all_users_data: All user profiles data
        candidate_type: Type of candidate ('test' or 'lecture')
        margin: Target score margin
        weight_topic: Weight for topic deficiency component
        weight_collab: Weight for collaborative component
        
    Returns:
        Final score for the candidate
    """
    # Validate inputs
    if not isinstance(user_profile, dict) or not isinstance(candidate, dict):
        logger.warning(f"Invalid input types: user_profile={type(user_profile)}, candidate={type(candidate)}")
        return 0.0
    
    # --- 1. Content-based component ---
    recommended_difficulty = user_profile.get("target", 500) + margin
    max_diff = 100  # Constant to normalize difficulty gap
    
    # Get candidate ID based on type - try different field name patterns
    candidate_id = ""
    if candidate_type == "test":
        candidate_id = candidate.get("testId", candidate.get("id", ""))
    else:
        candidate_id = candidate.get("lectureId", candidate.get("id", ""))
    
    if not candidate_id:
        logger.warning(f"Could not determine candidate ID for {candidate_type}")
        return 0.0
    
    # Calculate topic deficiency
    total_deficiency = 0
    topics = candidate.get("topics", [])
    if isinstance(topics, list):
        for topic in topics:
            total_deficiency += get_topic_deficiency(topic, user_profile.get("topicStats", []))
    
    # Select base difficulty
    if user_profile.get("averageTotalScore", 0) < user_profile.get("target", 0):
        base_difficulty = user_profile.get("averageTotalScore", 0)
    else:
        base_difficulty = recommended_difficulty
    
    # For tests, calculate difficulty match
    if candidate_type == "test" and "difficulty" in candidate:
        diff_gap = abs(candidate["difficulty"] - base_difficulty)
        difficulty_score = max(0, (max_diff - diff_gap) / max_diff)  # Normalize 0-1
        
        # Normalize content score to 0-1 range
        topic_count = len(topics) if isinstance(topics, list) else 0
        topic_score = total_deficiency / max(1, topic_count * 0.8)
        content_score = (difficulty_score + topic_score) / 2  # Average instead of sum
    else:  # For lectures
        topic_count = len(topics) if isinstance(topics, list) else 0
        content_score = min(1.0, total_deficiency / max(1, topic_count * 0.8))
    
    # --- 2. Collaborative component ---
    collab_score = get_collaborative_score(
        candidate_id,
        similar_users, 
        all_users_data,
        candidate_type
    )
    
    # --- 3. Calculate weighted final score ---
    final_score = (
        weight_topic * content_score + 
        weight_collab * collab_score
    ) / (weight_topic + weight_collab)
    
    return final_score 