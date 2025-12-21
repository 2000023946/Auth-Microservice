class ITokenAdapter:
    def encode(self, payload: dict) -> str:
        """Generate a token from a payload dictionary."""
        raise NotImplementedError()

    def decode(self, token: str) -> dict:
        """Decode a token back into a payload dictionary."""
        raise NotImplementedError()
