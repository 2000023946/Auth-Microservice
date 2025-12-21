import jwt
from .Ijwt import ITokenAdapter


class JwtAdapter(ITokenAdapter):
    def __init__(self, secret: str, algorithm: str):
        self.secret = secret
        self.algorithm = algorithm

    def encode(self, payload: dict) -> str:
        return jwt.encode(payload, self.secret, algorithm=self.algorithm)

    def decode(self, token: str) -> dict:
        return jwt.decode(token, self.secret, algorithms=[self.algorithm])


# adapter = JwtAdapter(secret="mysecret", algorithm="HS256")

# import time

# now = time.time()  # current timestamp
# delta = 86400  # 1 day in seconds

# payload = {
#     "sub": "1",
#     "type": "refresh",
#     "jti": "1",
#     "exp": now + delta,  # expires in 1 day
#     "iat": now,
# }

# print(payload)

# token = adapter.encode(payload)  # returns str
# decoded = adapter.decode(token)  # returns dict
