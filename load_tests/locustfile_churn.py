from locust import HttpUser, task, between
import itertools

# Pool of emails to cycle through
users = itertools.cycle([f"user{i}@test.com" for i in range(100)])


class ChurnUser(HttpUser):
    wait_time = between(2, 5)

    @task
    def life_cycle(self):
        # 1. Login (Start a new session)
        email = next(users)
        self.client.post(
            "/api/auth/login", json={"email": email, "password": "password!@#76"}
        )

        # 2. Do some work (Simulate short activity)
        self.client.get("/api/auth/me")
        self.client.post("/api/auth/refresh")

        # 3. Logout (Cleanly destroy the session)
        self.client.post("/api/auth/logout")

        # 4. Loop finishes, and Locust immediately starts this function again
        #    which forces a NEW login. No Zombie Users!
