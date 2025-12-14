User Authentication Microservice – Class Structure & Layers

This microservice is built using a layered architecture with clear separation of responsibilities for Domain, Services, Repository, Cache, Mapper, and Serializer layers.

Layer: Domain

Responsible for business logic and rules for the User entity.

Class: User

Represents the rich domain model of a user.

Attributes:

email

password (hashed)

Methods:

Method	Description
create(email, p1, p2) → User	Validates email and that p1 == p2, hashes password, returns a new User instance.
verify_password(password) → bool	Checks if a given password matches the stored hash.
validate_email(email) → bool	Validates the email format.
validate_password(password) → bool	Validates password strength and rules.
Layer: Services

Orchestrates use cases, validates input, interacts with Domain and Repository.

Class: UserService

Dependencies: User (Domain), UserRepo

Methods:

Method	Description
register(email, p1, p2) → UserDTO	Orchestrates registration: validates inputs, creates User, saves via Repo, returns UserDTO.
login(email, password) → UserDTO	Fetches user info via Repo, verifies credentials, returns UserDTO.
Class: TokenService

Methods:

Method	Description
create_jwt(user_id) → (access_token, refresh_token)	Generates a pair of JWTs for authentication.
refresh_token(refresh_token) → new_access	Validates refresh token, blacklists old, issues a new access token.
logout(token) → None	Decodes token and blacklists it in Redis.
Layer: Repository

Acts as the intermediary between Domain objects and the database.

Class: UserRepo

Methods:

Method	Description
save(userDBEntity) → None	Persists a user to the database.
validate_credentials(email) → User	Fetches user information (password hash, id) to validate login.
Layer: Cache

Stores token information for scalability and security (Redis).

Class: RedisCache

Methods:

Method	Description
blacklist_token(jti, expiry) → None	Saves a token JTI in Redis to prevent reuse.
is_blacklisted(jti) → bool	Checks if a token is blacklisted.
Layer: Mapper

Maps between rich Domain models, database entities, and DTOs.

Class: UserMapper

Methods:

Method	Description
to_db(user) → UserDB	Converts a User (rich domain) to a UserDB (anemic DB entity).
to_domain(userDB) → User	Converts a UserDB entity back to the User domain model.
Layer: Serializer

Converts Domain objects to JSON and vice versa for client responses.

Class: UserSerializer

Methods:

Method	Description
to_json(user) → JSON	Maps a User domain object to a client-friendly JSON object (excludes sensitive info).
to_domain(json) → User	Maps JSON input from the client to a User domain object.
Request Schemas

Defines validated input bodies for the controllers.

Schema	Fields	Description
registerSchema	email, pass1, pass2	Used for sign-up requests.
loginSchema	email, password	Used for login requests.
tokenSchema	refresh_token	Used for token rotation and logout.