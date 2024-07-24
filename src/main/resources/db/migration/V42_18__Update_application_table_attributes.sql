alter table "application"
    rename column "github_repository" to "github_repository_name";
alter table "application"
    add column "is_github_repository_private" boolean;
