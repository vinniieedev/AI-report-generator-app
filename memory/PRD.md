# AI Report Tool - Product Requirements Document

## Original Problem Statement
Transform an existing AI Report Tool project (React + TypeScript + Spring Boot) from a portfolio project into a production-grade SaaS platform with:
- Complete report generation with OpenAI integration (decoupled, user provides API key)
- Credit-based monetization system with Paysecure payment gateway
- Google OAuth authentication alongside existing JWT auth
- PostgreSQL database for production use
- File upload support (PDF, Excel, Word, CSV, JSON, TXT)
- PDF export for generated reports
- Data visualization with interactive charts (pie, bar, line, doughnut)

## Architecture Overview

### Tech Stack
- **Frontend**: React 18 + TypeScript + Vite + TailwindCSS + shadcn/ui + Chart.js
- **Backend**: Spring Boot 3.x + Java 17
- **Database**: PostgreSQL 15
- **Auth**: JWT + Google OAuth 2.0
- **File Processing**: Apache POI (Excel/Word), PDFBox (PDF), Gson (JSON)
- **PDF Generation**: iText 7

### Domain Model
1. **Report Definition** (Admin-controlled)
   - ReportTemplate
   - InputField (dynamic form fields)
   - ReportPromptConfig (AI prompts)

2. **Report Execution** (User-generated)
   - Report (with inputs and generated content)
   - ReportInput (dynamic inputs per report)
   - ReportChart (visualization data)
   - UploadedFile (user-uploaded data files)
   - AIRequest (OpenAI call tracking)

3. **Commercial Layer** (Monetization)
   - SubscriptionPlan (Free, Pro, Enterprise)
   - UserSubscription
   - CreditWallet (balance)
   - CreditTransaction (ledger)
   - Payment (Paysecure integration)

## Core Requirements

### User Personas
1. **End User**: Creates reports, uploads files, views charts, exports PDFs
2. **Admin**: Manages templates, users, subscriptions, AI prompts

### Features

#### Authentication
- [x] JWT-based login/register
- [x] Google OAuth integration (UI ready, backend configured)
- [x] Role-based access (USER, ADMIN)

#### Report Generation
- [x] 16 financial calculators (EMI, SIP, ROI, Tax, etc.)
- [x] Multi-step wizard flow (Industry → Report Type → Audience → Tone → Data → Review)
- [x] Dynamic input fields per template
- [x] OpenAI integration for AI-powered generation
- [x] Mock generation fallback when OpenAI not configured

#### File Upload & Analysis
- [x] Support for PDF, Excel (.xlsx, .xls), Word (.docx), CSV, JSON, TXT
- [x] Automatic text extraction and parsing
- [x] Structured data extraction from spreadsheets
- [x] Column statistics calculation (sum, avg, min, max)
- [x] File data included in AI prompts for analysis

#### Data Visualization
- [x] Pie charts for distribution analysis
- [x] Bar charts for comparisons
- [x] Line charts for trends
- [x] Doughnut charts for proportions
- [x] Auto-generated demo charts in mock mode
- [x] AI-generated chart recommendations (with OpenAI)

#### Export & Download
- [x] PDF export with professional formatting
- [x] Markdown export
- [x] Brand-colored PDF styling
- [x] Chart section in PDF reports

#### Subscription & Credits
- [x] 3 subscription plans (Free, Pro, Enterprise)
- [x] Credit wallet with transaction ledger
- [x] Credit packages for purchase
- [x] Paysecure payment integration (demo mode available)

#### Dashboard
- [x] User dashboard with stats, recent reports
- [x] My Reports page with view/download
- [x] Report Viewer with interactive charts
- [x] Billing page with plans and credit packages

## What's Been Implemented (Feb 2, 2026)

### Backend (Spring Boot)
- [x] PostgreSQL configuration
- [x] Entities: SubscriptionPlan, UserSubscription, CreditWallet, CreditTransaction, Payment, AIRequest, ReportPromptConfig, ReportInput, UploadedFile, ReportChart
- [x] Services: CreditService, SubscriptionService, PaymentService, OpenAIService, FileParserService, PdfGenerationService
- [x] File parsing: PDF, Excel, Word, CSV, JSON, TXT
- [x] PDF generation with iText 7
- [x] Controllers: FileUploadController, ReportExportController
- [x] Docker configuration (Dockerfile, docker-compose.yml)

### Frontend (React + TypeScript)
- [x] API client with typed services including file upload
- [x] File upload UI with drag-and-drop
- [x] Chart rendering with Chart.js (pie, bar, line, doughnut)
- [x] Report Viewer page with charts and export buttons
- [x] PDF and Markdown export functionality

## Backlog / Future Features

### P0 (Critical)
- [ ] Google OAuth callback handler (backend endpoint)
- [ ] Token refresh mechanism
- [ ] Error boundary for API failures

### P1 (High Priority)
- [ ] Admin dashboard for template management
- [ ] Custom input fields per template (admin configurable)
- [ ] Chart images in PDF export
- [ ] Email notifications

### P2 (Medium Priority)
- [ ] Webhook for Paysecure payment confirmation
- [ ] Usage analytics dashboard
- [ ] Rate limiting
- [ ] Audit logging

### P3 (Nice to Have)
- [ ] Dark mode toggle
- [ ] Multi-language support
- [ ] Report sharing/collaboration
- [ ] API access for Enterprise tier

## Environment Setup

### Required Environment Variables
```
# Backend
DATABASE_URL=jdbc:postgresql://localhost:5432/reportdb
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
JWT_SECRET=<32+ char secret>
OPENAI_API_KEY=<optional>
GOOGLE_CLIENT_ID=<optional>
GOOGLE_CLIENT_SECRET=<optional>
PAYSECURE_API_KEY=<optional>

# Frontend
VITE_API_URL=http://localhost:8080/api
```

## Next Steps
1. Set up PostgreSQL database locally
2. Configure Google OAuth credentials (Google Cloud Console)
3. Add OpenAI API key for AI generation
4. Run backend: `./mvnw spring-boot:run`
5. Run frontend: `yarn dev`
