DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id                      BIGINT              PRIMARY KEY,
    bui                     BIGINT                         ,
    buizel                  BIGINT                         ,
    xps                     INT                            ,
    joined                  DATE                NOT NULL   ,
    birthday                DATE                           ,
    here                    BOOLEAN             NOT NULL
);