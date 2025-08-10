package com.example.ticketsystem.service.impl;

import com.example.ticketsystem.dto.AgentReplyRequest;
import com.example.ticketsystem.dto.AssignTicketRequest;
import com.example.ticketsystem.dto.CreateTicketRequest;
import com.example.ticketsystem.dto.MessageResponse;
import com.example.ticketsystem.dto.TicketResponse;
import com.example.ticketsystem.dto.SendMessageRequest;
import com.example.ticketsystem.entity.Agent;
import com.example.ticketsystem.entity.Customer;
import com.example.ticketsystem.entity.Ticket;
import com.example.ticketsystem.entity.TicketMessage;
import com.example.ticketsystem.entity.TicketPriority;
import com.example.ticketsystem.entity.TicketStatus;
import com.example.ticketsystem.repository.AgentRepository;
import com.example.ticketsystem.repository.CustomerRepository;
import com.example.ticketsystem.repository.TicketMessageRepository;
import com.example.ticketsystem.repository.TicketPriorityRepository;
import com.example.ticketsystem.repository.TicketRepository;
import com.example.ticketsystem.repository.TicketStatusRepository;
import com.example.ticketsystem.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final AgentRepository agentRepository;
    private final TicketStatusRepository ticketStatusRepository;
    private final TicketPriorityRepository ticketPriorityRepository;
    private final TicketMessageRepository ticketMessageRepository;

    private static final DateTimeFormatter DEFAULT_DT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_ASSIGNED = "ASSIGNED";
    private static final String STATUS_CLOSED = "CLOSED";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";



    @Override
    public TicketResponse create(CreateTicketRequest request) {
        try {
            log.debug("Creating ticket for request: {}", request);

            if (request == null || !request.hasValidData()) {
                throw new IllegalArgumentException("Geçersiz create ticket isteği");
            }

            Customer customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> notFound("Customer", request.customerId()));


            TicketPriority priority;
            if (request.priorityId() != null) {
                priority = ticketPriorityRepository.findById(request.priorityId())
                        .orElseThrow(() -> notFound("TicketPriority", request.priorityId()));
            } else {

                priority = ticketPriorityRepository.findById(2L)
                        .or(() -> ticketPriorityRepository.findAll().stream().findFirst())
                        .orElseThrow(() -> new IllegalStateException("Varsayılan öncelik bulunamadı"));
            }


            TicketStatus openStatus = findStatusByName(STATUS_OPEN)
                    .or(() -> ticketStatusRepository.findById(1L)) // Default status ID=1
                    .or(() -> ticketStatusRepository.findAll().stream().findFirst())
                    .orElseThrow(() -> new IllegalStateException("Açık (OPEN) durumu bulunamadı"));

            LocalDateTime now = LocalDateTime.now();

            Ticket ticket = new Ticket();
            ticket.setCustomer(customer);
            ticket.setTitle(request.getTrimmedTitle());
            ticket.setDescription(request.getTrimmedDescription());
            ticket.setPriority(priority);
            ticket.setStatus(openStatus);
            ticket.setCreatedAt(now);
            ticket.setUpdatedAt(now);

            ticket = ticketRepository.save(ticket);
            log.debug("Ticket created successfully with ID: {}", ticket.getId());

            return toDetailedResponse(ticket);
        } catch (Exception e) {
            log.error("Error creating ticket: ", e);
            throw e;
        }
    }

    @Override
    public TicketResponse assign(Long ticketId, AssignTicketRequest request) {
        try {
            Ticket ticket = requireTicket(ticketId);
            Agent agent = agentRepository.findById(request.agentId())
                    .orElseThrow(() -> notFound("Agent", request.agentId()));

            TicketStatus assigned = findStatusByName(STATUS_ASSIGNED)
                    .or(() -> ticketStatusRepository.findById(2L)) // Default assigned status ID=2
                    .orElseThrow(() -> new IllegalStateException("Atanmış (ASSIGNED) durumu bulunamadı"));

            ticket.setAgent(agent);
            ticket.setStatus(assigned);
            ticket.setUpdatedAt(LocalDateTime.now());

            if (request.hasNote()) {
                createSystemMessage(ticket, "Atama Notu: " + request.getTrimmedNote());
            }

            return toDetailedResponse(ticket);
        } catch (Exception e) {
            log.error("Error assigning ticket {}: ", ticketId, e);
            throw e;
        }
    }

    @Override
    public TicketResponse updateStatus(Long ticketId, Long statusId) {
        try {
            Ticket ticket = requireTicket(ticketId);
            TicketStatus newStatus = ticketStatusRepository.findById(statusId)
                    .orElseThrow(() -> notFound("TicketStatus", statusId));

            ticket.setStatus(newStatus);
            ticket.setUpdatedAt(LocalDateTime.now());


            if (STATUS_CLOSED.equalsIgnoreCase(newStatus.getName()) ||
                    "Kapalı".equalsIgnoreCase(newStatus.getName())) {
                ticket.setClosedAt(LocalDateTime.now());
            }

            return toDetailedResponse(ticket);
        } catch (Exception e) {
            log.error("Error updating status for ticket {}: ", ticketId, e);
            throw e;
        }
    }

    @Override
    public TicketResponse close(Long ticketId) {
        try {
            Ticket ticket = requireTicket(ticketId);
            TicketStatus closed = findStatusByName(STATUS_CLOSED)
                    .or(() -> ticketStatusRepository.findById(3L)) // Default closed status ID=3
                    .orElseThrow(() -> new IllegalStateException("Kapalı (CLOSED) durumu bulunamadı"));

            ticket.setStatus(closed);
            ticket.setClosedAt(LocalDateTime.now());
            ticket.setUpdatedAt(LocalDateTime.now());

            return toDetailedResponse(ticket);
        } catch (Exception e) {
            log.error("Error closing ticket {}: ", ticketId, e);
            throw e;
        }
    }

    @Override
    public TicketResponse sendMessage(Long ticketId, SendMessageRequest request) {
        try {
            Ticket ticket = requireTicket(ticketId);

            if (request == null || !request.hasValidData()) {
                throw new IllegalArgumentException("Geçersiz mesaj isteği");
            }

            TicketMessage.SenderType senderType = detectSenderType(request.senderId());

            TicketMessage message = new TicketMessage();
            message.setTicket(ticket);
            message.setSenderId(request.senderId());
            message.setSenderType(senderType);
            message.setMessage(request.getTrimmedMessage());
            message.setCreatedAt(LocalDateTime.now());
            ticketMessageRepository.save(message);

            // Agent cevaplıyorsa status güncelle
            if (senderType == TicketMessage.SenderType.AGENT) {
                findStatusByName(STATUS_IN_PROGRESS)
                        .or(() -> ticketStatusRepository.findById(4L))
                        .ifPresent(status -> {
                            ticket.setStatus(status);
                            ticket.setUpdatedAt(LocalDateTime.now());
                        });
            }

            return toDetailedResponse(ticket);
        } catch (Exception e) {
            log.error("Error sending message for ticket {}: ", ticketId, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public TicketResponse get(Long ticketId) {
        return toDetailedResponse(requireTicket(ticketId));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TicketResponse> list(Pageable pageable) {
        return ticketRepository.findAll(pageable)
                .map(this::toBasicResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MessageResponse> getMessages(Long ticketId) {
        requireTicket(ticketId);
        return ticketMessageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }



    @Override
    public TicketResponse agentReply(Long ticketId, AgentReplyRequest request) {
        try {
            log.debug("Agent {} replying to ticket {}", request.agentId(), ticketId);

            Ticket ticket = requireTicket(ticketId);


            Agent agent = agentRepository.findById(request.agentId())
                    .orElseThrow(() -> notFound("Agent", request.agentId()));


            if (ticket.getAgent() == null) {
                log.info("Auto-assigning ticket {} to agent {}", ticketId, request.agentId());
                ticket.setAgent(agent);

                // Status'u ASSIGNED yap
                findStatusByName(STATUS_ASSIGNED)
                        .or(() -> ticketStatusRepository.findById(2L))
                        .ifPresent(ticket::setStatus);
            }


            TicketMessage message = new TicketMessage();
            message.setTicket(ticket);
            message.setSenderId(request.agentId());
            message.setSenderType(TicketMessage.SenderType.AGENT);
            message.setMessage(request.getTrimmedMessage());
            message.setCreatedAt(LocalDateTime.now());


            if (request.isInternalMessage()) {
                message.setMessageType(TicketMessage.MessageType.INTERNAL);
            }

            ticketMessageRepository.save(message);


            ticket.setUpdatedAt(LocalDateTime.now());


            if (request.hasStatusUpdate()) {
                TicketStatus newStatus = ticketStatusRepository.findById(request.newStatusId())
                        .orElseThrow(() -> notFound("TicketStatus", request.newStatusId()));
                ticket.setStatus(newStatus);


                if (STATUS_CLOSED.equalsIgnoreCase(newStatus.getName()) ||
                        "Kapalı".equalsIgnoreCase(newStatus.getName())) {
                    ticket.setClosedAt(LocalDateTime.now());
                }
            } else {

                findStatusByName(STATUS_IN_PROGRESS)
                        .or(() -> ticketStatusRepository.findById(3L))
                        .ifPresent(ticket::setStatus);
            }

            log.debug("Agent reply saved successfully for ticket {}", ticketId);
            return toDetailedResponse(ticket);

        } catch (Exception e) {
            log.error("Error in agent reply for ticket {}: ", ticketId, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TicketResponse> getAgentTickets(Long agentId, Pageable pageable) {
        try {
            log.debug("Getting tickets for agent {}", agentId);

            // Agent'ı kontrol et
            agentRepository.findById(agentId)
                    .orElseThrow(() -> notFound("Agent", agentId));

            // Agent'a atanmış ticket'ları getir
            return ticketRepository.findByAgentIdOrderByUpdatedAtDesc(agentId, pageable)
                    .map(this::toBasicResponse);

        } catch (Exception e) {
            log.error("Error getting agent {} tickets: ", agentId, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<TicketResponse> getUnassignedTickets(Pageable pageable) {
        try {
            log.debug("Getting unassigned tickets");


            return ticketRepository.findByAgentIsNullOrderByCreatedAtAsc(pageable)
                    .map(this::toBasicResponse);

        } catch (Exception e) {
            log.error("Error getting unassigned tickets: ", e);
            throw e;
        }
    }



    private Ticket requireTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> notFound("Ticket", id));
    }

    private RuntimeException notFound(String what, Object id) {
        return new NoSuchElementException("%s not found: %s".formatted(what, id));
    }


    private Optional<TicketStatus> findStatusByName(String statusName) {
        try {
            return ticketStatusRepository.findByName(statusName);
        } catch (Exception e) {
            log.warn("findByName method not available for status: {}", statusName);
            return Optional.empty();
        }
    }

    private TicketMessage.SenderType detectSenderType(Long senderId) {
        try {
            if (senderId == null || senderId == 0L) {
                return TicketMessage.SenderType.SYSTEM;
            }

            if (agentRepository.findById(senderId).isPresent()) {
                return TicketMessage.SenderType.AGENT;
            }
            if (customerRepository.findById(senderId).isPresent()) {
                return TicketMessage.SenderType.CUSTOMER;
            }
            return TicketMessage.SenderType.SYSTEM;
        } catch (Exception e) {
            log.warn("Error detecting sender type for ID {}: ", senderId, e);
            return TicketMessage.SenderType.SYSTEM;
        }
    }

    private void createSystemMessage(Ticket ticket, String body) {
        try {
            TicketMessage m = new TicketMessage();
            m.setTicket(ticket);
            m.setSenderType(TicketMessage.SenderType.SYSTEM);
            m.setSenderId(0L);
            m.setMessage(body);
            m.setCreatedAt(LocalDateTime.now());
            ticketMessageRepository.save(m);
        } catch (Exception e) {
            log.error("Error creating system message: ", e);
        }
    }

    private TicketResponse toBasicResponse(Ticket t) {
        return TicketResponse.basic(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus() != null ? t.getStatus().getName() : "Unknown",
                t.getPriority() != null ? t.getPriority().getName() : "Unknown",
                t.getCustomer() != null ? t.getCustomer().getName() : "Unknown",
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }

    private TicketResponse toDetailedResponse(Ticket t) {
        try {
            List<MessageResponse> messages = ticketMessageRepository
                    .findByTicketIdOrderByCreatedAtAsc(t.getId())
                    .stream()
                    .map(this::toMessageResponse)
                    .toList();

            return TicketResponse.detailed(
                    t.getId(),
                    t.getTitle(),
                    t.getDescription(),
                    t.getStatus() != null ? t.getStatus().getName() : "Unknown",
                    t.getPriority() != null ? t.getPriority().getName() : "Unknown",
                    t.getCustomer() != null ? t.getCustomer().getName() : "Unknown",
                    t.getCustomer() != null ? t.getCustomer().getEmail() : null,
                    t.getAgent() != null ? t.getAgent().getName() : null,
                    t.getAgent() != null ? t.getAgent().getEmail() : null,
                    t.getCreatedAt(),
                    t.getUpdatedAt(),
                    t.getClosedAt(),
                    messages
            );
        } catch (Exception e) {
            log.error("Error creating detailed response for ticket {}: ", t.getId(), e);
            throw e;
        }
    }

    private MessageResponse toMessageResponse(TicketMessage m) {
        try {
            String senderName = "Unknown";
            String senderEmail = null;


            switch (m.getSenderType()) {
                case AGENT:
                    Optional<Agent> agent = agentRepository.findById(m.getSenderId());
                    if (agent.isPresent()) {
                        senderName = agent.get().getName();
                        senderEmail = agent.get().getEmail();
                    }
                    break;
                case CUSTOMER:
                    Optional<Customer> customer = customerRepository.findById(m.getSenderId());
                    if (customer.isPresent()) {
                        senderName = customer.get().getName();
                        senderEmail = customer.get().getEmail();
                    }
                    break;
                case SYSTEM:
                    senderName = "System";
                    senderEmail = null;
                    break;
                default:
                    senderName = "Unknown";
                    break;
            }

            var metadata = new MessageResponse.MessageMetadata(
                    m.getMessage() != null ? m.getMessage().length() : 0,
                    m.getCreatedAt() != null ? m.getCreatedAt().format(DEFAULT_DT_FORMAT) : null
            );

            return new MessageResponse(
                    m.getId(),
                    m.getSenderType() != null ? m.getSenderType().name() : "UNKNOWN",
                    senderName,
                    senderEmail,
                    m.getMessage(),
                    m.getCreatedAt(),
                    metadata
            );
        } catch (Exception e) {
            log.error("Error creating message response: ", e);
            throw e;
        }
    }
}