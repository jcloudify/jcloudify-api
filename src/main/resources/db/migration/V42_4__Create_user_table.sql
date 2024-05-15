do
$$
    begin
        if not exists(select from pg_type where typname = 'user_role') then
            create type user_role as enum ('USER', 'ADMIN');
        end if;
    end
$$;

create table if not exists "user" (
    id varchar
        constraint pk_user primary key default uuid_generate_v4(),
    first_name varchar not null,
    last_name varchar not null,
    username varchar not null,
    email varchar not null,
    role user_role default 'USER',
    github_id varchar,
    id_plan varchar,
    constraint fk_plan foreign key (id_plan) references "plan" (id)
);