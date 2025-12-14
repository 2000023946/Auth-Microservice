from src.app.domain.exceptions import TokenError


# ----------------------------------------------------------------
# Helper: Simple Response Object
# ----------------------------------------------------------------
class HttpResponse:
    def __init__(self, body, status_code=200):
        self.body = body
        self.status_code = status_code
        self.headers = {}

    def set_cookie(
        self, key, value, httponly=True, secure=True, path="/", max_age=None
    ):
        cookie_str = f"{key}={value}; HttpOnly; Secure; Path={path}; SameSite=Lax"
        if max_age:
            cookie_str += f"; Max-Age={max_age}"

        if "Set-Cookie" in self.headers:
            self.headers["Set-Cookie"] += ", " + cookie_str
        else:
            self.headers["Set-Cookie"] = cookie_str


# ----------------------------------------------------------------
# Refresh Token Controller
# ----------------------------------------------------------------
class RefreshTokenController:
    def __init__(self, token_service):
        self.token_service = token_service

    def handle(self, request) -> HttpResponse:
        try:
            # 1. Extract Token
            old_refresh_token = request.cookies.get("refresh_token")

            if not old_refresh_token:
                return HttpResponse({"error": "Missing refresh token"}, status_code=401)

            # 2. Rotate Tokens (Call Service)
            # The service Blacklists the old one and issues a NEW pair
            access_token, new_refresh_token = self.token_service.refresh_token(
                old_refresh_token
            )

            # 3. Build Response
            response = HttpResponse({"message": "Token refreshed"}, status_code=200)

            # 4. Set NEW Cookies
            # Access Token: 15 mins (900s)
            response.set_cookie("access_token", access_token, max_age=900)

            # New Refresh Token: 7 days (604800s)
            response.set_cookie(
                "refresh_token", new_refresh_token, path="/auth/refresh", max_age=604800
            )

            return response

        except TokenError as e:
            # If token is invalid/blacklisted, client must log in again
            return HttpResponse({"error": str(e)}, status_code=401)

        except ValueError:
            # Catches unpacking errors if Service only returns 1 token (Safety net)
            return HttpResponse({"error": "Token rotation failed"}, status_code=500)

        except Exception as e:
            print(f"Refresh Error: {e}")
            return HttpResponse({"error": "Internal Server Error"}, status_code=500)
