# AI Report Tool - SaaS Platform

A full-stack AI-powered financial report generation platform built with React + TypeScript (frontend) and Spring Boot (backend).

## Features

- **16+ Financial Calculators**: EMI, SIP, ROI, Tax, Portfolio Risk, and more
- **AI-Powered Report Generation**: Integrates with OpenAI for intelligent report generation
- **Credit-Based System**: Users purchase credits to generate reports
- **Subscription Plans**: Free, Pro, and Enterprise tiers
- **Role-Based Access**: Admin and User dashboards
- **JWT Authentication**: Secure authentication with Google OAuth support
- **Dynamic Report Configuration**: Admin-configurable report templates

## Tech Stack

### Frontend
- React 18 with TypeScript
- Vite for build tooling
- TailwindCSS + shadcn/ui for styling
- Framer Motion for animations
- React Router for navigation

### Backend
- Spring Boot 3.x
- PostgreSQL database
- JWT + Spring Security
- JPA/Hibernate ORM
- Google OAuth2 integration

## Getting Started

### Prerequisites
- Node.js 18+
- Java 17+
- PostgreSQL 15+
- Maven or use the included wrapper

### Environment Setup

1. **Backend Configuration**

Copy the example environment file:
```bash
cp backend/.env.example backend/.env
```

Configure the following in `backend/.env`:
- `DATABASE_URL`: PostgreSQL connection string
- `JWT_SECRET`: Secret key for JWT (min 32 characters)
- `OPENAI_API_KEY`: Your OpenAI API key (optional, enables AI generation)
- `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET`: For Google OAuth

2. **Frontend Configuration**

```bash
# frontend/.env
VITE_API_URL=http://localhost:8080/api
```

### Running Locally

**Using Docker Compose (Recommended)**
```bash
docker-compose up -d
```

**Manual Setup**

1. Start PostgreSQL and create database:
```sql
CREATE DATABASE reportdb;
```

2. Start Backend:
```bash
cd backend
./mvnw spring-boot:run
```

3. Start Frontend:
```bash
cd frontend
yarn install
yarn dev
```

### Access Points
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- API Documentation: http://localhost:8080/swagger-ui.html (if enabled)

## Project Structure

```
/app/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/com/paysecure/ai_report_tool_backend/
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # Business logic
│   │   ├── repository/      # Data access
│   │   ├── model/           # JPA entities
│   │   ├── dto/             # Data transfer objects
│   │   ├── security/        # JWT & OAuth
│   │   ├── config/          # App configuration
│   │   └── exception/       # Error handling
│   └── pom.xml
│
├── frontend/                # React + TypeScript frontend
│   ├── src/
│   │   ├── components/      # Reusable UI components
│   │   ├── pages/           # Route pages
│   │   ├── services/        # API client
│   │   ├── hooks/           # Custom hooks
│   │   ├── auth/            # Auth logic
│   │   └── types/           # TypeScript types
│   └── package.json
│
└── docker-compose.yml       # Docker setup
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `GET /api/auth/me` - Get current user

### Reports
- `GET /api/reports` - List user reports
- `POST /api/reports` - Create report
- `POST /api/reports/{id}/generate` - Generate report with AI
- `GET /api/reports/{id}` - Get report details

### Tools
- `GET /api/tools` - List available tools/calculators
- `GET /api/tools/{id}` - Get tool details
- `GET /api/tools/{id}/fields` - Get input fields for tool

### Credits & Payments
- `GET /api/credits/balance` - Get credit balance
- `GET /api/credits/transactions` - Get transaction history
- `GET /api/payments/packages` - List credit packages
- `POST /api/payments/purchase` - Purchase credits

### Subscriptions
- `GET /api/subscriptions/plans` - List plans
- `GET /api/subscriptions/current` - Get current subscription
- `POST /api/subscriptions/subscribe` - Subscribe to plan

## Configuration

### OpenAI Integration
The platform supports OpenAI for AI-powered report generation. Without an API key, reports are generated with mock content (demo mode).

### Paysecure Payment Gateway
Payment processing is integrated with Paysecure. In demo mode (no API key), purchases are auto-completed for testing.

### Google OAuth
To enable Google login:
1. Create a project in Google Cloud Console
2. Enable OAuth 2.0
3. Add `http://localhost:8080/oauth2/callback/google` as redirect URI
4. Set client ID and secret in environment

## License

MIT License
