# Client (Authentication Frontend)

This client is a React-based authentication interface providing user login and registration flows with real-time form validation and API communication via an Nginx-backed `/api` gateway.

## Features

- Email/password login and registration
- Client-side form validation (email, password, terms)
- Reusable form components and custom hooks
- Clean separation of UI, validation, and API logic
- API requests routed through `/api/**` (no direct backend exposure)

## Architecture

- **Pages**
  - Login (email/password)
  - Sign up (email, password confirmation, terms agreement)

- **Components**
  - `InputField` – controlled input with animated labels and validation feedback
  - `CheckBox` – custom terms agreement checkbox
  - `SubmitButton` – reusable submit actions

- **Hooks & Utilities**
  - `useForm` – centralized form state and validation handling
  - `validators` – email and password validation logic
  - `api` – fetch wrapper for backend communication

## API Integration

All requests are sent through the Nginx API gateway:

- `POST /api/auth/login`
- `POST /api/auth/register`

This enables a single-origin setup and avoids CORS issues in production.

## Tech Stack

- React
- React Router
- JavaScript (ES6+)
- CSS (custom styles)
