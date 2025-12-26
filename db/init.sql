DROP DATABASE IF EXISTS auth_db;
CREATE DATABASE auth_db;
USE auth_db;

DROP TABLE IF EXISTS users;
CREATE TABLE users (
    -- UUIDs are typically 36 characters (including dashes)
    id CHAR(36) PRIMARY KEY, 
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------
-- 1. Create User (Accepts UUID from Python)
-- ----------------------------------------------------------------
DROP PROCEDURE IF EXISTS create_user;
DELIMITER //

CREATE PROCEDURE create_user(
    IN ip_id CHAR(36),
    IN ip_email VARCHAR(255),
    IN ip_password_hash VARCHAR(255)
)
sp_main: BEGIN
    -- Null checks
    IF ip_id IS NULL OR ip_email IS NULL OR ip_password_hash IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'id, email, or password_hash is null';
    END IF;

    -- ID already exists 
    IF EXISTS (SELECT 1 FROM users WHERE id = ip_id) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'id already exists';
    END IF;

    -- Email already exists check
    IF EXISTS (SELECT 1 FROM users WHERE email = ip_email) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'email already registered';
    END IF;

    -- Insert user with the provided UUID
    INSERT INTO users (id, email, password_hash)
    VALUES (ip_id, LOWER(ip_email), ip_password_hash);

END //
DELIMITER ;

-- ----------------------------------------------------------------
-- 2. Login User
-- ----------------------------------------------------------------
DROP PROCEDURE IF EXISTS login_user;
DELIMITER //

CREATE PROCEDURE login_user(
    IN ip_email VARCHAR(255)
)
sp_main: BEGIN
    SELECT *
    FROM users
    WHERE email = LOWER(ip_email)
    LIMIT 1;
END //
DELIMITER ;

-- ----------------------------------------------------------------
-- 3. Get User By ID
-- ----------------------------------------------------------------
DROP PROCEDURE IF EXISTS get_user_by_id;
DELIMITER //

CREATE PROCEDURE get_user_by_id(
    IN ip_user_id CHAR(36)
)
sp_main: BEGIN
    SELECT *
    FROM users
    WHERE id = ip_user_id
    LIMIT 1;
END //
DELIMITER ;