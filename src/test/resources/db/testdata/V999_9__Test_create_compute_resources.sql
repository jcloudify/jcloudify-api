insert into "compute_resources"
    (id, frontal_function_name, worker_1_function_name, worker_2_function_name, environment_id, creation_datetime)
values ('poja_application_compute_1_resources_id', 'prod-compute-frontal-function',
        'prod-compute-worker-1-function', 'prod-compute-worker-2-function','poja_application_environment_id', '2024-07-18T10:15:30.00Z'),
    ('poja_application_compute_2_resources_id', 'preprod-compute-frontal-function',
     'preprod-compute-worker-1-function', 'preprod-compute-worker-2-function','poja_application_environment_id', '2023-07-18T10:15:30.00Z');