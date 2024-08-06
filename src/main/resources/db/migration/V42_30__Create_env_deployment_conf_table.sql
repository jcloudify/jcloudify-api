create table env_deployment_conf
(
    id                                varchar primary key                              default uuid_generate_v4(),
    compute_permission_stack_file_key varchar,
    event_stack_file_key              varchar,
    storage_bucket_stack_file_key     varchar,
    build_template_file               varchar,
    env_id                            varchar references "environment" ("id") not null,
    creation_datetime                 timestamp without time zone             not null default now(),
    unique (creation_datetime, env_id)
);
