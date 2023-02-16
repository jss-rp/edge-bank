CREATE TABLE users
(
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE users_roles
(
    username VARCHAR(255) NOT NULL,
    role     VARCHAR(255) NOT NULL
);

CREATE TABLE roles_perms
(
    role VARCHAR(255) NOT NULL,
    perm VARCHAR(255) NOT NULL
);

ALTER TABLE users
    ADD CONSTRAINT pk_username PRIMARY KEY (username);
ALTER TABLE users_roles
    ADD CONSTRAINT pk_users_roles PRIMARY KEY (username, role);
ALTER TABLE roles_perms
    ADD CONSTRAINT pk_roles_perms PRIMARY KEY (role, perm);

ALTER TABLE users_roles
    ADD CONSTRAINT fk_username FOREIGN KEY (username) REFERENCES users (username);

INSERT INTO roles_perms (role, perm) VALUE ('root', 'all');
INSERT INTO roles_perms (role, perm) VALUE ('root', 'view,manage');

INSERT INTO users (username, password)
    VALUE (
           'root',
           '$pbkdf2$urwqRuO67ILiMDqD7x/bzhskW7ahv2DKbQ8hQZoabng$879BU44ubTVm3PWnZTd9KDbw8UcI+eKyhW4JMrSI917uoxH/1meuZjNqj1UlztIj6axQJY1ojNf60uAYnYOxpA'
    );

INSERT INTO users_roles(username, role) VALUE ('root', 'user');
INSERT INTO users_roles(username, role) VALUE ('root', 'root');

CREATE TABLE people
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(45) NOT NULL,
    surname    VARCHAR(45) NOT NULL,
    birth_date DATE        NOT NULL,
    document   VARCHAR(15) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE people
    ADD CONSTRAINT uk_person_document UNIQUE KEY (document);

CREATE TABLE accounts
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    agency     VARCHAR(10)  NOT NULL,
    code       VARCHAR(10)  NOT NULL,
    dtVerifier VARCHAR(10)  NOT NULL,
    balance    DOUBLE       NOT NULL,
    username   VARCHAR(255) NOT NULL,
    person_id  INT          NOT NULL
);

ALTER TABLE accounts
    ADD CONSTRAINT uk_account_code UNIQUE KEY (code);

ALTER TABLE accounts
    ADD CONSTRAINT fk_username_account FOREIGN KEY (username) REFERENCES users (username);

ALTER TABLE accounts
    ADD CONSTRAINT fk_person_id_account FOREIGN KEY (person_id) REFERENCES people (id);

CREATE TABLE transactions
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    uuid         VARCHAR(50)    NOT NULL,
    value        DECIMAL(15, 5) NOT NULL,
    type         VARCHAR(10)    NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    finished_at  TIMESTAMP,
    account_code INT            NOT NULL
);

ALTER TABLE transactions
    ADD CONSTRAINT uk_transaction_uuid UNIQUE KEY (uuid);

ALTER TABLE transactions
    ADD CONSTRAINT fk_transaction_account FOREIGN KEY (account_code) REFERENCES accounts (id);

CREATE TRIGGER balance_sum
    AFTER INSERT
    ON transactions
    FOR EACH ROW
BEGIN
    UPDATE accounts a SET a.balance = a.balance + NEW.value WHERE a.id = NEW.account_code;
END;
