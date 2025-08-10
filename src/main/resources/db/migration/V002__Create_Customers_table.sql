create table customers
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6)  not null,
    email      varchar(100) not null,
    name       varchar(100) not null,
    phone      varchar(20)  null,
    updated_at datetime(6)  null,
    constraint UKrfbvkrffamfql7cjmen8v976v
        unique (email)
);

create index idx_customer_created_at
    on customers (created_at);

create index idx_customer_email
    on customers (email);

create index idx_customer_name
    on customers (name);

