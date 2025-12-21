import sys


class HttpResponse:
    def __init__(self, body, status_code=200):
        self.body = body
        self.status_code = status_code
        # MUST remain a list of tuples for multiple Set-Cookie headers
        self.headers = []

    def set_cookie(self, key, value, httponly=True, path="/", max_age=None):
        is_prod = False

        cookie_parts = [f"{key}={value}", f"Path={path}"]

        if httponly:
            cookie_parts.append("HttpOnly")

        if is_prod:
            cookie_parts.append("Secure")
            cookie_parts.append("SameSite=None")
        else:
            cookie_parts.append("SameSite=Lax")

        if max_age:
            cookie_parts.append(f"Max-Age={max_age}")

        cookie_str = "; ".join(cookie_parts)
        # Always use .append() for the list
        self.headers.append(("Set-Cookie", cookie_str))

    def delete_cookie(self, key, path="/"):
        """
        Clears a cookie by appending a new Set-Cookie header with Max-Age=0.
        """
        # We match the same flags (HttpOnly/SameSite) to ensure the browser
        # identifies the correct cookie to delete.
        cookie_parts = [
            f"{key}=",
            f"Path={path}",
            "Max-Age=0",
            "HttpOnly",
            "SameSite=Lax",
        ]

        cookie_str = "; ".join(cookie_parts)

        # FIXED: Logic changed from dict-style lookup to simple list append
        self.headers.append(("Set-Cookie", cookie_str))
