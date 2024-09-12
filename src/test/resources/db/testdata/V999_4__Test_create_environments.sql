insert into "environment"(id, environment_type, archived, id_application, configuration_file_key)
values ('poja_application_environment_id', 'PROD', false, 'poja_application_id', 'file1_conf_key.yml'),
       ('other_poja_application_environment_2_id', 'PREPROD', false, 'other_poja_application_id', null),
       ('other_poja_application_environment_id', 'PROD', false, 'other_poja_application_id', 'file_conf_1_key.yml'),
       ('archived_other_poja_app_env_id', 'PROD', true, 'other_poja_application_id', 'file2_conf_key.yml');
