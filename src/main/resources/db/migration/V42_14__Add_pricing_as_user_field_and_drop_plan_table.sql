drop table plan;

do
$$
    begin
        if not exists(select from pg_type where typname = 'pricing_method') then
            create type pricing_method as enum ('TEN_MICRO');
        end if;
    end
$$;

alter table "user" add column pricing_method pricing_method default 'TEN_MICRO'::pricing_method;
