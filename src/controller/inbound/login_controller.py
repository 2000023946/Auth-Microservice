import json
from pydantic import BaseModel, ValidationError, EmailStr  # type: ignore
from src.app.domain.exceptions import AuthenticationError


# ----------------------------------------------------------------
# Request Schema (Pydantic)
# ----------------------------------------------------------------
class LoginSchema(BaseModel):
    email: EmailStr
    password: str


import os
import sys  # Import sys to force printing to stderr


# ----------------------------------------------------------------
# Helper: Simple Response Object
# ----------------------------------------------------------------
class HttpResponse:
    def __init__(self, body, status_code=200):
        self.body = body
        self.status_code = status_code
        # Use a list of tuples instead of a dict to allow duplicate headers (like Set-Cookie)
        self.headers = []

    def set_cookie(self, key, value, httponly=True, path="/", max_age=None):
        # 1. Check Env Var directly to avoid import caching issues
        # default to False (Development) if not explicitly 'production'
        is_prod = False

        # Force print to stderr (shows up in Docker logs immediately)
        print(
            f"DEBUG: Setting cookie {key}. Production Mode? {is_prod}", file=sys.stderr
        )

        cookie_parts = [f"{key}={value}", f"Path={path}"]

        # 2. Add flags
        if httponly:
            cookie_parts.append("HttpOnly")

        # 3. Dynamic Secure Flag
        if is_prod:
            cookie_parts.append("Secure")
            cookie_parts.append("SameSite=None")
        else:
            # Lax is safer for local HTTP dev
            cookie_parts.append("SameSite=Lax")

        if max_age:
            cookie_parts.append(f"Max-Age={max_age}")

        cookie_str = "; ".join(cookie_parts)

        # 4. Append as a tuple (Key, Value)
        self.headers.append(("Set-Cookie", cookie_str))


# ----------------------------------------------------------------
# IMPORTANT: Update LoginController to use the new HttpResponse
# ----------------------------------------------------------------
# Ensure your LoginController.handle method returns this updated HttpResponse object.


# ----------------------------------------------------------------
# Login Controller
# ----------------------------------------------------------------
class LoginController:
    def __init__(self, user_service, token_service):
        self.user_service = user_service
        self.token_service = token_service

    def handle(self, request) -> HttpResponse:
        try:
            # 1. Validation (Pydantic)
            # We assume request.json is a dict. If it's a string, use json.loads(request.body)
            data = LoginSchema(**request.json)

            # 2. Business Logic (User Service)
            user_dto = self.user_service.login(data.email, data.password)

            # 3. Security Logic (Token Service)
            access_token, refresh_token = self.token_service.create_jwt(user_dto.id)

            # 4. Build Response
            response = HttpResponse(
                body={
                    "message": "Login successful",
                    "email": user_dto.email,
                    "id": user_dto.id,
                },
                status_code=200,
            )

            # 5. Set Secure Cookies
            # Access Token: 15 mins (900s)
            response.set_cookie("access_token", access_token, max_age=900)

            # Refresh Token: 7 days (604800s), RESTRICTED PATH
            response.set_cookie("refresh_token", refresh_token, max_age=604800)

            return response

        except ValidationError as e:
            return HttpResponse({"error": str(e)}, status_code=400)

        except AuthenticationError as e:
            return HttpResponse({"error": str(e)}, status_code=401)

        except Exception as e:
            # Global fallback
            print(f"Unexpected Error: {e}")
            return HttpResponse({"error": "Internal Server Error"}, status_code=500)
