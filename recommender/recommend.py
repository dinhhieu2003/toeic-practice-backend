import pandas as pd
import numpy as np
import json
from collections import defaultdict

# --- 1. Data Loading and Processing ---
def load_data(file_path="training_data.csv"):
    """
    Load and preprocess the training data.
    """
    df = pd.read_csv(file_path, encoding="utf-8")
    
    # Parse JSON fields
    json_columns = ['topicStats', 'testHistory', 'learningProgress']
    for col in json_columns:
        df[col] = df[col].apply(json.loads)
    
    return df

# --- 2. Build User and Item Profiles ---
def build_profiles(df):
    """
    Create user profiles and candidate pools with additional metadata.
    """
    # User profiles
    user_profiles = {}
    for _, row in df.iterrows():
        user_profiles[row["userId"]] = {
            "target": row["target"],
            "averageListeningScore": row["averageListeningScore"],
            "averageReadingScore": row["averageReadingScore"],
            "averageTotalScore": row["averageTotalScore"],
            "topicStats": row["topicStats"],
            "testHistory": row["testHistory"],
            "learningProgress": row["learningProgress"],
        }
    
    # Enhanced candidate pools with learning dependencies and engagement metrics
    candidate_tests = [
        {"testId": "T101", "difficulty": 840, "topics": ["Grammar", "Vocabulary"], 
         "avgTimeToComplete": 30},
        {"testId": "T102", "difficulty": 860, "topics": ["Listening", "Grammar"], 
         "avgTimeToComplete": 35},
        {"testId": "T103", "difficulty": 880, "topics": ["Reading Comprehension", "Vocabulary"], 
         "avgTimeToComplete": 40},
        {"testId": "T104", "difficulty": 900, "topics": ["Grammar", "Listening"], 
         "avgTimeToComplete": 45},
        {"testId": "T105", "difficulty": 870, "topics": ["Vocabulary", "Reading Comprehension"], 
         "avgTimeToComplete": 40},
    ]
    
    candidate_lessons = [
        {"lessonId": "L101", "topics": ["Grammar", "Writing"], 
         "avgTimeToComplete": 20},
        {"lessonId": "L102", "topics": ["Vocabulary", "Reading Comprehension"], 
         "avgTimeToComplete": 25},
        {"lessonId": "L103", "topics": ["Listening", "Grammar"], 
         "avgTimeToComplete": 30},
        {"lessonId": "L104", "topics": ["Reading Comprehension", "Vocabulary"], 
         "avgTimeToComplete": 35},
        {"lessonId": "L105", "topics": ["Writing", "Listening"], 
         "avgTimeToComplete": 30},
    ]
    
    return user_profiles, candidate_tests, candidate_lessons

# --- 3. Utility Functions ---
def get_topic_deficiency(topic, topicStats, desired_rate=0.8):
    """
    Calculate topic deficiency.
    A higher deficiency means the user needs more practice in this topic.
    """
    for item in topicStats:
        if item["Topic"] == topic:
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

def find_similar_users(target_user_id, user_profiles, n=5):
    """
    Find users with similar target scores and performance profiles.
    Returns a list of (user_id, similarity_score) tuples.
    """
    target_profile = user_profiles[target_user_id]
    similarities = []
    
    for uid, profile in user_profiles.items():
        if uid == target_user_id:
            continue
        
        # Calculate similarity based on score profiles and targets
        target_diff = abs(profile["target"] - target_profile["target"])
        listening_diff = abs(profile["averageListeningScore"] - target_profile["averageListeningScore"])
        reading_diff = abs(profile["averageReadingScore"] - target_profile["averageReadingScore"])
        total_diff = abs(profile["averageTotalScore"] - target_profile["averageTotalScore"])
        
        # Normalize differences (lower is better)
        normalized_diff = (target_diff / 100 + listening_diff / 100 + reading_diff / 100 + total_diff / 100) / 4
        similarity = 1 - normalized_diff  # Convert to similarity (higher is better)
        
        similarities.append((uid, similarity))
    
    # Return top n similar users
    return sorted(similarities, key=lambda x: x[1], reverse=True)[:n]

def get_collaborative_score(candidate_id, similar_users, user_profiles, candidate_type="test"):
    """
    Calculate collaborative score based on similar users' interactions.
    Higher score means similar users performed well with this content.
    """
    if not similar_users:
        return 0
    
    score = 0
    count = 0
    for uid, similarity in similar_users:
        profile = user_profiles[uid]
        
        if candidate_type == "test":
            # Check if the user has taken this test
            test_history = profile.get("testHistory", {})
            if isinstance(test_history, dict) and candidate_id in test_history:
                # Score is based on similar users' performance on this test
                test_result = test_history[candidate_id]
                if isinstance(test_result, dict) and "avgScore" in test_result and "attemp" in test_result:
                    avg_score = test_result["avgScore"]
                    attempts = test_result["attemp"]
                    
                    # Normalize score (0-1 scale)
                    normalized_score = avg_score / 990
                    
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
                    
        elif candidate_type == "lesson":
            # Check if the user has started this lesson
            learning_progress = profile.get("learningProgress", [])
            for item in learning_progress:
                if isinstance(item, dict) and candidate_id in item:
                    progress_data = item[candidate_id]
                    if isinstance(progress_data, dict) and "percent" in progress_data:
                        completion = min(progress_data["percent"], 100) / 100
                        score += similarity * completion
                        count += 1
                        break
    
    return score / max(1, count)  # Avoid division by zero

# --- 4. Enhanced Scoring Function ---
def score_candidate(user_profiles, user_profile, candidate, similar_users, candidate_type="test", 
                   margin=50, weight_topic=10, weight_collab=5):
    """
    Calculate a comprehensive score for a candidate item.
    """
    # --- 1. Content-based component ---
    recommended_difficulty = user_profile["target"] + margin
    max_diff = 100  # Constant to normalize difficulty gap
    
    # Calculate topic deficiency
    total_deficiency = 0
    for topic in candidate["topics"]:
        total_deficiency += get_topic_deficiency(topic, user_profile["topicStats"])
    
    # Select base difficulty
    if user_profile["averageTotalScore"] < user_profile["target"]:
        base_difficulty = user_profile["averageTotalScore"]
    else:
        base_difficulty = recommended_difficulty
    
    # For tests, calculate difficulty match
    if candidate_type == "test" and "difficulty" in candidate:
        diff_gap = abs(candidate["difficulty"] - base_difficulty)
        difficulty_score = max(0, (max_diff - diff_gap) / max_diff)  # Normalize 0-1
        
        # Normalize content score to 0-1 range
        topic_score = total_deficiency / (len(candidate["topics"]) * 0.8)
        content_score = (difficulty_score + topic_score) / 2  # Average instead of sum
    else:  # For lessons
        content_score = min(1.0, total_deficiency / (len(candidate["topics"]) * 0.8))
    
    # --- 2. Collaborative component ---
    collab_score = get_collaborative_score(
        candidate["testId"] if candidate_type == "test" else candidate["lessonId"],
        similar_users, 
        user_profiles,
        candidate_type
    )
    
    # --- 3. Calculate weighted final score ---
    final_score = (
        weight_topic * content_score + 
        weight_collab * collab_score
    ) / (weight_topic + weight_collab)
    
    return final_score

# --- 5. Enhanced Recommendation Function ---
def recommend_hybrid(user_id, user_profiles, candidate_tests, candidate_lessons, 
                    top_n=3, margin=50):
    """
    Generate personalized recommendations using a hybrid approach:
    - Content-based filtering using topic deficiency and difficulty
    - Collaborative filtering using similar users' performance
    """
    if user_id not in user_profiles:
        print(f"User {user_id} doesn't exist!")
        return [], []
    
    profile = user_profiles[user_id]
    
    # Find similar users for collaborative filtering
    similar_users = find_similar_users(user_id, user_profiles, n=5)

    print(f"Người giống {user_id}: ", similar_users)
    print("\n")
    
    # Score and filter candidate tests
    scored_tests = []
    for test in candidate_tests:
        # Allow tests that are slightly below the user's current score
        if test["difficulty"] >= profile["averageTotalScore"] - 20:
            score = score_candidate(
                user_profiles, profile, test, similar_users, 
                candidate_type="test", margin=margin
            )
            
            # Check if test was already taken
            already_taken = False
            attempts = 0
            avg_score = 0
            
            test_history = profile.get("testHistory", {})
            if isinstance(test_history, dict) and test["testId"] in test_history:
                already_taken = True
                test_data = test_history[test["testId"]]
                if isinstance(test_data, dict):
                    attempts = test_data.get("attemp", 0)
                    avg_score = test_data.get("avgScore", 0)
            
            test_label = test["testId"]
            if already_taken:
                test_label += f" (Làm lại - {attempts} lần)"
            
            scored_tests.append({
                "id": test_label,
                "score": score,
                "already_taken": already_taken,
                "attempts": attempts,
                "avg_score": avg_score,
                "difficulty": test["difficulty"],
                "topics": test["topics"],
            })
    
    # Score candidate lessons
    scored_lessons = []
    for lesson in candidate_lessons:
        score = score_candidate(
            user_profiles, profile, lesson, similar_users,
            candidate_type="lesson", margin=margin
        )
        
        # Check if lesson was already started
        already_learned = False
        completion_percent = 0
        
        learning_progress = profile.get("learningProgress", [])
        for item in learning_progress:
            if isinstance(item, dict) and lesson["lessonId"] in item:
                already_learned = True
                progress_data = item[lesson["lessonId"]]
                if isinstance(progress_data, dict):
                    completion_percent = progress_data.get("percent", 0)
                break
        
        lesson_label = lesson["lessonId"]
        if already_learned:
            lesson_label += f" (Học tiếp - {completion_percent}%)"
        
        scored_lessons.append({
            "id": lesson_label,
            "score": score,
            "already_learned": already_learned,
            "completion": completion_percent,
            "topics": lesson["topics"],
        })
    
    # Sort and select top recommendations
    recommended_tests = sorted(scored_tests, key=lambda x: x["score"], reverse=True)[:top_n]
    recommended_lessons = sorted(scored_lessons, key=lambda x: x["score"], reverse=True)[:top_n]
    
    # Format output with detailed explanation
    test_recommendations = []
    for rec in recommended_tests:
        # Create explanation for why this test was recommended
        explanation = []
        if rec["already_taken"]:
            if rec["avg_score"] < profile["target"]:
                explanation.append(f"Bài test đã làm {rec['attempts']} lần với điểm trung bình {rec['avg_score']} (thấp hơn mục tiêu {profile['target']})")
            else:
                explanation.append(f"Bài test đã làm {rec['attempts']} lần với điểm trung bình {rec['avg_score']}")
        
        topic_list = ", ".join(rec["topics"])
        explanation.append(f"Liên quan đến các chủ đề: {topic_list}")
        
        diff_gap = abs(rec["difficulty"] - profile["target"])
        explanation.append(f"Độ khó {rec['difficulty']}, cách mục tiêu {diff_gap} điểm")
        
        test_recommendations.append({
            "id": rec["id"],
            "score": rec["score"],
            "explanation": explanation
        })
    
    lesson_recommendations = []
    for rec in recommended_lessons:
        # Create explanation for why this lesson was recommended
        explanation = []
        if rec["already_learned"]:
            explanation.append("Bài học đã học trước đây")
        
        topic_list = ", ".join(rec["topics"])
        explanation.append(f"Giúp cải thiện các chủ đề: {topic_list}")
        
        lesson_recommendations.append({
            "id": rec["id"],
            "score": rec["score"],
            "explanation": explanation
        })
    
    return test_recommendations, lesson_recommendations

# --- 6. Demo Usage Function ---
def run_recommendation_demo():
    """
    Demonstrate the enhanced recommendation system on sample data.
    """
    # Load data
    df = load_data()
    
    # Build profiles
    user_profiles, candidate_tests, candidate_lessons = build_profiles(df)
    
    # Generate recommendations for few sample users
    for uid in list(user_profiles.keys())[:3]:
        rec_tests, rec_lessons = recommend_hybrid(
            uid, user_profiles, candidate_tests, candidate_lessons, 
            top_n=3, margin=50
        )
        
        print(f"\n======= Recommendations for User {uid} =======")
        print("\nRecommended Tests:")
        for i, test in enumerate(rec_tests, 1):
            print(f"{i}. {test['id']} (score: {test['score']:.4f})")
            for expl in test['explanation']:
                print(f"   - {expl}")
        
        print("\nRecommended Lessons:")
        for i, lesson in enumerate(rec_lessons, 1):
            print(f"{i}. {lesson['id']} (score: {lesson['score']:.4f})")
            for expl in lesson['explanation']:
                print(f"   - {expl}")

# --- 7. Main execution point ---
if __name__ == "__main__":
    run_recommendation_demo()