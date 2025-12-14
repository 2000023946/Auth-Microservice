from pydantic import BaseModel, EmailStr, ConfigDict  # type: ignore

# ----------------------------------------------------------------
# Success Responses
# ----------------------------------------------------------------


class UserResponse(BaseModel):
    """
    Defines the shape of the JSON response for a User.
    Strictly excludes sensitive fields like 'password_hash'.
    """

    id: int
    email: EmailStr

    # OLD WAY (Deprecated):
    # class Config:
    #     from_attributes = True

    # NEW WAY (Pydantic V2):
    model_config = ConfigDict(from_attributes=True)


# ----------------------------------------------------------------
# Error Responses
# ----------------------------------------------------------------


class ErrorResponse(BaseModel):
    error: str
    code: int
