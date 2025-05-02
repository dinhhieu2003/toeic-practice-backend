"""
FastAPI Recommender Service Entry Point
"""
from fastapi import FastAPI, HTTPException, Depends, Header, Query
from typing import List, Optional, Dict, Any
import logging
from pydantic import BaseModel

from app.logic import data_fetcher
from app.logic.core_recommend import recommend_hybrid
from app.logic.cold_start import generate_cold_start_recommendations

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

# Create FastAPI app instance
app = FastAPI(
    title="TOEIC Practice Recommender API",
    description="API for generating personalized TOEIC test and lecture recommendations",
    version="1.0.0"
)

# Pydantic models for request and response
class RecommendRequest(BaseModel):
    userId: str

class RecommendResponse(BaseModel):
    userId: str
    recommendedTests: List[Dict[str, Any]]
    recommendedLectures: List[Dict[str, Any]]

@app.get("/")
async def root():
    """Root endpoint that returns API information"""
    return {
        "name": "TOEIC Practice Recommender API",
        "version": "1.0.0",
        "status": "running"
    }

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy"}

# @app.post("/recommend", response_model=RecommendResponse)
# async def recommend(request: RecommendRequest):
#     """
#     Generate personalized recommendations for a user
    
#     Args:
#         request: The recommendation request containing the user ID
        
#     Returns:
#         A list of recommended test and lecture IDs
#     """
#     user_id = request.userId
#     logger.info(f"Processing recommendation request for user: {user_id}")
    
#     try:
#         # Fetch the user profile
#         user_profile = await data_fetcher.get_user_profile(user_id)
        
#         # Check if this is a cold start user (no history)
#         is_cold_start = (
#             not user_profile.get('testHistory') and 
#             not user_profile.get('learningProgress')
#         )
        
#         if is_cold_start:
#             logger.info(f"Using cold start recommendations for new user: {user_id}")
#             result = await get_cold_start_recommendations()
#         else:
#             logger.info(f"Generating personalized recommendations for user: {user_id}")
#             hybrid_result = await recommend_hybrid(user_id)
            
#             # Extract just the IDs from the hybrid result
#             test_ids = [
#                 item.get('testId', item.get('id', '')) 
#                 for item in hybrid_result.get('tests', [])
#             ]
            
#             lecture_ids = [
#                 item.get('lectureId', item.get('id', '')) 
#                 for item in hybrid_result.get('lectures', [])
#             ]
            
#             result = {
#                 "recommended_tests": test_ids,
#                 "recommended_lectures": lecture_ids
#             }
        
#         return RecommendResponse(**result)
        
#     except HTTPException as e:
#         # Re-raise HTTP exceptions (like 404 from data_fetcher)
#         logger.error(f"HTTP error while processing recommendations: {str(e)}")
#         raise
        
#     except Exception as e:
#         logger.error(f"Error generating recommendations for user {user_id}: {str(e)}")
#         raise HTTPException(
#             status_code=500, 
#             detail=f"Internal server error processing recommendations: {str(e)}"
#         )

@app.get("/recommendations/{user_id}")
async def get_recommendations(
    user_id: str,
    limit: int = Query(5, description="Maximum number of recommendations to return")
):
    """
    Generate personalized recommendations for a user
    
    Args:
        user_id: The ID of the user to generate recommendations for
        limit: The maximum number of recommendations to return
        
    Returns:
        A dictionary containing the recommended tests and lectures
    """
    logger.info(f"Processing recommendation request for user: {user_id}")
    try:
        # Fetch the user profile
        user_profile = await data_fetcher.get_user_profile(user_id)
        logger.info(f"Success get user profile id {user_id}")
        # Check if this is a cold start user (no history)
        is_cold_start = (
            not user_profile.get('testHistory') and 
            not user_profile.get('learningProgress')
        )
        
        if is_cold_start:
            logger.info(f"Using cold start recommendations for new user: {user_id}")
            result = await generate_cold_start_recommendations(user_profile, limit)
            return result
        else:
            logger.info(f"Generating personalized recommendations for user: {user_id}")
            hybrid_result = await recommend_hybrid(user_profile, limit)
            return hybrid_result
    except Exception as e:
        logger.error(f"Error generating recommendations: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e)) 