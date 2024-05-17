create extension if not exists "uuid-ossp";
create table if not exists "plan" (
    id varchar
            constraint pk_plan primary key default uuid_generate_v4(),
    name varchar not null,
    cost double precision not null default 0.0
);