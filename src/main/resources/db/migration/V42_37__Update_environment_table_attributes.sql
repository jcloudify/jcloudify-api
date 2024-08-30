alter table "environment" drop constraint if exists unique_env_by_app;

create unique index unique_active_env_type_per_app on "environment" (environment_type, id_application) where archived = false;