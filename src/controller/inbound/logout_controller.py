from ..outbound.http import HttpResponse


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
            response.delete_cookie("refresh_token")

            return response

        except Exception as e:
            # Even if something fails, we try to return 200 to ensure the client
            # feels "logged out", but we log the error on the server.
            print(f"Logout Error: {e}")
            return HttpResponse({"error": "Logout failed"}, status_code=500)
