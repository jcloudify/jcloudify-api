CREATE TABLE IF NOT EXISTS "compute_resources" (
                                                   id varchar
                                                       constraint pk_env_build primary key default uuid_generate_v4(),
    frontal_function_name varchar,
    worker_1_function_name varchar,
    worker_2_function_name varchar,
    environment_id varchar,
    constraint compute_resources_env_fk foreign key (environment_id) references environment (id)
);