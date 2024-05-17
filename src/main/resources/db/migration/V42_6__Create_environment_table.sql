do
$$
    begin
        if not exists(select from pg_type where typname = 'environment_type') then
            create type environment_type as enum ('PROD', 'PREPROD');
        end if;
    end
$$;

create table if not exists "environment" (
    id varchar
            constraint pk_environment primary key default uuid_generate_v4(),
    environment_type environment_type default 'PREPROD',
    archived boolean default false,
    id_application varchar,
    constraint fk_application foreign key (id_application) references "application" (id)
)