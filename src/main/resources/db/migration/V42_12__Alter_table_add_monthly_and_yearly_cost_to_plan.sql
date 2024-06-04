alter table "plan" rename column cost to monthly_cost;

alter table "plan" add column yearly_cost double precision;