User Authentication Microservice – Layered Controller Overview

This microservice implements a layered architecture for user authentication and session management. The Controller layer acts as the entry point for all user-facing operations and orchestrates requests to the Service layer.

Layer: Controller

The Controller layer is responsible for:

Acting as the entry point for all use cases.

Parsing HTTP requests and validating inputs.

Handling exceptions via global try/catch blocks.

Orchestrating responses to the client.

The microservice currently supports five main controllers:

1. RegisterController

Method: handle(request: HttpRequest) -> JSON Response

Input Body:

{
  "email": "user@example.com",
  "pass1": "password",
  "pass2": "password"
}


Description:

Orchestrates the Sign-Up flow.

Parses and validates input body using a Pydantic/Schema validator (ensures pass1 == pass2).

Validates fields against the register schema.

Calls UserService.register() to handle the business logic.

Catches exceptions like DomainValidationError or EmailAlreadyExistsError.

Returns 201 Created with a serialized User DTO (no sensitive data).

2. LoginController

Method: handle(request: HttpRequest) -> JSON Response

Input Body:

{
  "email": "user@example.com",
  "password": "password"
}


Description:

Orchestrates the Login flow.

Parses input to ensure email/password formats are valid.

Validates schema against the login schema.

Calls UserService.login() to verify credentials against the database.

On success, calls TokenService.create_auth_tokens(user_id) to generate JWT token pair.

Sets Access-Token and Refresh-Token as HttpOnly, Secure cookies in the response.

Returns 200 OK with user profile info.

3. LogoutController

Method: handle(request: HttpRequest) -> JSON Response

Input Cookies:

{
  "refresh_token": "<token>"
}


Description:

Orchestrates the Logout flow.

Extracts refresh_token from the HttpOnly cookies.

Validates input against the token schema.

Calls TokenService.logout(token) to blacklist the token in Redis.

Clears cookies by setting expiry to the past.

Returns 200 OK, confirming session termination.

4. RefreshTokenController

Method: handle(request: HttpRequest) -> JSON Response

Input Cookies:

{
  "refresh_token": "<token>"
}


Description:

Handles token rotation when the Access Token expires.

Extracts refresh_token from cookies and validates the token.

Calls TokenService.refresh_token(), which:

Verifies the token’s validity

Checks Redis blacklist

Blacklists the old token to prevent reuse

Issues a new token pair

Sets new tokens in HttpOnly cookies.

Returns 200 OK.

5. SilentAuthController

Method: handle(request: HttpRequest) -> JSON Response

Input Cookies:

{
  "access_token": "<token>"
}


or

{
  "refresh_token": "<token>"
}


Description:

Handles “App Load” or browser refresh authentication checks.

Middleware likely already verified the token signature.

Controller calls TokenService to verify token against Redis blacklist.

If valid:

Retrieves the latest User payload to hydrate frontend state

Optionally rotates tokens if close to expiry to keep the session alive.