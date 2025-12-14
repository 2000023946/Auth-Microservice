from src.controller.inbound.register_controller import RegisterController
from src.controller.inbound.login_controller import LoginController
from src.controller.inbound.refresh_controller import RefreshTokenController
from src.controller.inbound.logout_controller import LogoutController
from src.controller.inbound.silent_auth_controller import SilentAuthController


class ControllerComponent:
    def __init__(self, services):
        self.register = RegisterController(services.user_service)
        self.login = LoginController(
            services.user_service,
            services.token_service,
        )
        self.refresh = RefreshTokenController(services.token_service)
        self.logout = LogoutController(services.token_service)
        self.silent_auth = SilentAuthController(
            services.token_service,
            services.user_service,
        )
