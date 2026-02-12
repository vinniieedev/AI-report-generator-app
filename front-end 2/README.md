# AI Report Generation Tool – Frontend

This repository contains the **frontend application** for an AI-powered report generation platform.
The application is built using **React + TypeScript + Vite**, styled with **Tailwind CSS**, and uses **shadcn/ui** for scalable, accessible UI components.


---

## 1. Tech Stack Overview

### Core Technologies
- **React 19** – UI framework
- **TypeScript** – Type safety
- **Vite** – Fast dev & build tool
- **Tailwind CSS** – Utility-first styling
- **shadcn/ui** – Reusable UI components
- **ESLint** – Code quality
- **Modern SaaS UI patterns** – Dashboard-first architecture

---

## 2. Project Goals

- Clean, modern **enterprise SaaS UI**
- Scalable layout for **public pages, user dashboard, and admin panel**
- Component-driven architecture
- Ready for backend integration (Spring Boot + AI layer)

---

## 3. Folder Structure (Proposed)

```src/
│
├── app/
│   ├── routes/              # Page-level components
│   ├── layouts/             # Public, Auth, Dashboard layouts
│   ├── providers/           # Theme, Auth, Query providers
│
├── components/
│   ├── ui/                  # shadcn generated components
│   ├── common/              # Navbar, Footer, Sidebar
│   ├── dashboard/           # Dashboard-specific components
│
├── features/
│   ├── auth/
│   ├── reports/
│   ├── billing/
│   ├── admin/
│
├── hooks/
├── lib/                     # utils, cn(), constants
├── services/                # API layer (axios/fetch)
├── styles/
├── types/
│
├── main.tsx
└── index.css
```
## 4. Layout Architecture

### Layouts & Usage

| Layout            | Used For                                   |
|-------------------|---------------------------------------------|
| **PublicLayout**  | Home, Pricing, Blog, About                  |
| **AuthLayout**    | Login, Signup, Forgot Password              |
| **DashboardLayout** | User dashboard                            |
| **AdminLayout**   | Admin dashboard                             |

### Common Layout Elements
- Shared navigation
- Sidebar (if applicable)
- Footer (public pages only)

---

## 5. Public Pages Implementation

### 5.1 Home Page Sections

1. Sticky Top Navigation
2. Hero Section (CTA + Preview)
3. How It Works (3 cards)
4. Who It’s For (roles grid)
5. Sample Report Preview
6. Key Features (2 × 3 grid)
7. Pricing Preview
8. Final CTA
9. Footer

#### Reusable Components
- `<Navbar />`
- `<Hero />`
- `<FeatureCard />`
- `<PricingCard />`
- `<Footer />`

---

### 5.2 Industries Page

- Hero text
- Grid of industry cards

Each card includes:
- Industry name
- Supported report types
- CTA

---

### 5.3 Pricing Page

- Pricing cards (Free / Pro / Enterprise)
- Feature comparison table
- FAQ accordion (shadcn Accordion)

---

### 5.4 Sample Reports Page

- Tabs (Business / Marketing / Finance)
- Report preview cards
- Download / View CTA

---

### 5.5 Blog / Resources

- Blog listing grid
- Blog card includes:
  - Title
  - Summary
  - Read more link

---

### 5.6 About Page

- Mission
- Problem statement
- Vision
- Roadmap snapshot

---

### 5.7 Contact Page

- Contact form
- Support email
- Address placeholder

---

## 6. Authentication Pages

### Shared Auth Card Component
- Centered container
- Form validation ready
- Consistent spacing

### Pages
- Login
- Signup
- Forgot Password

---

## 7. User Dashboard

### 7.1 Global Dashboard Layout

#### Sidebar
- Dashboard
- Create Report
- My Reports
- Drafts
- Exports
- Billing
- Profile

#### Top Bar
- User avatar
- Credits remaining

---

### 7.2 Dashboard Overview

- Stats cards
- Recent reports
- CTA: **Create New Report**

---

### 7.3 Create Report Wizard

#### Stepper-Based Flow
1. Industry
2. Report Type
3. Audience & Purpose
4. Tone & Depth
5. Data Upload
6. Review & Generate

#### shadcn Components
- Custom Stepper
- Select
- Radio
- Slider
- File Upload

---

### 7.4 Report Preview & Editor

- Section list sidebar
- Editable content blocks

#### Actions
- Regenerate
- Save Draft
- Export

---

### 7.5 My Reports / Drafts / Exports

- Table view
- Filters
- Resume / Download actions

---

### 7.6 Billing

- Current plan
- Usage statistics
- Upgrade CTA
- Invoice list

---

### 7.7 Profile Settings

- Personal information
- Password update

---

## 8. Admin Dashboard

### Admin Sidebar Additions
- Overview
- User Management
- Industry Management
- Template Management
- AI Usage Analytics
- System Settings

---

## 9. State & Data Handling (Planned)

- Local UI state using React hooks
- API layer under `services/`

### Future Enhancements
- React Query / TanStack Query
- Role-based route guards

---

## 10. Design Principles

- Content-first layout
- Clear visual hierarchy
- Minimal color palette
- Reusable components
- Dashboard scalability

---

## 11. Next Steps

1. Initialize Tailwind CSS & shadcn/ui
2. Create base layouts
3. Implement public pages
4. Build dashboard shell
5. Integrate backend APIs
6. Add authentication & role guards
7. Optimize and polish UI

---

## 12. License

**Private – Internal Project**