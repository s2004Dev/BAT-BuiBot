DROP TABLE IF EXISTS user_mappings;

CREATE TABLE user_mappings (
    id                      BIGINT              NOT NULL        ,
    id_source               VARCHAR(255)        NOT NULL        ,
    source                  VARCHAR(63)         NOT NULL        ,

    CONSTRAINT user_mapping_pk PRIMARY KEY(id, id_source, source)
);