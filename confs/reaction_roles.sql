DROP TABLE IF EXISTS reaction_roles;

CREATE TABLE reaction_roles (
    id                      BIGINT              PRIMARY KEY,
    name                    VARCHAR             NOT NULL   ,
    message_id              BIGINT              NOT NULL   ,
    role_id                 BIGINT              NOT NULL   ,
    emoji_id                VARCHAR             NOT NULL
);

DROP SEQUENCE IF EXISTS reaction_roles;
CREATE SEQUENCE reaction_role_ids START WITH 0 INCREMENT BY 1 MINVALUE 0;