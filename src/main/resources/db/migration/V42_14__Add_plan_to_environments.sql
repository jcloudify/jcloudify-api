alter table "environment"
    add column id_plan varchar;

alter table "environment"
    add constraint fk_plan foreign key (id_plan) references "plan" (id);