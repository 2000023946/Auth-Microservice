from src.app.domain.exceptions import TokenError
from src.controller.outbound.response_models import UserResponse


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
# Controller
# ----------------------------------------------------------------
class SilentAuthController:
    def __init__(self, token_service, user_service):
        self.token_service = token_service
        self.user_service = user_service

    def handle(self, request) -> HttpResponse:
        access_token = request.cookies.get("access_token")
        refresh_token = request.cookies.get("refresh_token")

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
            # Fetch User Profile
            user_dto = self.user_service.get_user_by_id(user_id)

            # Serialize to JSON
            response_body = UserResponse(
                id=user_dto.id, email=user_dto.email
            ).model_dump()
            response_body["isAuthenticated"] = True

            response = HttpResponse(response_body, status_code=200)

            # If we rotated tokens, update the cookies!
            if new_tokens:
                new_access, new_refresh = new_tokens
                response.set_cookie("access_token", new_access, max_age=900)
                response.set_cookie(
                    "refresh_token", new_refresh, path="/auth/refresh", max_age=604800
                )

            return response
        except Exception:
            # User ID in token but User not in DB? (Rare edge case)
            return HttpResponse({"isAuthenticated": False}, status_code=401)
