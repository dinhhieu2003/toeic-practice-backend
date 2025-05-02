"""
Data fetcher module for retrieving data from the backend API.
"""
import logging
import os
from typing import Dict, List, Any, Optional, Union
import aiohttp
import backoff
from fastapi import HTTPException
from ..utils.config import BACKEND_API_BASE_URL, INTERNAL_API_KEY

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

@backoff.on_exception(backoff.expo, 
                     (aiohttp.ClientError, aiohttp.ClientResponseError),
                     max_tries=3,
                     max_time=10)
async def _make_internal_api_request(
    endpoint: str,
    method: str = "GET",
    params: Optional[Dict[str, Any]] = None,
    data: Optional[Dict[str, Any]] = None,
    headers: Optional[Dict[str, str]] = None
) -> Dict[str, Any]:
    """
    Make an internal API request to the backend service.
    
    Args:
        endpoint: The API endpoint to call (without the base URL)
        method: The HTTP method to use (GET, POST, etc.)
        params: Optional query parameters
        data: Optional request body for POST/PUT requests
        headers: Optional HTTP headers
        
    Returns:
        The JSON response from the API
        
    Raises:
        HTTPException: If the API request fails
    """
    # Ensure endpoint starts with /api/v1/internal and has proper format
    if not endpoint.startswith("/api/v1/internal"):
        endpoint = f"/api/v1/internal{endpoint if endpoint.startswith('/') else '/' + endpoint}"
    
    url = f"{BACKEND_API_BASE_URL}{endpoint}"
    
    # Set default headers for internal API calls
    default_headers = {
        "Content-Type": "application/json",
        "X-Internal-API-Key": INTERNAL_API_KEY
    }
    
    if headers:
        default_headers.update(headers)
    
    try:
        logger.debug(f"Making {method} request to {url}")
        async with aiohttp.ClientSession() as session:
            async with session.request(
                method=method,
                url=url,
                params=params,
                json=data,
                headers=default_headers
            ) as response:
                if response.status >= 400:
                    error_text = await response.text()
                    logger.error(f"API request failed: {response.status} - {error_text}")
                    raise HTTPException(
                        status_code=response.status,
                        detail=f"Backend API error: {error_text}"
                    )
                
                return await response.json()
    
    except aiohttp.ClientError as e:
        logger.error(f"Connection error when calling {url}: {str(e)}")
        raise HTTPException(status_code=503, detail=f"Backend service unavailable: {str(e)}")
    
    except Exception as e:
        logger.error(f"Unexpected error when calling {url}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Internal server error: {str(e)}")

async def get_user_profile(user_id: str) -> Dict[str, Any]:
    """
    Get a user's profile with their learning history
    
    Args:
        user_id: The ID of the user
        
    Returns:
        The user profile data
    """
    logger.info(f"Fetching user profile for user ID: {user_id}")
    endpoint = f"/users/{user_id}/profile"
    
    try:
        response = await _make_internal_api_request(endpoint)
        # Validate response structure
        if not isinstance(response, dict):
            logger.warning(f"Unexpected response type from user profile API: {type(response)}")
            return {"userId": user_id, "testHistory": [], "learningProgress": {}}
            
        return response
    except HTTPException as e:
        if e.status_code == 404:
            logger.warning(f"User profile not found for user ID: {user_id}")
            return {"userId": user_id, "testHistory": [], "learningProgress": {}}
        raise

async def get_test_candidates() -> List[Dict[str, Any]]:
    """
    Get all available tests for recommendation
    
    Returns:
        List of test data
    """
    logger.info("Fetching available tests")
    endpoint = "/tests/candidates"
    
    try:
        response = await _make_internal_api_request(endpoint)
        if not isinstance(response, dict) or "tests" not in response:
            logger.warning("Unexpected response structure from tests API")
            return []
            
        tests = response.get("tests", [])
        if not isinstance(tests, list):
            logger.warning(f"Expected tests to be a list, got {type(tests)}")
            return []
            
        return tests
    except Exception as e:
        logger.error(f"Error fetching test candidates: {str(e)}")
        raise

async def get_lecture_candidates() -> List[Dict[str, Any]]:
    """
    Get all available lectures for recommendation
    
    Returns:
        List of lecture data
    """
    logger.info("Fetching available lectures")
    endpoint = "/lectures/candidates"
    
    try:
        response = await _make_internal_api_request(endpoint)
        if not isinstance(response, dict) or "lectures" not in response:
            logger.warning("Unexpected response structure from lectures API")
            return []
            
        lectures = response.get("lectures", [])
        if not isinstance(lectures, list):
            logger.warning(f"Expected lectures to be a list, got {type(lectures)}")
            return []
            
        return lectures
    except Exception as e:
        logger.error(f"Error fetching lecture candidates: {str(e)}")
        raise

async def get_all_user_profiles_for_similarity() -> List[Dict[str, Any]]:
    """
    Get all user profiles for collaborative filtering
    
    Returns:
        List of user profiles with learning history
    """
    logger.info("Fetching all user profiles for similarity calculation")
    endpoint = "/users/profiles-for-similarity"
    
    try:
        response = await _make_internal_api_request(endpoint)
        if not isinstance(response, dict) or "profiles" not in response:
            logger.warning("Unexpected response structure from profiles API")
            return []
            
        profiles = response.get("profiles", [])
        if not isinstance(profiles, list):
            logger.warning(f"Expected profiles to be a list, got {type(profiles)}")
            return []
            
        return profiles
    except Exception as e:
        logger.error(f"Error fetching user profiles for similarity: {str(e)}")
        raise

async def get_test_details(test_id: str) -> Dict[str, Any]:
    """
    Get detailed information about a specific test
    
    Args:
        test_id: The ID of the test
        
    Returns:
        Test details
    """
    logger.info(f"Fetching details for test ID: {test_id}")
    endpoint = f"/tests/{test_id}"
    
    return await _make_internal_api_request(endpoint)

async def get_lecture_details(lecture_id: str) -> Dict[str, Any]:
    """
    Get detailed information about a specific lecture
    
    Args:
        lecture_id: The ID of the lecture
        
    Returns:
        Lecture details
    """
    logger.info(f"Fetching details for lecture ID: {lecture_id}")
    endpoint = f"/lectures/{lecture_id}"
    
    return await _make_internal_api_request(endpoint)

async def save_recommendation_feedback(user_id: str, item_id: str, item_type: str, feedback: Dict[str, Any]) -> Dict[str, Any]:
    """
    Save user feedback on a recommendation
    
    Args:
        user_id: The ID of the user
        item_id: The ID of the recommended item
        item_type: The type of the item ('test' or 'lecture')
        feedback: The feedback data
        
    Returns:
        Confirmation of saved feedback
    """
    endpoint = "/recommendations/feedback"
    data = {
        "userId": user_id,
        "itemId": item_id,
        "itemType": item_type,
        **feedback
    }
    
    return await _make_internal_api_request(endpoint, method="POST", data=data) 