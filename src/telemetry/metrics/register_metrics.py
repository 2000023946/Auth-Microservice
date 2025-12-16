from prometheus_client import Counter, Histogram

# ==========================
# REGISTER METRICS
# ==========================
auth_register_success = Counter(
    "auth_register_success_total", "Total successful registration attempts"
)

auth_register_failure = Counter(
    "auth_register_failure_total", "Total failed registration attempts", ["reason"]
)

auth_register_latency = Histogram(
    "auth_register_latency_seconds",
    "Registration request latency in seconds",
    buckets=(0.1, 0.3, 0.5, 1, 2, 5),
)
