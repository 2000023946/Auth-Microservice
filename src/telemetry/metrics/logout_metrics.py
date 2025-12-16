from prometheus_client import Counter, Histogram

# ==========================
# LOGOUT METRICS
# ==========================
auth_logout_success = Counter(
    "auth_logout_success_total", "Total successful logout attempts"
)

auth_logout_failure = Counter(
    "auth_logout_failure_total", "Total failed logout attempts", ["reason"]
)

auth_logout_latency = Histogram(
    "auth_logout_latency_seconds",
    "Logout request latency in seconds",
    buckets=(0.1, 0.3, 0.5, 1, 2, 5),
)
