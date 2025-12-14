# ----------------------------------------------------------------
# Helper: Simple Response Object
# ----------------------------------------------------------------
class HttpResponse:
    def __init__(self, body, status_code=200):
        self.body = body
        self.status_code = status_code
        self.headers = {}

    def delete_cookie(self, key, path="/"):
        """
        Clears a cookie by setting its value to empty and expiry to immediate.
        """
        # Max-Age=0 tells the browser to delete it immediately
        cookie_str = f"{key}=; HttpOnly; Secure; Path={path}; Max-Age=0; SameSite=Lax"

        if "Set-Cookie" in self.headers:
            self.headers["Set-Cookie"] += ", " + cookie_str
        else:
            self.headers["Set-Cookie"] = cookie_str


# ----------------------------------------------------------------
# Logout Controller
# ----------------------------------------------------------------
class LogoutController:
    def __init__(self, token_service):
        self.token_service = token_service

    def handle(self, request) -> HttpResponse:
        try:
            # 1. Extract Token from Cookies
            # We specifically want to blacklist the Refresh Token
            token = request.cookies.get("refresh_token")

            # 2. Blacklist Logic
            if token:
                self.token_service.logout(token)

            # 3. Build Response
            response = HttpResponse(
                {"message": "Logged out successfully"}, status_code=200
            )

            # 4. Clear Cookies (The "Logout" action in the browser)
            response.delete_cookie("access_token")
            response.delete_cookie("refresh_token", path="/auth/refresh")

            return response

        except Exception as e:
            # Even if something fails, we try to return 200 to ensure the client
            # feels "logged out", but we log the error on the server.
            print(f"Logout Error: {e}")
            return HttpResponse({"error": "Logout failed"}, status_code=500)
