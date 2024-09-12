INSERT INTO app_environment_deployment (id, app_id, env_id, env_depl_conf_id, deployed_url, creation_datetime,
                                        gh_commit_branch, gh_commit_author_name, gh_commit_message, gh_commit_sha,
                                        gh_org, gh_is_pushed, gh_repo_id, gh_is_repo_private, gh_repo_url,
                                        gh_repo_name, gh_repo_owner_type, creator_email, creator_username,
                                        creator_gh_id,
                                        creator_avatar_url)
VALUES
-- First entry on 01 August 2024
('deployment_1_id', 'other_poja_application_id', 'other_poja_application_environment_id', 'env_1_depl_files_1_id',
 'https://example.com/deploy1', '2024-08-01 10:15:00',
 'main', 'John Doe', 'Initial deployment', 'abc123def456',
 'poja-org', true, 'repo1_id', false, 'https://github.com/poja-org/repo1',
 'repo1', 'organization', 'john.doe@example.com', 'johndoe', '12345678',
 'https://avatars.githubusercontent.com/u/12345678'),

-- Second entry on 02 August 2024
('deployment_2_id', 'other_poja_application_id', 'other_poja_application_environment_2_id', 'env_1_depl_files_2_id',
 'https://example.com/deploy2', '2024-08-02 14:30:00',
 'develop', 'Jane Smith', 'Bug fixes', '789ghi012jkl',
 'poja-org', true, 'repo2_id', true, 'https://github.com/poja-org/repo2',
 'repo2', 'user', 'jane.smith@example.com', 'janesmith', '87654321',
 'https://avatars.githubusercontent.com/u/87654321'),

-- Third entry on 02 August 2024 with archived environment
('deployment_3_id', 'other_poja_application_id', 'archived_other_poja_app_env_id', 'env_1_depl_files_1_id',
 'https://example.com/deploy3', '2024-08-02 09:00:00',
 'release', 'Sam Brown', 'Final deployment', 'mno345pqr678',
 'poja-org', false, 'repo3_id', false, 'https://github.com/poja-org/repo3',
 'repo3', 'organization', 'sam.brown@example.com', 'sambrown', '98765432',
 'https://avatars.githubusercontent.com/u/98765432');
