alter table "app_installation"
    add column "type" varchar;
alter table "app_installation"
    drop column "is_org";
alter table "app_installation"
    add column "avatar_url" varchar;
