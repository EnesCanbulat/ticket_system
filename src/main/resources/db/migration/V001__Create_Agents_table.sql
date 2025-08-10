create table agents
(
    id         bigint auto_increment
        primary key,
    created_at datetime(6)  not null,
    email      varchar(100) not null,
    is_active  bit          not null,
    name       varchar(100) not null,
    phone      varchar(20)  null,
    updated_at datetime(6)  null,
    constraint UK6nicrn4ojbvsy06bwtbe719md
        unique (email)
);

create index idx_agent_active
    on agents (is_active);

create index idx_agent_active_name
    on agents (is_active, name);

create index idx_agent_created_at
    on agents (created_at);

create index idx_agent_email
    on agents (email);

create index idx_agent_name
    on agents (name);

