CREATE TABLE IF NOT EXISTS "user_payment_request" (
    id varchar constraint pk_user_payment_request primary key default uuid_generate_v4(),
    invoice_id varchar,
    status varchar default 'PENDING',
    invoice_url varchar,
    parent_id varchar,
    user_id varchar,
    constraint fk_payment_request (parent_id) references "payment_request" (id),
    constraint fk_user foreign_key (user_id) references "user" (id)
);