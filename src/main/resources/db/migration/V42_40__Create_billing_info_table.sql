do
$$
    begin
        if not exists(select from pg_type where typname = 'billing_info_compute_status') then
            create type billing_info_compute_status as enum ('PENDING','IN_PROGRESS','FINISHED');
        end if;
    end
$$;

create table if not exists "billing_info"
(
    id                           varchar
        constraint pk_billing_info primary key               default uuid_generate_v4(),
    creation_datetime            timestamp without time zone,
    compute_datetime             timestamp without time zone,
    user_id                      varchar        not null,
    app_id                       varchar        not null,
    env_id                       varchar        not null,
    query_id                     varchar        not null,
    pricing_method               pricing_method not null     default 'TEN_MICRO'::pricing_method,
    computed_price_in_usd        numeric,
    computed_duration_in_minutes numeric,
    computed_memory_used_in_mo   numeric,
    status                       billing_info_compute_status default 'PENDING',
    constraint fk_user_billing foreign key (user_id) references "user" (id),
    constraint fk_app_billing foreign key (app_id) references "application" (id),
    constraint fk_env_billing foreign key (env_id) references "environment" (id)
);
