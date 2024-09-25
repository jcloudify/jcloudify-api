do
$$
    begin
        if not exists(select from pg_type where typname = 'execution_type') then
            create type execution_type as enum ('SYNCHRONOUS','ASYNCHRONOUS');
        end if;
        if not exists(select from pg_type where typname = 'deployment_progression_status') then
            create type deployment_progression_status as enum (
                'TEMPLATE_FILE_CHECK_IN_PROGRESS','TEMPLATE_FILE_CHECK_FAILED',
                'INDEPENDENT_STACKS_DEPLOYMENT_INITIATED', 'INDEPENDENT_STACKS_DEPLOYMENT_IN_PROGRESS',
                'INDEPENDENT_STACKS_DEPLOYED', 'INDEPENDENT_STACKS_DEPLOYMENT_FAILED',
                'COMPUTE_STACK_DEPLOYMENT_IN_PROGRESS', 'COMPUTE_STACK_DEPLOYMENT_FAILED', 'COMPUTE_STACK_DEPLOYED');
        end if;
    end
$$;

create table if not exists "deployment_state"(
    id varchar constraint pk_deployment_state primary key default uuid_generate_v4(),
    progression_status deployment_progression_status not null,
    timestamp timestamp without time zone,
    execution_type execution_type not null,
    app_env_deployment_id varchar,
    constraint fk_app_env_deployment_state foreign key (app_env_deployment_id) references "app_environment_deployment"(id)
);