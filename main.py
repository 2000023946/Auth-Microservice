from flask import Flask, request, make_response, jsonify  # type: ignore
from src.application import container  # <--- Import the wired container
from prometheus_client import Counter, Histogram, generate_latest, CONTENT_TYPE_LATEST

from src.telemetry.metrics.metrics_decorator import track_metrics
from src.telemetry.metrics.login_metrics import (
    auth_login_success,
    auth_login_failure,
    auth_login_latency,
)
from src.telemetry.metrics.register_metrics import (
    auth_register_success,
    auth_register_failure,
    auth_register_latency,
)

from src.telemetry.metrics.refresh_metrics import (
    auth_refresh_success,
    auth_refresh_failure,
    auth_refresh_latency,
)

from src.telemetry.metrics.logout_metrics import (
    auth_logout_success,
    auth_logout_failure,
    auth_logout_latency,
)

from src.telemetry.metrics.silent_metrics import (
    auth_me_success,
    auth_me_failure,
    auth_me_latency,
)


app = Flask(__name__)


# ==============================================================================
# PROMETHEUS METRICS ENDPOINT
# ==============================================================================


@app.route("/metrics")
def metrics():
    """Expose Prometheus metrics"""
    return make_response(generate_latest(), 200, {"Content-Type": CONTENT_TYPE_LATEST})


# ==============================================================================
# 1. ADAPTER (The Bridge)
# ==============================================================================


class FrameworkRequest:
    def __init__(self, flask_req):
        self.json = flask_req.get_json(silent=True) or {}
        self.cookies = flask_req.cookies


def flask_adapter(controller, flask_req):
    """
    Runs the Clean Architecture Controller and converts the result back to Flask.
    """
    # 1. Convert IN
    internal_req = FrameworkRequest(flask_req)

    # 2. Run Logic
    internal_res = controller.handle(internal_req)

    # 3. Convert OUT
    flask_res = make_response(jsonify(internal_res.body))
    flask_res.status_code = internal_res.status_code

    # Map Headers (Set-Cookie)
    # NEW (Works with list of tuples)

    # 4. Map Headers (Set-Cookie)
    # Checks if headers is a dict (legacy) or list of tuples (new support for multiple cookies)
    if isinstance(internal_res.headers, dict):
        for k, v in internal_res.headers.items():
            flask_res.headers[k] = v
    else:
        # This handles the list of tuples: [('Set-Cookie', '...'), ('Set-Cookie', '...')]
        for k, v in internal_res.headers:
            # .add() allows duplicate keys, which is required for multiple Set-Cookie headers
            flask_res.headers.add(k, v)

    return flask_res


# ==============================================================================
# 2. ROUTES
# ==============================================================================


@app.route("/health", methods=["GET"])
def health_check():
    # You can later add logic here to check if the DB connection is live
    return {"status": "healthy"}, 200


@app.route("/api/auth/login", methods=["POST"])
@track_metrics(auth_login_success, auth_login_failure, auth_login_latency)
def login():
    print("received the login request")
    return flask_adapter(container.controllers.login, request)


@app.route("/api/auth/register", methods=["POST"])
@track_metrics(auth_register_success, auth_register_failure, auth_register_latency)
def register():
    return flask_adapter(container.controllers.register, request)


@app.route("/api/auth/refresh", methods=["POST"])
@track_metrics(auth_refresh_success, auth_refresh_failure, auth_refresh_latency)
def refresh():
    return flask_adapter(container.controllers.refresh, request)


@app.route("/api/auth/logout", methods=["POST"])
@track_metrics(auth_logout_success, auth_logout_failure, auth_logout_latency)
def logout():
    return flask_adapter(container.controllers.logout, request)


@app.route("/api/auth/me", methods=["GET"])
@track_metrics(auth_me_success, auth_me_failure, auth_me_latency)
def me():
    return flask_adapter(container.controllers.silent_auth, request)


# ==============================================================================
# 3. ENTRY POINT
# ==============================================================================

if __name__ == "__main__":
    print("🚀 Auth Service is running on http://localhost:5000")
    app.run(host="0.0.0.0", debug=True, port=5000)
