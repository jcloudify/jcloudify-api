create table if not exists "environment_variable" (
    id varchar
        constraint pk_env_var primary key default uuid_generate_v4(),
    name varchar not null,
    value varchar not null,
    archived boolean default false,
    id_environment varchar,
    constraint fk_environment foreign key (id_environment) references environment (id)
);