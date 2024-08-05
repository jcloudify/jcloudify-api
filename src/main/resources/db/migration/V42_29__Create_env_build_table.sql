CREATE TABLE IF NOT EXISTS "environment_build"(
                                          id varchar
                                              constraint pk_env_build primary key default uuid_generate_v4(),
    bucket_key varchar not null,
    creation_datetime timestamp without time zone default current_timestamp,
                                          id_environment varchar,
    constraint environment_fk foreign key (id_environment) references "environment"(id)
);