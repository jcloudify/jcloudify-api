insert into "env_deployment_conf"(id, compute_permission_stack_file_key, event_stack_file_key,
                                  storage_bucket_stack_file_key, storage_database_sqlite_stack_file_key,
                                  build_template_file, env_id, creation_datetime, poja_conf_file_key)
values ('env_1_depl_files_1_id', 'compute-permission.yml', 'event-stack.yml', 'storage_bucket_stack.yml',
        'storage-database-sqlite-stack.yml',
        'template.yml', 'poja_application_environment_id',
        '2023-06-18T10:15:30.00Z', 'poja_1.yml'),
       ('env_1_depl_files_2_id', 'compute-permission.yml', 'event-stack.yml', 'storage_bucket_stack.yml',
        'storage-database-sqlite-stack.yml',
        'template.yml', 'poja_application_environment_id', '2023-06-17T10:15:30.00Z', 'poja_1.yml'),
       ('other_env_1_depl_files_1_id', 'compute-permission.yml', 'event-stack.yml', 'storage_bucket_stack.yml',
        'storage-database-sqlite-stack.yml',
        'template.yml', 'other_poja_application_environment_id', '2023-06-17T10:15:30.00Z', 'poja_1.yml');
