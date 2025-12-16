from prometheus_client import Counter, Histogram


# ==========================
# SILENT AUTH / ME METRICS
# ==========================
auth_me_success = Counter(
    "auth_me_success_total", "Total successful silent auth requests"
)

auth_me_failure = Counter(
    "auth_me_failure_total", "Total failed silent auth requests", ["reason"]
)

auth_me_latency = Histogram(
    "auth_me_latency_seconds",
    "Silent auth / me request latency in seconds",
    buckets=(0.1, 0.3, 0.5, 1, 2, 5),
)
