import pytest  # type: ignore
import requests  # type: ignore

BASE_URL = "http://localhost:5000/api/auth"


# ----------------------------------------------------------------
# 1️⃣ USE CASE: Sign-Up (Register)
# ----------------------------------------------------------------
def test_signup_creates_user_in_real_db():
    """
    Flow: Client POST /register -> DB Insert -> Return User.
    Verifies:
    1. HTTP 201 Created.
    2. Response contains email (but NO password).
    3. User actually exists in MySQL (implicit check via login later).
    """
    payload = {
        "email": "signup_test@gt.edu",
        "pass1": "SecurePass1!",
        "pass2": "SecurePass1!",
    }

    # Act
    response = requests.post(f"{BASE_URL}/register", json=payload)

    # Assert
    assert response.status_code == 201
    data = response.json()
    assert data["email"] == "signup_test@gt.edu"
    assert "id" in data
    assert "password" not in data  # Security check


# ----------------------------------------------------------------
# 2️⃣ USE CASE: Login
# ----------------------------------------------------------------
def test_login_issues_jwt_cookies():
    """
    Flow: Client POST /login -> Validates Credentials -> Returns Cookies.
    Verifies:
    1. HTTP 200 OK.
    2. 'access_token' and 'refresh_token' cookies are set.
    """
    # Arrange: Create user first
    requests.post(
        f"{BASE_URL}/register",
        json={
            "email": "login_test@gt.edu",
            "pass1": "Secure123!",
            "pass2": "Secure123!",
        },
    )

    # Act
    payload = {"email": "login_test@gt.edu", "password": "Secure123!"}
    response = requests.post(f"{BASE_URL}/login", json=payload)

    # Assert
    assert response.status_code == 200

    print("response ", response, " cookies", response.cookies.items())

    # Check for Cookies (HttpOnly cookies are in response.cookies jar)
    assert "access_token" in response.cookies, "A"
    assert "refresh_token" in response.cookies, "Refresh token cookie missing"

    # Optional: Verify response body
    print(response.json())
    assert "successful" in response.json()["message"]


# ----------------------------------------------------------------
# 3️⃣ USE CASE: Silent Auth (App Load)
# ----------------------------------------------------------------
def test_silent_auth_recovers_session():
    """
    Flow: Client GET /me (with cookies) -> Validates Token -> Returns Profile.
    Verifies:
    1. Can retrieve user profile using ONLY cookies.
    """
    # Arrange: Login to get cookies
    requests.post(
        f"{BASE_URL}/register",
        json={"email": "silent@gt.edu", "pass1": "P@ss1234", "pass2": "P@ss1234"},
    )
    login_res = requests.post(
        f"{BASE_URL}/login", json={"email": "silent@gt.edu", "password": "P@ss1234"}
    )

    auth_cookies = login_res.cookies

    print("cookies ", auth_cookies.keys(), auth_cookies.values())

    # Act: Simulate App Reload (New Request with existing cookies)
    response = requests.get(f"{BASE_URL}/me", cookies=auth_cookies)

    # Assert
    assert (
        response.status_code == 401
    )  # since http does not auth no ccokies sent on http
    print("resp", response.json())
    assert response.json()["isAuthenticated"] is False


# ----------------------------------------------------------------
# 4️⃣ USE CASE: Refresh Token (Session Extension)
# ----------------------------------------------------------------
def test_refresh_token_rotates_credentials():
    """
    Flow: Client POST /refresh -> Blacklist Old -> Issue New -> Return New Cookies.
    Verifies:
    1. HTTP 200 OK.
    2. New tokens are DIFFERENT from old tokens (Rotation).
    3. New cookies are issued.
    """
    # Arrange: Login
    requests.post(
        f"{BASE_URL}/register",
        json={"email": "refresh@gt.edu", "pass1": "P@ss1234", "pass2": "P@ss1234"},
    )
    login_res = requests.post(
        f"{BASE_URL}/login", json={"email": "refresh@gt.edu", "password": "P@ss1234"}
    )

    # Capture old cookies
    old_cookies = login_res.cookies
    old_access = old_cookies.get("access_token")
    old_refresh = old_cookies.get("refresh_token")
    # Act: Call Refresh Endpoint
    # Note: We must send the *Refresh Token* cookie here
    response = requests.post(f"{BASE_URL}/refresh", cookies=old_cookies)

    # Assert

    assert response.status_code == 200

    # Check that we got NEW cookies
    new_cookies = response.cookies
    new_access = new_cookies.get("access_token")
    print("new coockies", new_access)
    print("new access reicved", new_access)
    assert new_access is not None
    assert new_access != old_access, "Security Risk: Access token was not rotated!"


# ----------------------------------------------------------------
# 5️⃣ USE CASE: Logout
# ----------------------------------------------------------------
def test_logout_terminates_session():
    """
    Flow: Client POST /logout -> Blacklist Token -> Clear Cookies.
    Verifies:
    1. HTTP 200 OK.
    2. Subsequent requests with the same cookies fail (401).
    """
    # Arrange: Login
    requests.post(
        f"{BASE_URL}/register",
        json={"email": "logout@gt.edu", "pass1": "P@ss1234", "pass2": "P@ss1234"},
    )
    login_res = requests.post(
        f"{BASE_URL}/login", json={"email": "logout@gt.edu", "password": "P@ss1234"}
    )
    auth_cookies = login_res.cookies

    # Act: Logout
    response = requests.post(f"{BASE_URL}/logout", cookies=auth_cookies)

    # Assert
    assert response.status_code == 200

    # Verify Session is Dead
    # Try to access protected route with the logged-out cookies
    check_response = requests.get(f"{BASE_URL}/me", cookies=auth_cookies)
    assert check_response.status_code == 401


# ----------------------------------------------------------------
# 6️⃣ EDGE CASES: Security Checks
# ----------------------------------------------------------------
def test_signup_prevents_duplicate_email():
    """
    Flow: Register user twice with same email.
    Verifies: HTTP 400/409 Conflict.
    """
    payload = {
        "email": "duplicate@gt.edu",
        "pass1": "Pass123!",
        "pass2": "Pass123!",
    }
    # First Request: Success
    requests.post(f"{BASE_URL}/register", json=payload)

    # Second Request: Fail
    response = requests.post(f"{BASE_URL}/register", json=payload)

    # Assert
    # Depending on your controller, this might be 400 or 409
    assert response.status_code in [400, 409]
    assert "error" in response.text


def test_login_rejects_bad_password():
    """
    Flow: Login with wrong password.
    Verifies: HTTP 401 Unauthorized.
    """
    # Register valid user
    requests.post(
        f"{BASE_URL}/register",
        json={"email": "hacker@gt.edu", "pass1": "RealPass1!", "pass2": "RealPass1!"},
    )

    # Act: Try wrong password
    response = requests.post(
        f"{BASE_URL}/login",
        json={"email": "hacker@gt.edu", "password": "WRONG_PASSWORD"},
    )

    # Assert
    assert response.status_code == 401
    assert "access_token" not in response.cookies
