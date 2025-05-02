"""
Similarity calculation module for user-based collaborative filtering.
"""
import logging
from typing import Dict, List, Any, Tuple

logger = logging.getLogger(__name__)

def calculate_user_similarity(user1: Dict[str, Any], user2: Dict[str, Any]) -> float:
    """
    Calculate similarity between two users based on their profile metrics.
    
    Args:
        user1: First user profile data
        user2: Second user profile data
        
    Returns:
        Similarity score (0-1 where 1 is most similar)
    """
    try:
        # Validate required fields in both profiles
        required_fields = ["target", "averageListeningScore", "averageReadingScore", "averageTotalScore"]
        missing_fields_1 = [field for field in required_fields if field not in user1]
        missing_fields_2 = [field for field in required_fields if field not in user2]
        
        if missing_fields_1 or missing_fields_2:
            logger.warning(f"Missing fields for similarity calculation: user1 missing {missing_fields_1}, user2 missing {missing_fields_2}")
            # If any fields are missing, fall back to minimal similarity
            return 0.1
            
        # Calculate similarity based on score profiles and targets
        target_diff = abs(user1.get("target", 0) - user2.get("target", 0))
        listening_diff = abs(user1.get("averageListeningScore", 0) - user2.get("averageReadingScore", 0))
        reading_diff = abs(user1.get("averageReadingScore", 0) - user2.get("averageListeningScore", 0))
        total_diff = abs(user1.get("averageTotalScore", 0) - user2.get("averageTotalScore", 0))
        
        # Normalize differences (lower is better)
        # Use a small epsilon to avoid division by zero
        max_target_diff = 500.0  # Reasonable max difference for target scores
        max_score_diff = 300.0   # Reasonable max difference for average scores
        
        normalized_diffs = [
            min(1.0, target_diff / max_target_diff),
            min(1.0, listening_diff / max_score_diff),
            min(1.0, reading_diff / max_score_diff),
            min(1.0, total_diff / max_score_diff)
        ]
        
        avg_normalized_diff = sum(normalized_diffs) / len(normalized_diffs)
        
        # Convert to similarity (higher is better)
        similarity = 1 - avg_normalized_diff
        
        return max(0, min(1, similarity))  # Ensure result is between 0 and 1
    
    except Exception as e:
        logger.error(f"Error calculating similarity: {str(e)}")
        return 0.0

def find_similar_users(target_user_profile: Dict[str, Any], all_users_data: List[Dict[str, Any]], n: int = 5) -> List[Tuple[str, float]]:
    """
    Find users with similar target scores and performance profiles.
    
    Args:
        target_user_profile: The profile of the target user
        all_users_data: List of all user profiles for comparison
        n: Number of similar users to return
        
    Returns:
        List of (user_id, similarity_score) tuples for the most similar users
    """
    # Validate input data
    if not isinstance(target_user_profile, dict):
        logger.error(f"Expected target_user_profile to be a dict, got {type(target_user_profile)}")
        return []
        
    if not isinstance(all_users_data, list):
        logger.error(f"Expected all_users_data to be a list, got {type(all_users_data)}")
        return []
    
    target_user_id = target_user_profile.get("userId")
    if not target_user_id:
        logger.error("Target user profile missing userId")
        return []
    
    logger.info(f"Finding similar users for user ID: {target_user_id}")
    similarities = []
    
    # Validate required fields in target profile
    required_fields = ["target", "averageListeningScore", "averageReadingScore", "averageTotalScore"]
    if not all(field in target_user_profile for field in required_fields):
        logger.warning(f"Target user profile missing some required fields for similarity calculation")
    
    # Calculate similarity for each user
    valid_profiles_count = 0
    for profile in all_users_data:
        if not isinstance(profile, dict):
            logger.warning(f"Expected user profile to be a dict, got {type(profile)}")
            continue
            
        user_id = profile.get("userId")
        if not user_id or user_id == target_user_id:
            continue
        
        # Verify the profile has necessary fields for similarity calculation
        if not all(field in profile for field in required_fields):
            logger.debug(f"User {user_id} missing required fields for similarity calculation")
            continue
            
        valid_profiles_count += 1
        
        # Calculate similarity between target user and this user
        similarity = calculate_user_similarity(target_user_profile, profile)
        
        similarities.append((user_id, similarity))
    
    # Sort by similarity (descending) and get top n
    similarities.sort(key=lambda x: x[1], reverse=True)
    
    result = similarities[:n]
    logger.debug(f"Processed {valid_profiles_count} valid profiles and found {len(result)} similar users for {target_user_id}")
    return result 