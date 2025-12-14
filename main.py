from flask import Flask, request, make_response, jsonify  # type: ignore
from src.application import container  # <--- Import the wired container

app = Flask(__name__)

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
    for key, value in internal_res.headers.items():
        flask_res.headers[key] = value

    return flask_res


# ==============================================================================
# 2. ROUTES
# ==============================================================================


@app.route("/api/auth/register", methods=["POST"])
def register():
    return flask_adapter(container.register_controller, request)


@app.route("/api/auth/login", methods=["POST"])
def login():
    return flask_adapter(container.login_controller, request)


@app.route("/api/auth/refresh", methods=["POST"])
def refresh():
    return flask_adapter(container.refresh_controller, request)


@app.route("/api/auth/logout", methods=["POST"])
def logout():
    return flask_adapter(container.logout_controller, request)


@app.route("/api/auth/me", methods=["GET"])
def me():
    return flask_adapter(container.silent_auth_controller, request)


# ==============================================================================
# 3. ENTRY POINT
# ==============================================================================

if __name__ == "__main__":
    print("🚀 Auth Service is running on http://localhost:5000")
    app.run(debug=True, port=5000)
