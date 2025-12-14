import json
from pydantic import BaseModel, ValidationError, EmailStr  # type: ignore
from src.app.domain.exceptions import AuthenticationError


# ----------------------------------------------------------------
# Request Schema (Pydantic)
# ----------------------------------------------------------------
class LoginSchema(BaseModel):
    email: EmailStr
    password: str


# ----------------------------------------------------------------
# Helper: Simple Response Object (Simulates Framework Response)
# ----------------------------------------------------------------
class HttpResponse:
    def __init__(self, body, status_code=200):
        self.body = body
        self.status_code = status_code
        self.headers = {}

    def set_cookie(
        self, key, value, httponly=True, secure=True, path="/", max_age=None
    ):
        # Simply appending to a string for this generic implementation.
        # Real frameworks handle multiple cookies better.
        cookie_str = f"{key}={value}; HttpOnly; Secure; Path={path}; SameSite=Lax"
        if max_age:
            cookie_str += f"; Max-Age={max_age}"

        # Append to existing headers or create new
        if "Set-Cookie" in self.headers:
            self.headers["Set-Cookie"] += ", " + cookie_str
        else:
            self.headers["Set-Cookie"] = cookie_str


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
            response.set_cookie(
                "refresh_token", refresh_token, path="/auth/refresh", max_age=604800
            )

            return response

        except ValidationError as e:
            return HttpResponse({"error": str(e)}, status_code=400)

        except AuthenticationError as e:
            return HttpResponse({"error": str(e)}, status_code=401)

        except Exception as e:
            # Global fallback
            print(f"Unexpected Error: {e}")
            return HttpResponse({"error": "Internal Server Error"}, status_code=500)
