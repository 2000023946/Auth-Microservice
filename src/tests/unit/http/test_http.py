from src.controller.outbound.http import HttpResponse

# ----------------------------------------------------------------
# 1. Basic Response Tests
# ----------------------------------------------------------------


def test_http_response_initialization():
    """Verify the basic structure of the response object."""
    body = {"data": "test"}
    status = 201

    response = HttpResponse(body, status)

    assert response.body == body
    assert response.status_code == status
    assert isinstance(response.headers, list)
    assert len(response.headers) == 0


# ----------------------------------------------------------------
# 2. Cookie Logic Tests
# ----------------------------------------------------------------


def test_set_cookie_formats_correctly():
    """
    Scenario: Setting a standard session cookie.
    Expected: Header string contains key, value, and security flags.
    """
    response = HttpResponse({"msg": "ok"})

    # Act
    response.set_cookie(key="session", value="secret123", httponly=True, max_age=3600)

    # Assert
    assert len(response.headers) == 1
    key, val = response.headers[0]

    assert key == "Set-Cookie"
    assert "session=secret123" in val
    assert "HttpOnly" in val
    assert "Max-Age=3600" in val
    assert "SameSite=Lax" in val


def test_set_multiple_cookies():
    """
    Scenario: Setting both Access and Refresh tokens.
    Expected: Two distinct tuples in the headers list (Standard HTTP behavior).
    """
    response = HttpResponse({"msg": "ok"})

    # Act
    response.set_cookie("access", "token_a")
    response.set_cookie("refresh", "token_b")

    # Assert
    assert len(response.headers) == 2
    header_keys = [h[0] for h in response.headers]
    header_vals = [h[1] for h in response.headers]

    assert header_keys == ["Set-Cookie", "Set-Cookie"]
    assert any("access=token_a" in v for v in header_vals)
    assert any("refresh=token_b" in v for v in header_vals)


def test_delete_cookie_logic():
    """
    Scenario: Clearing a cookie on logout.
    Expected: Max-Age=0 and empty value.
    """
    response = HttpResponse({"msg": "ok"})

    # Act
    response.delete_cookie("auth_token")

    # Assert
    key, val = response.headers[0]
    assert key == "Set-Cookie"
    assert "auth_token=;" in val
    assert "Max-Age=0" in val
    assert "HttpOnly" in val
