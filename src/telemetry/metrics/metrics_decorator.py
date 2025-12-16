import time
from functools import wraps


def track_metrics(success_counter, failure_counter, latency_histogram):
    """
    Decorator to measure latency and increment Prometheus metrics.
    """

    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            start = time.time()
            response = func(*args, **kwargs)

            # If response is a Flask Response, call .get_json()
            if hasattr(response, "get_json"):
                res_json = response.get_json(silent=True) or {}
            # If response is a dict directly, just use it
            elif isinstance(response, dict):
                res_json = response
            else:
                res_json = {}

            # Increment success or failure
            if "error" not in res_json:
                success_counter.inc()
            else:
                failure_counter.labels(reason=res_json["error"]).inc()

            # Record latency
            latency_histogram.observe(time.time() - start)

            return response

        return wrapper

    return decorator
