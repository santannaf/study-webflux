CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- CREATE OR REPLACE FUNCTION ARRAY_TO_STRING_IMMUTABLE(
--     _nome varchar,
--     _apelido varchar,
--     _stack text[]
-- ) RETURNS TEXT AS
-- $$
-- BEGIN
--     RETURN _nome || _apelido || _stack;
-- END;
-- $$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION ARRAY_TO_STRING_IMMUTABLE(
    arr TEXT[],
    sep TEXT
) RETURNS TEXT
    IMMUTABLE PARALLEL SAFE
    LANGUAGE SQL AS
$$
SELECT ARRAY_TO_STRING(arr, sep)
$$;

-- CREATE TABLE IF NOT EXISTS pessoas
-- (
--     id         varchar(43) unique not null,
--     apelido    text unique not null,
--     nome       text        not null,
--     nascimento date        not null,
--     stack      character varying,
--     busca text GENERATED ALWAYS AS ((((((nome)::text || ' '::text) || (apelido)::text) || ' '::text) || (COALESCE(stack, ' '::character varying))::text)) STORED
-- );

CREATE TABLE IF NOT EXISTS pessoas
(
    id         uuid not null,
    apelido    text
        constraint id_pk primary key,
    nome       text not null,
    nascimento date not null,
    stack     varchar(32)[],
    busca      text
--     busca      TEXT GENERATED ALWAYS AS (nome || ' ' || apelido || ' ' ||
--                                         COALESCE(ARRAY_TO_STRING_IMMUTABLE(stack, ' '), '')) STORED
);

create unlogged table if not exists cache
(
    key  text
        constraint cache_id_pk primary key,
    data jsonb
);

-- CREATE TABLE IF NOT EXISTS pessoas
-- (
--     id         uuid unique not null,
--     apelido    text unique not null,
--     nome       text        not null,
--     nascimento date        not null,
--     stack      varchar(32)[],
--     busca      TEXT GENERATED ALWAYS AS (ARRAY_TO_STRING_IMMUTABLE(nome, apelido, stack)) STORED
-- );

create unique index if not exists idx_id_uuid on pessoas (id);
-- create unique index if not exists index_pessoas_on_apelido on pessoas using btree (apelido);
create index if not exists index_pessoas_on_search on pessoas using gist (busca gist_trgm_ops);

CREATE USER rinhaubr WITH PASSWORD 'rinhaubr';
GRANT CONNECT ON DATABASE rinha TO rinhaubr;

GRANT USAGE, CREATE ON SCHEMA public TO rinhaubr;
GRANT TRUNCATE ON ALL TABLES IN SCHEMA public TO rinhaubr;
GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE ON ALL TABLES IN SCHEMA public TO rinhaubr;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO rinhaubr;
ALTER USER rinhaubr WITH SUPERUSER;