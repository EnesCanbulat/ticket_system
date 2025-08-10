package com.example.ticketsystem.controller;

import com.example.ticketsystem.dto.AgentReplyRequest;
import com.example.ticketsystem.dto.AssignTicketRequest;
import com.example.ticketsystem.dto.TicketResponse;
import com.example.ticketsystem.entity.Agent;
import com.example.ticketsystem.repository.AgentRepository;
import com.example.ticketsystem.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@RestController
@RequestMapping("/api/representatives")
@RequiredArgsConstructor
public class RepresentativeController {

    private final AgentRepository agentRepository;
    private final TicketService ticketService;



    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Agent create(@Valid @RequestBody Agent request) {
        log.info("Creating new representative: {}", request.getName());
        return agentRepository.save(request);
    }

    @GetMapping
    public Page<Agent> list(Pageable pageable) {
        log.debug("Listing representatives with pageable: {}", pageable);
        return agentRepository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public Agent get(@PathVariable Long id) {
        log.debug("Getting representative: {}", id);
        return agentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative not found"));
    }

    @PutMapping("/{id}")
    public Agent update(@PathVariable Long id, @Valid @RequestBody Agent update) {
        log.info("Updating representative: {}", id);
        var existing = agentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative not found"));
        existing.setName(update.getName());
        existing.setEmail(update.getEmail());
        existing.setPhone(update.getPhone());
        return agentRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("Deleting representative: {}", id);
        if (!agentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative not found");
        }
        agentRepository.deleteById(id);
    }



    @PostMapping("/{agentId}/tickets/{ticketId}/reply")
    public TicketResponse replyToTicket(@PathVariable Long agentId,
                                        @PathVariable Long ticketId,
                                        @Valid @RequestBody AgentReplyRequest request) {
        try {
            log.info("=== REPRESENTATIVE REPLY ===");
            log.info("Agent ID: {}, Ticket ID: {}, Message length: {}",
                    agentId, ticketId, request.getMessageLength());


            AgentReplyRequest updatedRequest = new AgentReplyRequest(
                    agentId,
                    request.message(),
                    request.newStatusId(),
                    request.isInternal()
            );

            TicketResponse response = ticketService.agentReply(ticketId, updatedRequest);

            log.info("✓ Representative reply sent successfully");
            return response;

        } catch (Exception e) {
            log.error("Error in representative reply: {}", e.getMessage(), e);
            throw e;
        }
    }


    @GetMapping("/{agentId}/tickets")
    public Page<TicketResponse> getAssignedTickets(@PathVariable Long agentId, Pageable pageable) {
        try {
            log.info("Getting assigned tickets for representative: {}", agentId);


            agentRepository.findById(agentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative not found"));

            return ticketService.getAgentTickets(agentId, pageable);
        } catch (Exception e) {
            log.error("Error getting representative tickets: {}", e.getMessage(), e);
            throw e;
        }
    }


    @PostMapping("/{agentId}/tickets/{ticketId}/assign")
    public TicketResponse assignTicketToSelf(@PathVariable Long agentId, @PathVariable Long ticketId) {
        try {
            log.info("Self-assigning ticket {} to representative {}", ticketId, agentId);


            Agent agent = agentRepository.findById(agentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative not found"));


            AssignTicketRequest assignRequest = new AssignTicketRequest(
                    agentId,
                    "Ticket " + agent.getName() + " tarafından kendisine atandı"
            );

            return ticketService.assign(ticketId, assignRequest);

        } catch (Exception e) {
            log.error("Error in self-assign: {}", e.getMessage(), e);
            throw e;
        }
    }


    @GetMapping("/tickets/unassigned")
    public Page<TicketResponse> getUnassignedTickets(Pageable pageable) {
        try {
            log.info("Getting unassigned tickets for representatives");
            return ticketService.getUnassignedTickets(pageable);
        } catch (Exception e) {
            log.error("Error getting unassigned tickets: {}", e.getMessage(), e);
            throw e;
        }
    }


    @PatchMapping("/{agentId}/tickets/{ticketId}/status")
    public TicketResponse updateTicketStatus(@PathVariable Long agentId,
                                             @PathVariable Long ticketId,
                                             @RequestParam Long statusId,
                                             @RequestParam(required = false) String note) {
        try {
            log.info("Representative {} updating ticket {} status to {}", agentId, ticketId, statusId);


            Agent agent = agentRepository.findById(agentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative not found"));


            TicketResponse ticket = ticketService.get(ticketId);
            if (ticket.agentName() == null) {

                AssignTicketRequest assignRequest = new AssignTicketRequest(
                        agentId,
                        "Durum güncellerken " + agent.getName() + " tarafından otomatik atandı"
                );
                ticketService.assign(ticketId, assignRequest);
            }


            TicketResponse response = ticketService.updateStatus(ticketId, statusId);


            if (note != null && !note.trim().isEmpty()) {
                AgentReplyRequest replyRequest = new AgentReplyRequest(
                        agentId,
                        "Durum güncellendi: " + note,
                        null,
                        true // Internal message
                );
                response = ticketService.agentReply(ticketId, replyRequest);
            }

            return response;

        } catch (Exception e) {
            log.error("Error in representative status update: {}", e.getMessage(), e);
            throw e;
        }
    }


    @PostMapping("/{agentId}/tickets/{ticketId}/close")
    public TicketResponse closeTicket(@PathVariable Long agentId,
                                      @PathVariable Long ticketId,
                                      @RequestParam(required = false) String closeNote) {
        try {
            log.info("Representative {} closing ticket {}", agentId, ticketId);


            Agent agent = agentRepository.findById(agentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative not found"));


            if (closeNote != null && !closeNote.trim().isEmpty()) {
                AgentReplyRequest replyRequest = new AgentReplyRequest(
                        agentId,
                        "Ticket kapatıldı: " + closeNote,
                        null,
                        false // Public message
                );
                ticketService.agentReply(ticketId, replyRequest);
            }


            return ticketService.close(ticketId);

        } catch (Exception e) {
            log.error("Error closing ticket: {}", e.getMessage(), e);
            throw e;
        }
    }


    @GetMapping("/{agentId}/dashboard")
    public RepresentativeDashboard getDashboard(@PathVariable Long agentId) {
        try {
            log.info("Getting dashboard for representative: {}", agentId);


            Agent agent = agentRepository.findById(agentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Representative not found"));


            Page<TicketResponse> assignedTickets = ticketService.getAgentTickets(agentId, Pageable.ofSize(5));
            Page<TicketResponse> unassignedTickets = ticketService.getUnassignedTickets(Pageable.ofSize(5));

            return new RepresentativeDashboard(
                    agent.getName(),
                    agent.getEmail(),
                    assignedTickets.getTotalElements(),
                    unassignedTickets.getTotalElements(),
                    assignedTickets.getContent(),
                    unassignedTickets.getContent()
            );

        } catch (Exception e) {
            log.error("Error getting dashboard: {}", e.getMessage(), e);
            throw e;
        }
    }


    public record RepresentativeDashboard(
            String name,
            String email,
            long assignedTicketCount,
            long unassignedTicketCount,
            java.util.List<TicketResponse> recentAssignedTickets,
            java.util.List<TicketResponse> recentUnassignedTickets
    ) {}
}