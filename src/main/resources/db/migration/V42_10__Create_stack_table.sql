create table if not exists "stack" (
    id varchar constraint stack_pk primary key default uuid_generate_v4(),
    stack_name varchar not null,
    cf_stack_id varchar not null,
    id_environment varchar,
    id_application varchar,
    constraint user_fk foreign key (id_environment) references "environment"(id),
    constraint application_fk foreign key (id_application) references "application"(id)
);