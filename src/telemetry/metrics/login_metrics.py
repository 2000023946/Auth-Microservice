from prometheus_client import Counter, Histogram

# Metrics definitions
auth_login_success = Counter(
    "auth_login_success_total", "Total successful login attempts"
)

auth_login_failure = Counter(
    "auth_login_failure_total", "Total failed login attempts", ["reason"]
)

auth_login_latency = Histogram(
    "auth_login_latency_seconds",
    "Login request latency in seconds",
    buckets=(0.1, 0.3, 0.5, 1, 2, 5),
)
