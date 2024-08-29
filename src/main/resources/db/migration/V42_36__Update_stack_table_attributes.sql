alter table "stack" add column archived boolean default false;

alter table "stack" drop constraint if exists stack_type_unique_per_env;

create unique index unique_existing_stack_type_per_env on "stack" (type, id_environment) where archived = false;