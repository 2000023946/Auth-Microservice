from pydantic import BaseModel, EmailStr, model_validator  # type: ignore
from src.app.domain.exceptions import UserDomainValidationError, EmailAlreadyExistsError
from src.controller.outbound.response_models import UserResponse
from ..outbound.http import HttpResponse
from ...app.services.inbound.user_service import IUserService


# ----------------------------------------------------------------
# Request Schema
# ----------------------------------------------------------------
class RegisterSchema(BaseModel):
    email: EmailStr
    pass1: str
    pass2: str

    @model_validator(mode="after")
    def check_passwords_match(self):
        p1 = self.pass1
        p2 = self.pass2
        if p1 is not None and p2 is not None and p1 != p2:
            raise ValueError("Passwords do not match")
        return self


# ----------------------------------------------------------------
# Controller
# ----------------------------------------------------------------
class RegisterController:
    def __init__(self, user_service: IUserService):
        self.user_service = user_service

    def handle(self, request) -> HttpResponse:
        try:
            print("someone is registering")
            # 1. Validation (Schema)
            # Parses JSON and runs checks (email format, pass1==pass2)
            data = RegisterSchema(**request.json)
            print("valid data")
            # 2. Business Logic
            # Note: Domain rules (strength) are checked inside service.register
            user_dto = self.user_service.register(data.email, data.pass1, data.pass2)
            print("service validated it ", user_dto)
            # 3. Output Mapping
            # Convert DTO to strict JSON response (hides internal fields)
            response_body = UserResponse(
                id=user_dto.user_id, email=user_dto.email
            ).model_dump()
            print("resposne made")
            # 4. Success (201 Created)
            return HttpResponse(body=response_body, status_code=201)

        except ValueError as e:
            print(str(e))
            # Catches Pydantic validation errors (missing fields, mismatch pass)
            return HttpResponse({"error": str(e)}, status_code=400)

        except UserDomainValidationError as e:
            print(str(e))
            # Catches weak passwords from the Domain Layer
            return HttpResponse({"error": str(e)}, status_code=400)

        except EmailAlreadyExistsError as e:
            # Catches duplicate users
            return HttpResponse({"error": str(e)}, status_code=409)

        except Exception as e:
            print("error occured ", str(e))
            return HttpResponse({"error": "Internal Server Error"}, status_code=500)
