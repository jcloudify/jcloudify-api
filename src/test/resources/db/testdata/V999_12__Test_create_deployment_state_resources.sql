insert into "deployment_state"(id, progression_status, timestamp, execution_type, app_env_deployment_id)
values ('poja_application_deployment_state_1_id', 'TEMPLATE_FILE_CHECK_IN_PROGRESS',
        '2024-09-01 08:50:00', 'ASYNCHRONOUS', 'poja_deployment_1_id'),
       ('poja_application_deployment_state_2_id', 'INDEPENDENT_STACKS_DEPLOYMENT_INITIATED',
        '2024-09-01 08:51:00', 'ASYNCHRONOUS', 'poja_deployment_1_id'),
       ('poja_application_deployment_state_3_id', 'INDEPENDENT_STACKS_DEPLOYMENT_IN_PROGRESS',
        '2024-09-01 08:51:15', 'ASYNCHRONOUS', 'poja_deployment_1_id'),
    ('poja_application_deployment_state_4_id', 'INDEPENDENT_STACKS_DEPLOYED',
        '2024-09-01 08:51:35', 'ASYNCHRONOUS', 'poja_deployment_1_id'),
    ('poja_application_deployment_state_5_id', 'COMPUTE_STACK_DEPLOYMENT_IN_PROGRESS',
        '2024-09-01 08:52:00', 'ASYNCHRONOUS', 'poja_deployment_1_id');