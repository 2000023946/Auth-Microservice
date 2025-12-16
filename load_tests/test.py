import requests

BASE = "http://localhost:5000"

# Login
r = requests.post(
    f"{BASE}/api/auth/login",
    json={"email": "user0@test.com", "password": "password!@#76"},
)
print("Login:", r.json())
cookies = r.cookies

# Silent auth /me
r = requests.get(f"{BASE}/api/auth/me", cookies=cookies)
print("Silent Auth:", r.json())

# Logout
r = requests.post(f"{BASE}/api/auth/logout", cookies=cookies)
print("Logout:", r.json())
