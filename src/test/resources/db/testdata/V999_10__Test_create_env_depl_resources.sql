INSERT INTO app_environment_deployment (id, app_id, env_id, env_depl_conf_id, deployed_url, creation_datetime,
                                        gh_commit_message, gh_commit_sha, gh_repo_name, gh_repo_owner_name,
                                        gh_committer_email, gh_committer_name, gh_committer_id, gh_committer_avatar_url,
                                        gh_commit_url, gh_committer_login, gh_committer_type)
VALUES
-- First entry on 01 August 2024
('deployment_1_id', 'other_poja_application_id', 'other_poja_application_environment_id', 'env_1_depl_files_1_id',
 'https://example.com/deploy1', '2024-08-01 10:15:00',
 'Initial deployment', 'abc123def456', 'repo1', 'poja-org',
 'john.doe@example.com', 'John Doe', '12345678', 'https://avatars.githubusercontent.com/u/12345678',
 'https://github.com/poja-org/repo1/commit/abc123def456', 'johndoe', 'organization'),

-- Second entry on 02 August 2024
('deployment_2_id', 'other_poja_application_id', 'other_poja_application_environment_2_id', 'env_1_depl_files_2_id',
 'https://example.com/deploy2', '2024-08-02 14:30:00',
 'Bug fixes', '789ghi012jkl', 'repo2', 'poja-org',
 'jane.smith@example.com', 'Jane Smith', '87654321', 'https://avatars.githubusercontent.com/u/87654321',
 'https://github.com/poja-org/repo2/commit/789ghi012jkl', 'janesmith', 'user'),

-- Third entry on 02 August 2024 with archived environment
('deployment_3_id', 'other_poja_application_id', 'archived_other_poja_app_env_id', 'env_1_depl_files_1_id',
 'https://example.com/deploy3', '2024-08-02 09:00:00',
 'Final deployment', 'mno345pqr678', 'repo3', 'poja-org',
 'sam.brown@example.com', 'Sam Brown', '98765432', 'https://avatars.githubusercontent.com/u/98765432',
 'https://github.com/poja-org/repo3/commit/mno345pqr678', 'sambrown', 'organization'),

-- First entry on 01 September 2024 for poja_application_id
    ('poja_deployment_1_id', 'poja_application_id', 'poja_application_environment_id', 'env_1_depl_files_2_id',
 null, '2024-09-01 09:00:00',
 'Final deployment', 'mno345pqr6780', 'repo4', 'poja-org',
 'sam.brown@example.com', 'Sam Brown', '98765432', 'https://avatars.githubusercontent.com/u/98765432',
 'https://github.com/poja-org/repo3/commit/mno345pqr678', 'sambrown', 'organization');
