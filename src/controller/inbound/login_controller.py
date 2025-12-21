from pydantic import BaseModel, ValidationError, EmailStr  # type: ignore
from src.app.domain.exceptions import AuthenticationError
from ..outbound.http import HttpResponse
from ...app.services.inbound.user_service import IUserService
from ...app.services.inbound.token_service import ITokenService


# ----------------------------------------------------------------
# Request Schema (Pydantic)
# ----------------------------------------------------------------
class LoginSchema(BaseModel):
    email: EmailStr
    password: str


# ----------------------------------------------------------------
# Login Controller
# ----------------------------------------------------------------
class LoginController:
    def __init__(self, user_service: IUserService, token_service: ITokenService):
        self.user_service = user_service
        self.token_service = token_service

    def handle(self, request) -> HttpResponse:
        try:
            print("logging the user")
            # 1. Validation (Pydantic)
            # We assume request.json is a dict. If it's a string, use json.loads(request.body)
            data = LoginSchema(**request.json)
            print("valided teh data")
            # 2. Business Logic (User Service)
            user_dto = self.user_service.login(data.email, data.password)
            print("logged in the user ")
            # 3. Security Logic (Token Service)
            access_token, refresh_token = self.token_service.create_jwt(
                user_dto.user_id,
            )
            print("making the otkens")
            # 4. Build Response
            response = HttpResponse(
                body={
                    "message": "Login successful",
                    "email": user_dto.email,
                    "id": user_dto.user_id,
                },
                status_code=200,
            )
            print("making the reponse")
            # 5. Set Secure Cookies
            # Access Token: 15 mins (900s)
            response.set_cookie("access_token", access_token, max_age=900)

            # Refresh Token: 7 days (604800s), RESTRICTED PATH
            response.set_cookie("refresh_token", refresh_token, max_age=604800)
            print("setting teh coockies")
            return response

        except ValidationError as e:
            return HttpResponse({"error": str(e)}, status_code=400)

        except AuthenticationError as e:
            return HttpResponse({"error": str(e)}, status_code=401)

        except Exception as e:
            # Global fallback
            print(f"Unexpected Error: {e}")
            return HttpResponse({"error": "Internal Server Error"}, status_code=500)
