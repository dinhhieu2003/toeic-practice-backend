import csv
import json
import random

# Danh sách các topic mẫu
all_topics = ["Grammar", "Vocabulary", "Reading Comprehension", "Listening", "Writing"]

# Danh sách testId và lessonId
all_test_ids = [f"T{str(i).zfill(3)}" for i in range(101, 110)]
all_lecture_ids = [f"L{str(i).zfill(3)}" for i in range(101, 110)]

# Header cho file CSV
header = [
    "userId",
    "target",
    "averageListeningScore",
    "averageReadingScore",
    "averageTotalScore",
    "highestScore",
    "topicStats",
    "skillStats",
    "learningProgress",
    "testHistory",
    "weakness"
]

rows = []

for i in range(50):
    # Tạo userId
    user_id = f"U{str(i + 1).zfill(3)}"

    # Sinh điểm số TOEIC chia hết cho 5
    avg_listening = random.randint(350 // 5, 450 // 5) * 5
    avg_reading = random.randint(350 // 5, 450 // 5) * 5
    avg_total = avg_listening + avg_reading
    target = random.randint((avg_total + 5) // 5, 990 // 5) * 5
    highest = random.randint(avg_total // 5, (target - 5) // 5) * 5

    # topicStats
    topics_sample = random.sample(all_topics, 2)
    topic_stats = [
        {
            "Topic": topic,
            "totalCorrect": random.randint(5, 20),
            "totalIncorrect": random.randint(0, 10),
            "averageTime": random.randint(20, 40)
        }
        for topic in topics_sample
    ]
    topic_stats_json = json.dumps(topic_stats)

    # skillStats
    skill_stats = [
        {
            "skill": "listening",
            "totalCorrect": random.randint(10, 30),
            "totalIncorrect": random.randint(0, 10),
            "totalTime": random.randint(200, 400)
        },
        {
            "skill": "reading",
            "totalCorrect": random.randint(10, 30),
            "totalIncorrect": random.randint(0, 10),
            "totalTime": random.randint(200, 400)
        }
    ]
    skill_stats_json = json.dumps(skill_stats)

    # learningProgress (dạng [{"lessonId": {"percent": ...}}])
    lectures_sample = random.sample(all_lecture_ids, 2)
    learning_progress = [
        {lec: {"percent": random.randint(10, 100)}}
        for lec in lectures_sample
    ]
    learning_progress_json = json.dumps(learning_progress)

    # testHistory (dạng [{"testId": {"avgScore": ..., "attemp": ...}}])
    num_tests = random.randint(2, 4)
    selected_tests = random.sample(all_test_ids, num_tests)

    # Khởi tạo điểm ngẫu nhiên chia hết cho 5 để đảm bảo trung bình đúng bằng `averageTotalScore`
    test_scores = [random.randint(300 // 5, 990 // 5) * 5 for _ in range(num_tests)]
    total_score = sum(test_scores)

    # Điều chỉnh bài test cuối cùng để đảm bảo tổng điểm chia trung bình đúng bằng `averageTotalScore`
    test_scores[-1] += (avg_total * num_tests) - total_score
    test_scores[-1] = max(300, min(990, test_scores[-1]))  # Giới hạn từ 300 - 990

    test_history = [
        {test: {"avgScore": test_scores[idx], "attemp": random.randint(1, 5)}}
        for idx, test in enumerate(selected_tests)
    ]
    test_history_json = json.dumps(test_history)

    # weakness
    weakness_json = json.dumps(all_topics)

    row = {
        "userId": user_id,
        "target": target,
        "averageListeningScore": avg_listening,
        "averageReadingScore": avg_reading,
        "averageTotalScore": avg_total,
        "highestScore": highest,
        "topicStats": topic_stats_json,
        "skillStats": skill_stats_json,
        "learningProgress": learning_progress_json,
        "testHistory": test_history_json,
        "weakness": weakness_json
    }

    rows.append(row)

# Ghi dữ liệu vào file CSV
with open("training_data.csv", "w", newline="", encoding="utf-8") as f:
    writer = csv.DictWriter(f, fieldnames=header)
    writer.writeheader()
    writer.writerows(rows)

print("File CSV 'training_data.csv' đã được tạo với 50 dòng dữ liệu mẫu.")
