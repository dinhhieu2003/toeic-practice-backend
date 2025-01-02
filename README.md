# UTE TOEIC

## Overview
**UTE TOEIC** is a multi-platform learning application designed to help students prepare for TOEIC exams. It offers features such as realistic mock tests, role-based user management, and support for uploading test content via Excel, images, and audio files. The project includes a web application, Android app, and Chrome extension to provide seamless access across devices.

## Features
- **Mock TOEIC Tests**: Simulates TOEIC exams with 99% accuracy.
- **Content Management**: Allows uploading and managing test content through Excel, images, and audio files.
- **Offline Functionality**: Utilizes Web Workers and IndexedDB for efficient data handling.
- **Progress Tracking**: Integrated with Chart.js for detailed user progress visualization.
- **Role-based Access Control**: Differentiates user privileges (e.g., admins, students).
- **Multi-platform Support**: Accessible via web, Android, and Chrome extension.
- **Authentication**: Secured with Google OAuth2 for user login and access control.

## Technologies
### Frontend (Web)
- **Languages**: TypeScript, JavaScript
- **Frameworks/Libraries**: ReactJS, PrimeReact, PrimeFlex
- **Tools**: Chart.js, React.lazy, Web Workers, IndexedDB
- **Optimization**: Prefetching techniques and async/await for improved performance

### Backend
- **Technology**: Java Spring, Spring Data MongoDB, Spring Security, Oauth2 resource server, Map struct, Swagger, Apache POI OOXML, Apache commons text
- **Database**: MongoDB
- **Hosting**: Azure

### Mobile
- **Framework**: React Native

### Chrome Extension
- Built with web technologies to integrate with the browser seamlessly.

## Installation
### Prerequisites
- **Node.js** and **npm**
- **MongoDB** for backend database
- **Java** for Spring Boot backend
- **Maven** for build project
- A Google Cloud project with OAuth2 credentials

### Steps to Run
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/dinhhieu2003/toeic-practice-backend.git
   cd toeic-practice-backend
2. **Build project**
    ```bash
    mvn clean install
3. **Run backend server**
    ```bash
    java -jar target/toeic-practice-backend-0.0.1-SNAPSHOT.jar
4. **Access application**
    ```bash
    http://localhost:8080

### Steps to run by Docker
1. **Build Docker**
    ```bash
    docker build -t toeic-backend .
2. **Run Docker**
    ```bash
    docker run -d -p 8080:8080 --name toeic-backend toeic-backend
3. **Acccess application**
    ```bash
    Access application via http://localhost:8080