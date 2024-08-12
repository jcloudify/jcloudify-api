create table env_build_request
(
    id                 varchar primary key               default uuid_generate_v4(),
    app_id             varchar,
    user_id            varchar,
    constraint fk_user_build_request foreign key (user_id) references "user" (id),
    constraint fk_app_build_request foreign key (app_id) references application (id),
    creation_timestamp timestamp with time zone not null default now()
);
