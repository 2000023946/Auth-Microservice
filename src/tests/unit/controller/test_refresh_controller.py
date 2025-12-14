import pytest
from unittest.mock import Mock
from src.controller.inbound.refresh_controller import RefreshTokenController
from src.app.domain.exceptions import TokenError

# ----------------------------------------------------------------
# Fixtures
# ----------------------------------------------------------------


@pytest.fixture
def mock_token_service():
    return Mock()


@pytest.fixture
def refresh_controller(mock_token_service):
    return RefreshTokenController(mock_token_service)


# ----------------------------------------------------------------
# Happy Path
# ----------------------------------------------------------------


def test_refresh_success_rotates_tokens(refresh_controller, mock_token_service):
    """
    Scenario: Valid refresh cookie.
    Expected:
    1. Service generates NEW Access AND NEW Refresh tokens.
    2. Response 200 OK.
    3. Both Cookies are updated (Rotation).
    """
    # Arrange: Request has old token
    mock_request = Mock()
    mock_request.cookies = {"refresh_token": "old_refresh_token"}

    # Arrange: Service returns NEW PAIR (Rotation)
    # Note: If your current Service only returns one, this test expects the update.
    mock_token_service.refresh_token.return_value = ("new_access", "new_refresh")

    # Act
    response = refresh_controller.handle(mock_request)

    # Assert
    assert response.status_code == 200

    # Verify Cookies
    cookies = response.headers.get("Set-Cookie", "")
    assert "access_token=new_access" in cookies
    assert "refresh_token=new_refresh" in cookies


# ----------------------------------------------------------------
# Error Handling
# ----------------------------------------------------------------


def test_refresh_fails_no_cookie(refresh_controller):
    """
    Scenario: Request missing the refresh cookie.
    Expected: 401 Unauthorized.
    """
    mock_request = Mock()
    mock_request.cookies = {}

    response = refresh_controller.handle(mock_request)

    assert response.status_code == 401
    assert "Missing" in response.body["error"]


def test_refresh_fails_invalid_token(refresh_controller, mock_token_service):
    """
    Scenario: Token is expired or blacklisted.
    Expected: 401 Unauthorized (Force Logout).
    """
    # Arrange
    mock_request = Mock()
    mock_request.cookies = {"refresh_token": "bad_token"}

    # Service raises TokenError (Blacklisted/Expired)
    mock_token_service.refresh_token.side_effect = TokenError("Token is blacklisted")

    # Act
    response = refresh_controller.handle(mock_request)

    # Assert
    assert response.status_code == 401
    assert "Token is blacklisted" in response.body["error"]
