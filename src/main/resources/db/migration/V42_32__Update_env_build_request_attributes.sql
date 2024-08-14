alter table env_build_request
    add column built_zip_file_key varchar;
alter table env_build_request
    add column env_id varchar;
alter table env_build_request
    add constraint fk_buid_request_env foreign key (env_id) references environment ("id");
