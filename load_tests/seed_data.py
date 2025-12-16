import requests

for i in range(100):
    requests.post(
        "http://localhost:5000/api/auth/register",
        json={
            "email": f"user{i}@test.com",
            "pass1": "password!@#76",
            "pass2": "password!@#76",
        },
    )
