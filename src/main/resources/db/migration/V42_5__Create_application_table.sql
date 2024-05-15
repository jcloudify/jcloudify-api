create table if not exists "application" (
    id varchar
        constraint pk_application primary key default uuid_generate_v4(),
    name varchar not null,
    archived boolean default false,
    github_repository varchar,
    creation_datetime timestamp without time zone default current_timestamp,
    id_user varchar,
    constraint fk_user foreign key (id_user) references "user" (id)
)