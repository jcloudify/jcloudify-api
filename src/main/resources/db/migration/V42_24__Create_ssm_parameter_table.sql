create table if not exists "ssm_parameter"
(
    id             varchar
        constraint pk_ssm_param primary key default uuid_generate_v4(),
    name           varchar not null,
    id_environment varchar,
    constraint fk_environment foreign key (id_environment) references "environment" (id)
);