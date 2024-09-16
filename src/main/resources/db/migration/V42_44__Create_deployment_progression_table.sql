do
$$
begin
        if
not exists(select from pg_type where typname = 'deployment_progression_state') then
create type deployment_progression_state as enum (
                'TEMPLATE_FILE_CHECK_INITIATED', 'TEMPLATE_FILE_CHECK_FAILED',
                'INDEPENDENT_STACK_DEPLOYMENT_INITIATED', 'INDEPENDENT_STACK_DEPLOYMENT_IN_PROGRESS',
                'INDEPENDENT_STACK_DEPLOYMENT_FAILED', 'COMPUTE_STACK_DEPLOYMENT_INITIATED',
                'COMPUTE_STACK_DEPLOYMENT_FAILED','COMPUTE_STACK_DEPLOYED');
end if;
end
$$;

do
$$
begin
        if
not exists(select from pg_type where typname = 'execution_type') then
create type execution_type as enum ('SYNCHRONOUS', 'ASYNCHRONOUS');
end if;
end
$$;

create table if not exists "deployment_progression"(
    id varchar constraint pk_deployment_progression primary key default uuid_generate_v4(),
    "state" deployment_progression_state not null default 'TEMPLATE_FILE_CHECK_INITIATED',
    "timestamp" timestamp without time zone default now(),
    execution_type execution_type,
    app_environment_deployment_id varchar,
    constraint fk_app_environment_deployment foreign key (app_environment_deployment_id) references "app_environment_deployment"(id)
);