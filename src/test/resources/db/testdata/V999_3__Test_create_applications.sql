insert into "application"(id, name, archived, github_repository_name, is_github_repository_private, creation_datetime,
                          id_user, "description", "repo_http_url", "installation_id", github_repository_id)
values ('poja_application_id', 'poja-test-app', false, 'poja_application', false,
        '2023-06-18T10:15:30.00Z', 'joe_doe_id', 'a regular poja app', 'http://github.com/user/repo',
        'gh_app_install_1_id', 'gh_repository_1_id'),
       ('poja_application_2_id', 'poja-test-app-2', false, 'poja_application_2', false,
        '2023-06-18T10:16:30.00Z', 'joe_doe_id', 'a regular poja app', 'http://github.com/user/repo',
        'gh_app_install_1_id', 'gh_repository_2_id'),
       ('poja_application_3_id', 'poja-test-app-3', false, 'poja_application_3', false,
        '2023-06-18T10:17:30.00Z', 'jane_doe_id', 'a regular poja app', 'http://github.com/user/repo',
        'gh_app_install_2_id', 'gh_repository_3_id'),
       ('other_poja_application_id', 'other-poja-test-app', false, 'other_poja_application', false,
        '2023-06-18T10:15:30.00Z', 'joe_doe_id', 'a regular poja app', 'http://github.com/user/repo',
        'gh_app_install_1_id', 'gh_repository_4_id');
