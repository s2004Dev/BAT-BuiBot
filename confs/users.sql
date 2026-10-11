DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id                      BIGINT              PRIMARY KEY,
    source                  VARCHAR(63)         NOT NULL   ,
    bui                     BIGINT                         ,
    buizel                  BIGINT                         ,
    xps                     INT                            ,
    joined                  DATE                NOT NULL   ,
    birthday                VARCHAR(7)                     ,
    timezone                VARCHAR(255)                   ,
    here                    BOOLEAN             NOT NULL
);

CREATE SEQUENCE user_ids START WITH 1 INCREMENT BY 1;