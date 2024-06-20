do
$$
    begin
        if not exists(select from pg_type where typname = 'stack_type') then
            create type stack_type as enum ('EVENT', 'COMPUTE_PERMISSION', 'STORAGE_BUCKET', 'STORAGE_DATABASE');
        end if;
    end
$$;

create table if not exists "stack" (
    id varchar constraint stack_pk primary key default uuid_generate_v4(),
    name varchar not null,
    cf_stack_id varchar not null,
    type stack_type not null ,
    id_environment varchar,
    id_application varchar,
    constraint user_fk foreign key (id_environment) references "environment"(id),
    constraint application_fk foreign key (id_application) references "application"(id)
);