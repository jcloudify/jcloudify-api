insert into "billing_info" (id, creation_datetime, compute_datetime, user_id, app_id, env_id, query_id, pricing_method,
                            computed_price_in_usd, computed_duration_in_minutes, computed_memory_used_in_mo, status)
values ('joe_doe_billing_info_1', '2024-09-05T00:00:00.00Z', '2024-09-05T12:00:00.00Z', 'joe_doe_id',
        'other_poja_application_id', 'other_poja_application_environment_id', 'dummy', 'dummy', 100, 60, 512, 'FINISHED'),
       ('joe_doe_billing_info_2', '2024-06-05T00:00:00.00Z', '2024-06-05T12:00:00.00Z', 'joe_doe_id',
        'other_poja_application_id', 'other_poja_application_environment_2_id', 'dummy', 'dummy', 250, 120, 1024, 'FINISHED');