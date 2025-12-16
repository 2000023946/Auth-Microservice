from prometheus_client import Counter, Histogram

# ==========================
# REFRESH TOKEN METRICS
# ==========================
auth_refresh_success = Counter(
    "auth_refresh_success_total", "Total successful token refreshes"
)

auth_refresh_failure = Counter(
    "auth_refresh_failure_total", "Total failed token refreshes", ["reason"]
)

auth_refresh_latency = Histogram(
    "auth_refresh_latency_seconds",
    "Token refresh request latency in seconds",
    buckets=(0.1, 0.3, 0.5, 1, 2, 5),
)
