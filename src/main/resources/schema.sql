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

INSERT INTO users (username, password)
    VALUE (
    'root',
    '$pbkdf2$urwqRuO67ILiMDqD7x/bzhskW7ahv2DKbQ8hQZoabng$879BU44ubTVm3PWnZTd9KDbw8UcI+eKyhW4JMrSI917uoxH/1meuZjNqj1UlztIj6axQJY1ojNf60uAYnYOxpA'
);

INSERT INTO users_roles(username, role) VALUE ('root', 'root');

CREATE TABLE accounts
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    agency     VARCHAR(10)  NOT NULL,
    code       VARCHAR(10)  NOT NULL,
    dtVerifier VARCHAR(10)  NOT NULL,
    balance    DOUBLE       NOT NULL,
    username   VARCHAR(255) NOT NULL
);

ALTER TABLE accounts
    ADD CONSTRAINT uk_account_code UNIQUE KEY (code);

ALTER TABLE accounts
    ADD CONSTRAINT fk_username_account UNIQUE KEY (username);