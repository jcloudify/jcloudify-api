CREATE TABLE IF NOT EXISTS "payment_request" (
    id varchar constraint pk_payment_request primary key default uuid_generate_v4(),
    period integer check (period >= 0 and period <= 11),
    request_instant timestamp
);