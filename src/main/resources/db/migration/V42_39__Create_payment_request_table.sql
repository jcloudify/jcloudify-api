CREATE TABLE IF NOT EXISTS "payment_request" (
    id varchar constraint pk_payment_request primary key default uuid_generate_v4(),
    request_instant timestamp
);