Auth Service – Flow Overview

This Auth Service implements a layered architecture with clear separation of responsibilities between Controller, Service, Repository, and Cache layers.

The service handles Sign-Up, Login, Silent Auth, Refresh Token, and Logout flows with JWT authentication and Redis-based token management.

1️⃣ Sign-Up (Register)

Flow:

Client
(email, pass1, pass2)
   ↓
Controller
- Parses input using Zod schema
- Calls UserService.create_user
- Handles global try/catch
   ↓
UserService
- Validates input
- Creates User domain object
- Calls UserRepo.create_user
   ↓
UserRepo
- Calls SQL procedure to insert user
   ↓
Recursive return back to Client


Description:

Input validation ensures pass1 == pass2 and email format is correct.

Password is hashed in the Domain/User object before saving.

Returns serialized UserDTO without sensitive data.

2️⃣ Login

Flow:

Client
(email, pass)
   ↓
Controller
- Parses input with Zod schema
- Calls UserService.validate_credentials
- Calls TokenService to generate JWT tokens
   ↓
UserService
- Validates input
- Creates User domain object
- Calls UserRepo to fetch credentials from DB
   ↓
TokenService
- Creates JWT access + refresh tokens
- Sets cookies in response
   ↓
UserRepo
- Calls SQL procedure to validate credentials
   ↓
Recursive return back to Client


Description:

Verifies credentials with hashed password.

On success, generates secure HttpOnly JWT cookies for session management.

3️⃣ Silent Auth (App Load)

Flow:

Client
(Header: JWT cookie)
   ↓
Middleware
- Validates token signature
- Ensures refresh token is valid
   ↓
Controller
- Handles try/catch errors
- Calls TokenService to refresh session if needed
   ↓
TokenService
- Checks token state and blacklist via Redis
- Issues new token if close to expiry
- Retrieves user payload
   ↓
RedisService
- Handles token blacklist and payload retrieval
   ↓
Recursive return back to Client


Description:

Keeps session alive when the app reloads.

Optionally rotates tokens to maintain continuous authentication.

4️⃣ Refresh Token

Flow:

Client
(Header: JWT cookie – refresh token)
   ↓
Controller
- Validates refresh token
- Calls TokenService to generate new tokens
   ↓
TokenService
- Blacklists old token in Redis
- Retrieves payload from old token
- Issues new JWT token pair
   ↓
RedisService
- Handles blacklist and payload retrieval
   ↓
Recursive return back to Client


Description:

Ensures old tokens cannot be reused.

Maintains session security by rotating tokens.

5️⃣ Logout

Flow:

Client
(Header: JWT cookie)
   ↓
Controller
- Reads token from input
- Calls TokenService to logout
   ↓
TokenService
- Blacklists token in Redis
- Removes any session data
   ↓
RedisService
- Stores blacklist entry
   ↓
Recursive return back to Client


Description:

Terminates user session completely.

Ensures tokens cannot be reused.