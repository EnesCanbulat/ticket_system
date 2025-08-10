create table ticket_messages
(
    id                  bigint auto_increment
        primary key,
    created_at          datetime(6)                                                                                                                not null,
    message             text                                                                                                                       not null,
    sender_id           bigint                                                                                                                     not null,
    sender_type         enum ('ADMIN', 'AGENT', 'CUSTOMER', 'SYSTEM')                                                                              not null,
    ticket_id           bigint                                                                                                                     not null,
    deleted_at          datetime(6)                                                                                                                null,
    internal_notes      text                                                                                                                       null,
    is_active           bit                                                                                                                        not null,
    message_type        enum ('ASSIGNMENT', 'ESCALATION', 'FOLLOW_UP', 'INTERNAL', 'NORMAL', 'RESOLUTION', 'STATUS_UPDATE', 'SYSTEM_NOTIFICATION') null,
    priority            enum ('HIGH', 'LOW', 'NORMAL', 'URGENT')                                                                                   null,
    read_at             datetime(6)                                                                                                                null,
    reply_to_message_id bigint                                                                                                                     null,
    updated_at          datetime(6)                                                                                                                null,
    constraint FK8c9vw45c3j5m6arnwp4c3iaby
        foreign key (ticket_id) references tickets (id)
);

create index idx_message_created_at
    on ticket_messages (created_at);

create index idx_message_sender
    on ticket_messages (sender_type, sender_id);

create index idx_message_ticket
    on ticket_messages (ticket_id);

create index idx_message_ticket_created
    on ticket_messages (ticket_id, created_at);

create index idx_ticket_message_created_at
    on ticket_messages (created_at);

create index idx_ticket_message_sender
    on ticket_messages (sender_type, sender_id);

create index idx_ticket_message_ticket_id
    on ticket_messages (ticket_id);

