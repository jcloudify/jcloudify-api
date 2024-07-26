create table if not exists app_installation
(
    id                 varchar
        constraint app_installation_pk primary key default uuid_generate_v4(),
    gh_id              bigint,
    user_id            varchar references "user" ("id"),
    owner_github_login varchar,
    is_org             boolean
);
