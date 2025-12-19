DROP DATABASE IF EXISTS auth_db;
CREATE DATABASE auth_db;
USE auth_db;

drop table if exists users;
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


DROP PROCEDURE IF EXISTS create_user;
DELIMITER //

CREATE PROCEDURE create_user(
    IN ip_email VARCHAR(255),
    IN ip_password_hash VARCHAR(255)
)
sp_main: BEGIN

    -- Null check
    IF ip_email IS NULL OR ip_password_hash IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'email or password_hash is null';
    END IF;

    -- Basic hash format validation (bcrypt or argon2)
    IF NOT (
        ip_password_hash LIKE '$2a$%' OR
        ip_password_hash LIKE '$2b$%' OR
        ip_password_hash LIKE '$2y$%' OR
        ip_password_hash LIKE '$argon2%'
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'password is not a valid hash';
    END IF;

    -- Email already exists check
    IF EXISTS (
        SELECT 1 FROM users WHERE email = ip_email
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'email already registered';
    END IF;

    -- Insert user (id auto-incremented)
    INSERT INTO users (email, password_hash)
    VALUES (LOWER(ip_email), ip_password_hash);

    SELECT LAST_INSERT_ID() as id;

END //

DELIMITER ;


DROP PROCEDURE IF EXISTS login_user;
DELIMITER //

CREATE PROCEDURE login_user(
    IN ip_email VARCHAR(255)
)
sp_main: BEGIN

    -- Null check
    IF ip_email IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'email is null';
    END IF;

    -- Select the user ID and password hash
    SELECT id AS user_id, email, password_hash
    FROM users
    WHERE email = LOWER(ip_email)
    LIMIT 1;

    -- If no row returned, the service will handle "user not found"

END //

DELIMITER ;


DROP PROCEDURE IF EXISTS get_user_by_id;
DELIMITER //

CREATE PROCEDURE get_user_by_id(
    IN ip_user_id INT
)
sp_main: BEGIN

    -- Null check
    IF ip_user_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'user_id is null';
    END IF;

    -- Fetch user by ID
    SELECT id AS user_id, email, created_at
    FROM users
    WHERE id = ip_user_id
    LIMIT 1;

    -- If no row returned, the caller (app) can handle "user not found"
    
END //

DELIMITER ;




