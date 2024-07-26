alter table application
    add column "installation_id" varchar;
alter table application
    add constraint "fk_app_install" foreign key (installation_id) references "app_installation" ("id");
