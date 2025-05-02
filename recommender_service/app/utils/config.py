"""
Configuration module for the recommender service.
Loads all required configuration from environment variables.
"""
import os
from dotenv import load_dotenv
import logging

load_dotenv()

# Configure logging
logger = logging.getLogger(__name__)

# API Configuration
BACKEND_API_BASE_URL = os.getenv("BACKEND_API_BASE_URL", "http://backend-api:8000")
INTERNAL_API_KEY = os.getenv("INTERNAL_API_KEY")

# Recommendation algorithm weights
WEIGHT_TOPIC = float(os.getenv("WEIGHT_TOPIC", "10"))
WEIGHT_COLLAB = float(os.getenv("WEIGHT_COLLAB", "5"))
TARGET_MARGIN = int(os.getenv("TARGET_MARGIN", "50"))

# Validate critical configuration
if not INTERNAL_API_KEY:
    logger.warning("INTERNAL_API_KEY not set in environment variables. Internal API calls will fail.")
    
if not BACKEND_API_BASE_URL:
    logger.warning("BACKEND_API_BASE_URL not set, using default value: http://backend-api:8000")

# Log configuration (without sensitive values)
logger.info(f"Backend API Base URL: {BACKEND_API_BASE_URL}")
logger.info(f"Internal API Key configured: {'Yes' if INTERNAL_API_KEY else 'No'}")
logger.info(f"Recommendation weights - Topic: {WEIGHT_TOPIC}, Collaborative: {WEIGHT_COLLAB}")
logger.info(f"Target margin: {TARGET_MARGIN}") 