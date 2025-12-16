from src.app.domain.exceptions import TokenError
from src.controller.outbound.response_models import UserResponse
import sys  # Added for debug printing


# ----------------------------------------------------------------
# Helper: Simple Response Object (MATCHING REFRESH CONTROLLER)
# ----------------------------------------------------------------
class HttpResponse:
    def __init__(self, body, status_code=200):
        self.body = body
        self.status_code = status_code
        # CHANGED: Use a list of tuples to support multiple Set-Cookie headers
        self.headers = []

    def set_cookie(self, key, value, httponly=True, path="/", max_age=None):
        # 1. Check Env Var directly (Defaults to False/Dev for local testing)
        is_prod = False

        # Force print to stderr for Docker logs
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
            # Lax is safer for local HTTP dev so cookies aren't dropped
            cookie_parts.append("SameSite=Lax")

        if max_age:
            cookie_parts.append(f"Max-Age={max_age}")

        cookie_str = "; ".join(cookie_parts)

        # 4. Append as a tuple (Key, Value)
        self.headers.append(("Set-Cookie", cookie_str))


# ----------------------------------------------------------------
# Controller
# ----------------------------------------------------------------
class SilentAuthController:
    def __init__(self, token_service, user_service):
        self.token_service = token_service
        self.user_service = user_service

    def handle(self, request) -> HttpResponse:
        access_token = request.cookies.get("access_token")
        refresh_token = request.cookies.get("refresh_token")

        print(
            f"SilentAuth Check - Access: {bool(access_token)}, Refresh: {bool(refresh_token)}"
        )

        user_id = None
        new_tokens = None  # Tuple (access, refresh) if rotated

        # 1. STRATEGY A: Try Access Token
        if access_token:
            try:
                # Validates signature and expiry
                user_id = self.token_service.validate_and_get_user_id(access_token)
            except TokenError:
                # Token expired/invalid. Don't fail yet; try refresh.
                pass

        # 2. STRATEGY B: Try Refresh Token (if Access failed)
        if not user_id and refresh_token:
            try:
                # Perform Token Rotation (Blacklist old, issue new pair)
                new_access, new_refresh = self.token_service.refresh_token(
                    refresh_token
                )
                new_tokens = (new_access, new_refresh)

                # Get ID from the token we just minted (safe to skip DB check here)
                user_id = self.token_service.get_user_id_from_token(new_access)
            except TokenError:
                # Refresh failed too (Expired/Blacklisted). Game over.
                pass

        # 3. Final Decision
        if user_id:
            return self._build_success_response(user_id, new_tokens)
        else:
            return HttpResponse({"isAuthenticated": False}, status_code=401)

    def _build_success_response(self, user_id, new_tokens=None):
        """Helper to fetch profile and set cookies if needed."""
        try:
            user = self.user_service.fetchUser(user_id)
            print("fetched user", user)
            # Serialize to JSON
            response_body = UserResponse(id=user_id, email=user.email).model_dump()
            response_body["isAuthenticated"] = True

            response = HttpResponse(response_body, status_code=200)

            # If we rotated tokens, update the cookies!
            if new_tokens:
                new_access, new_refresh = new_tokens
                # Access Token: 15 mins (900s)
                response.set_cookie("access_token", new_access, max_age=900)
                # Refresh Token: 7 days (604800s)
                response.set_cookie("refresh_token", new_refresh, max_age=604800)

            return response
        except Exception as e:
            print(f"SilentAuth Error: {e}", file=sys.stderr)
            return HttpResponse({"isAuthenticated": False}, status_code=401)
