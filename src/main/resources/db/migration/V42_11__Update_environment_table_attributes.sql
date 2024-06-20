do
$$
    begin
        if not exists(select from pg_type where typname = 'state_enum') then
            create type state_enum as enum ('HEALTHY', 'UNHEALTHY');
        end if;
    end
$$;

alter table "environment" add column state state_enum not null default 'HEALTHY';