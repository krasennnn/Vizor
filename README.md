# Vizor

## Project Overview
Vizor is a full-stack web application designed to manage and analyze TikTok influencer campaigns.

Instead of relying on screenshots, spreadsheets, and manual tracking, the platform provides a structured environment where campaign owners can manage creators, track deliverables, and monitor performance in one place.

The system is built with Spring Boot (backend), React (frontend), PostgreSQL (database), and Docker for containerized deployment.

## Features

### User Authentication & Role-Based Access Control
- Register and Log In: Secure authentication for all users.
- User Roles:
   - Creator: Connect their TikTok accounts, participate in campaigns, and manage their assigned deliverables.
   - Campaign Owner: Create and manage campaigns, assign creators, and track performance and deliverables through the dashboard.
<img width="400" height="650" alt="image_2026-03-22_112514249" src="https://github.com/user-attachments/assets/35e62b03-0ff7-48ff-852b-6fb1ddf74d6a" />

### Campaign Management
- Create and manage campaigns
- Define campaign duration and posting requirements
- Assign creators to campaigns
<img width="420" height="300" alt="image_2026-03-22_114338445" src="https://github.com/user-attachments/assets/c6854db0-0c22-4773-aecb-d78acb6d117c" />


### Creator & Deliverable Tracking
- Track which creators are part of a campaign
- Define expected posts per creator
- Detect missing, late, or completed deliverables
  <img width="2508" height="967" alt="image_2026-03-22_113152418" src="https://github.com/user-attachments/assets/edbd11bb-e308-456a-8b03-3b5f7e3bfacb" />

### Analytics Dashboard
- Store and display video and account performance data
- Aggregate campaign metrics
- View performance per creator and per campaign
<img width="2482" height="1406" alt="image_2026-03-22_112749844" src="https://github.com/user-attachments/assets/3e861afa-4fa2-4ca4-869f-10e9935c0eb7" />

## Technical Details

### Backend
- Spring Boot (Java)
- REST API
- PostgreSQL
- Flyway migrations

### Frontend
- React (Vite + TypeScript)
- Dashboard-based UI

### DevOps
- Dockerized setup
- GitLab CI/CD
- Testcontainers for integration testing

## Architecture
- Three-layer architecture (Controller → Service → Repository)
- DTO-based data flow
- Clear separation of concerns between layers

## API Integration
TikTok API is not integrated in the MVP due to access restrictions.

However, the system is designed for future integration, with data models aligned to TikTok API structures. This allows integration without major refactoring.

## Installation and Setup

### Requirements
- Docker
- Node.js
- Java 17

# Run the Deployed Version (Recommended)

This runs the application using prebuilt Docker images, so no local build is required.

### 1. Clone the repository
```bash
git clone https://github.com/krasennnn/Vizor.git
cd Vizor
```

### 2. Start the application
```bash
docker compose -f docker-compose.prod.yml up
```

### 3. Access the application
- Frontend: http://localhost:3000 (User View)
- Backend: http://localhost:8080
- Database: localhost:5433

## Run the Local Development Version

This builds the backend and frontend locally and is intended for development.

### 1. Clone the repository
```bash
git clone https://github.com/krasennnn/Vizor.git
cd Vizor
```

### 2. Start all services
```bash
docker compose up --build
```

### 3. Access the application
- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- Database: localhost:5433
