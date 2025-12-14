from src.components.infrastructure_component import InfrastructureComponent
from src.components.repository_component import RepositoryComponent
from src.components.service_component import ServiceComponent
from src.components.controller_component import ControllerComponent


class AppContainer:
    """
    PURE composition root.
    No construction details.
    No business logic.
    """

    def __init__(self):
        self.infrastructure = InfrastructureComponent()
        self.repositories = RepositoryComponent(self.infrastructure)
        self.services = ServiceComponent(
            self.repositories,
            self.infrastructure,
        )
        self.controllers = ControllerComponent(self.services)


# singleton
container = AppContainer()
