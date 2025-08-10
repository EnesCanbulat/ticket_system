create table ticket_statuses
(
    id          bigint auto_increment
        primary key,
    description varchar(200) null,
    name        varchar(50)  not null,
    constraint UKfx8a84h5xxsatq8innj0047ux
        unique (name)
);

create index idx_status_name
    on ticket_statuses (name);

