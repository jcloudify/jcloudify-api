create table app_environment_deployment
(
    id                    varchar primary key                  default uuid_generate_v4(),
    app_id                varchar                     not null,
    env_id                varchar                     not null,
    env_depl_conf_id      varchar                     not null,
    deployed_url          varchar,
    creation_datetime     timestamp without time zone not null default now(),
    gh_commit_branch      varchar                     not null,
    gh_commit_author_name varchar                     not null,
    gh_commit_message     varchar                     not null,
    gh_commit_sha         varchar                     not null,
    gh_org                varchar,
    gh_is_pushed          boolean,
    gh_repo_id            varchar,
    gh_is_repo_private    varchar,
    gh_repo_url           varchar,
    gh_repo_name          varchar,
    gh_repo_owner_type    varchar,
    creator_email         varchar,
    creator_username      varchar,
    creator_gh_id         varchar,
    creator_avatar_url    varchar,
    constraint fk_env_depl_app foreign key (app_id) references "app" (id),
    constraint fk_env_depl_env foreign key (env_id) references "env" (id),
    constraint fk_env_depl_app foreign key (app_id) references "env_deployment_conf" (id)
);
