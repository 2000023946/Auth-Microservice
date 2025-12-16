from locust import HttpUser, task, between
import itertools

users = itertools.cycle([f"user{i}@test.com" for i in range(100)])


class AuthUser(HttpUser):
    wait_time = between(1, 2)

    def on_start(self):
        # Login: cookies are automatically stored in self.client
        email = next(users)
        self.client.post(
            "/api/auth/login", json={"email": email, "password": "password!@#76"}
        )

    @task(5)
    def me(self):
        # No Authorization header needed; cookies are sent automatically
        self.client.get("/api/auth/me")

    @task(2)
    def refresh(self):
        # Cookies will be sent automatically
        self.client.post("/api/auth/refresh")
