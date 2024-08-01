alter table "application"
    add column "github_repository_id" varchar;
alter table "application"
    add constraint unique_gh_repo_id unique (github_repository_id);
