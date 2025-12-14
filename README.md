User Authentication Service

This service provides secure and scalable user authentication using JWTs, HTTP-only cookies, Redis, and asynchronous background jobs.

Features

Email-based user authentication

JWT + cookie–based session management

Redis-backed token and session handling

Silent authentication for seamless user experience

Asynchronous welcome email delivery

Authentication Flows
1. Sign Up

Endpoint behavior

Accepts: email, password, confirm_password

Validates that the email does not already exist

Creates a new user record

Issues a JWT and sets an HTTP-only cookie

Enqueues an asynchronous job to send a welcome email

2. Login

Endpoint behavior

Accepts: email, password

Validates that the email exists and the password is correct

Issues a JWT and sets an HTTP-only cookie

Stores the user session in Redis

3. Silent Login (Session Restoration)

Purpose

Allows users to stay logged in across sessions without re-entering credentials

How it works

Client sends the authentication cookie in the request headers

Server verifies the JWT signature

If valid, the user is authenticated automatically

Token & Session Management

JWT + Cookie

JWT is issued on successful login/signup

Stored in an HTTP-only cookie for security

Redis Usage

Stores userId mapped to the decoded JWT payload

Maintains a blacklist of invalidated tokens

Every token validation request checks Redis to ensure the token is not blacklisted

Security Considerations

Passwords are securely hashed before storage

Tokens are invalidated via Redis blacklisting on logout or forced expiration

HTTP-only cookies prevent client-side JavaScript access