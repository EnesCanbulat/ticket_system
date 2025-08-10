create table ticket_priorities
(
    id    bigint auto_increment
        primary key,
    level int         not null,
    name  varchar(50) not null,
    constraint UK7rtvb69qp6qtmi7k0pxa8ttse
        unique (level),
    constraint UKnqqjj2o2077dxmdn56ehnb5i8
        unique (name),
    check ((`level` <= 10) and (`level` >= 1))
);

create index idx_priority_level
    on ticket_priorities (level);

create index idx_priority_name
    on ticket_priorities (name);

