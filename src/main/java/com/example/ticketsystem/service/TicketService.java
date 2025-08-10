package com.example.ticketsystem.service;

import com.example.ticketsystem.dto.AssignTicketRequest;
import com.example.ticketsystem.dto.CreateTicketRequest;
import com.example.ticketsystem.dto.SendMessageRequest;
import com.example.ticketsystem.dto.MessageResponse;
import com.example.ticketsystem.dto.TicketResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.ticketsystem.dto.AgentReplyRequest;

import java.util.List;

public interface TicketService {

    TicketResponse create(CreateTicketRequest request);

    TicketResponse assign(Long ticketId, AssignTicketRequest request);

    TicketResponse updateStatus(Long ticketId, Long statusId);

    TicketResponse close(Long ticketId);

    TicketResponse sendMessage(Long ticketId, SendMessageRequest request);

    TicketResponse get(Long ticketId);

    Page<TicketResponse> list(Pageable pageable);

    List<MessageResponse> getMessages(Long ticketId);


    TicketResponse agentReply(Long ticketId, AgentReplyRequest request);


    Page<TicketResponse> getAgentTickets(Long agentId, Pageable pageable);


    Page<TicketResponse> getUnassignedTickets(Pageable pageable);

}