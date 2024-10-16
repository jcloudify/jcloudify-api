do
$$
    begin
        if not exists(select from pg_type where typname = 'invoice_status') then
            create type invoice_status as enum ('DRAFT',
                'OPEN',
                'PAID',
                'PROCESSING',
                'REQUIRES_ACTION',
                'REQUIRES_CAPTURE',
                'REQUIRES_CONFIRMATION',
                'REQUIRES_PAYMENT_METHOD');
        end if;
    end
$$;

CREATE TABLE IF NOT EXISTS "user_payment_request" (
    id varchar constraint pk_user_payment_request primary key default uuid_generate_v4(),
    amount numeric,
    invoice_id varchar,
    invoice_status invoice_status default 'OPEN',
    invoice_url varchar,
    payment_request_id varchar,
    user_id varchar,
    constraint fk_payment_request foreign key (payment_request_id) references "payment_request" (id),
    constraint fk_user foreign key (user_id) references "user" (id)
)