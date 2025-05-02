# TOEIC Practice Recommender Service

A FastAPI-based microservice that provides personalized TOEIC test and lecture recommendations.

## Project Structure

```
/recommender_service
|-- app/
|   |-- __init__.py
|   |-- main.py             # FastAPI app instance and endpoints
|   |-- logic/
|   |   |-- __init__.py
|   |   |-- core_recommend.py # Core recommendation logic
|   |   |-- data_fetcher.py   # Backend API calling functions
|   |   |-- similarity.py     # Similarity calculation logic
|   |   |-- cold_start.py     # Cold start logic
|   |-- utils/
|   |   |-- __init__.py
|   |   |-- config.py         # Configuration loading
|-- requirements.txt
|-- Dockerfile
|-- .env.example            # Example environment variables
```

## Setup

### Prerequisites

- Python 3.11+
- pip

### Local Development

1. Clone the repository
2. Create a virtual environment:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows, use: venv\Scripts\activate
   ```
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
4. Create a `.env` file with your configuration (use `.env.example` as a template)
5. Run the server:
   ```bash
   uvicorn app.main:app --reload
   ```
6. The API will be available at http://localhost:8000

### Docker

1. Build the Docker image:
   ```bash
   docker build -t recommender-service .
   ```
2. Run the container:
   ```bash
   docker run -p 8000:8000 --env-file .env recommender-service
   ```

## API Endpoints

- `GET /`: Basic API information
- `GET /health`: Health check endpoint
- Additional endpoints will be implemented for recommendation functionality

## Environment Variables

- `BACKEND_API_BASE_URL`: URL of the TOEIC Practice backend internal API
- `INTERNAL_API_KEY`: API key for authenticating with the backend
- `LOG_LEVEL`: Logging level (default: INFO) 