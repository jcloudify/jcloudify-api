alter table if exists "environment"
    add column creation_datetime timestamp without time zone default now();
