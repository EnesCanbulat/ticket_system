create table tickets
(
    id          bigint auto_increment
        primary key,
    closed_at   datetime(6)  null,
    created_at  datetime(6)  not null,
    description text         not null,
    title       varchar(200) not null,
    updated_at  datetime(6)  not null,
    agent_id    bigint       null,
    customer_id bigint       not null,
    priority_id bigint       not null,
    status_id   bigint       not null,
    constraint FK4lqfc7l5suniniarnp7eur9dh
        foreign key (status_id) references ticket_statuses (id),
    constraint FKc0w0mcmslcg7wt8aomssvhcrr
        foreign key (priority_id) references ticket_priorities (id),
    constraint FKi81xre2n3j3as1sp24j440kq1
        foreign key (customer_id) references customers (id),
    constraint FKod7hki6d5kpasjet8w6glky7m
        foreign key (agent_id) references agents (id)
);

create index idx_ticket_agent
    on tickets (agent_id);

create index idx_ticket_agent_status
    on tickets (agent_id, status_id);

create index idx_ticket_created_at
    on tickets (created_at);

create index idx_ticket_customer
    on tickets (customer_id);

create index idx_ticket_customer_status
    on tickets (customer_id, status_id);

create index idx_ticket_priority
    on tickets (priority_id);

create index idx_ticket_status
    on tickets (status_id);

create index idx_ticket_status_priority
    on tickets (status_id, priority_id);

create index idx_ticket_updated_at
    on tickets (updated_at);

