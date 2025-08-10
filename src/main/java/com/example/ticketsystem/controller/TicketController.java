package com.example.ticketsystem.controller;

import com.example.ticketsystem.dto.AssignTicketRequest;
import com.example.ticketsystem.dto.CreateTicketRequest;
import com.example.ticketsystem.dto.MessageResponse;
import com.example.ticketsystem.dto.SendMessageRequest;
import com.example.ticketsystem.dto.TicketResponse;
import com.example.ticketsystem.entity.Customer;
import com.example.ticketsystem.entity.Ticket;
import com.example.ticketsystem.entity.TicketPriority;
import com.example.ticketsystem.entity.TicketStatus;
import com.example.ticketsystem.repository.AgentRepository;
import com.example.ticketsystem.repository.CustomerRepository;
import com.example.ticketsystem.repository.TicketMessageRepository;
import com.example.ticketsystem.repository.TicketPriorityRepository;
import com.example.ticketsystem.repository.TicketRepository;
import com.example.ticketsystem.repository.TicketStatusRepository;
import com.example.ticketsystem.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import com.example.ticketsystem.entity.Agent;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;


    private final CustomerRepository customerRepository;
    private final TicketStatusRepository ticketStatusRepository;
    private final TicketPriorityRepository ticketPriorityRepository;
    private final AgentRepository agentRepository;
    private final TicketRepository ticketRepository;
    private final TicketMessageRepository ticketMessageRepository;


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
            log.error("Validation error - Field: {}, Message: {}", fieldName, errorMessage);
        });

        errors.put("status", 400);
        errors.put("error", "Validation Failed");
        errors.put("message", "Girilen veriler geçersiz");
        errors.put("fieldErrors", fieldErrors);

        log.error("Total validation errors: {}", fieldErrors.size());
        return ResponseEntity.badRequest().body(errors);
    }



    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@Valid @RequestBody CreateTicketRequest request) {
        try {
            log.info("=== CREATE TICKET REQUEST ===");
            log.info("CustomerId: {}", request.customerId());
            log.info("Title: '{}'", request.title());
            log.info("Description length: {}", request.description() != null ? request.description().length() : "null");
            log.info("PriorityId: {}", request.priorityId());

            TicketResponse response = ticketService.create(request);

            log.info("=== TICKET CREATED SUCCESSFULLY ===");
            log.info("Ticket ID: {}", response.id());
            return response;

        } catch (Exception e) {
            log.error("=== CREATE TICKET ERROR ===");
            log.error("Error type: {}", e.getClass().getSimpleName());
            log.error("Error message: {}", e.getMessage());
            log.error("Stack trace: ", e);
            throw e;
        }
    }

    @PostMapping("/{id}/assign")
    public TicketResponse assign(@PathVariable Long id, @Valid @RequestBody AssignTicketRequest request) {
        return ticketService.assign(id, request);
    }

    @PatchMapping("/{id}/status/{statusId}")
    public TicketResponse updateStatus(@PathVariable Long id, @PathVariable Long statusId) {
        return ticketService.updateStatus(id, statusId);
    }

    @PostMapping("/{id}/close")
    public TicketResponse close(@PathVariable Long id) {
        return ticketService.close(id);
    }

    @PostMapping("/{id}/messages")
    public TicketResponse sendMessage(@PathVariable Long id, @Valid @RequestBody SendMessageRequest request) {
        return ticketService.sendMessage(id, request);
    }

    @GetMapping("/{id}")
    public TicketResponse get(@PathVariable Long id) {
        return ticketService.get(id);
    }

    @GetMapping
    public Page<TicketResponse> list(Pageable pageable) {
        return ticketService.list(pageable);
    }

    @GetMapping("/{id}/messages")
    public List<MessageResponse> getMessages(@PathVariable Long id) {
        return ticketService.getMessages(id);
    }

    // === DEBUG ENDPOINTS ===

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Controller çalışıyor! Zaman: " + LocalDateTime.now());
    }

    @GetMapping("/debug/database")
    public ResponseEntity<Map<String, Object>> debugDatabase() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("=== DATABASE DEBUG TEST ===");

            // Repository counts
            long customerCount = customerRepository.count();
            long statusCount = ticketStatusRepository.count();
            long priorityCount = ticketPriorityRepository.count();
            long agentCount = agentRepository.count();
            long ticketCount = ticketRepository.count();
            long messageCount = ticketMessageRepository.count();

            result.put("customerCount", customerCount);
            result.put("statusCount", statusCount);
            result.put("priorityCount", priorityCount);
            result.put("agentCount", agentCount);
            result.put("ticketCount", ticketCount);
            result.put("messageCount", messageCount);

            log.info("Customers: {}, Status: {}, Priority: {}, Agents: {}, Tickets: {}, Messages: {}",
                    customerCount, statusCount, priorityCount, agentCount, ticketCount, messageCount);


            if (customerCount > 0) {
                Optional<Customer> customer = customerRepository.findById(1L);
                result.put("customer1Exists", customer.isPresent());
                if (customer.isPresent()) {
                    result.put("customer1Name", customer.get().getName());
                    result.put("customer1Email", customer.get().getEmail());
                }
            }

            if (statusCount > 0) {
                Optional<TicketStatus> status = ticketStatusRepository.findById(1L);
                result.put("status1Exists", status.isPresent());
                if (status.isPresent()) {
                    result.put("status1Name", status.get().getName());
                }


                try {
                    Optional<TicketStatus> openStatus = ticketStatusRepository.findByName("OPEN");
                    result.put("openStatusByName", openStatus.isPresent());
                    if (openStatus.isPresent()) {
                        result.put("openStatusName", openStatus.get().getName());
                    }
                } catch (Exception e) {
                    result.put("findByNameError", e.getMessage());
                }
            }

            if (priorityCount > 0) {
                Optional<TicketPriority> priority = ticketPriorityRepository.findById(1L);
                result.put("priority1Exists", priority.isPresent());
                if (priority.isPresent()) {
                    result.put("priority1Name", priority.get().getName());
                    result.put("priority1Level", priority.get().getLevel());
                }
            }

            result.put("timestamp", LocalDateTime.now());
            result.put("status", "SUCCESS");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Database debug error: ", e);
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/debug/simple-create")
    public ResponseEntity<String> debugSimpleCreate() {
        try {
            log.info("=== SIMPLE CREATE TEST ===");


            Optional<Customer> customer = customerRepository.findById(1L);
            if (!customer.isPresent()) {
                return ResponseEntity.status(400).body("ERROR: Customer ID=1 not found");
            }
            log.info("✓ Customer found: {}", customer.get().getName());


            Optional<TicketPriority> priority = ticketPriorityRepository.findById(2L);
            if (!priority.isPresent()) {
                priority = ticketPriorityRepository.findAll().stream().findFirst();
                if (!priority.isPresent()) {
                    return ResponseEntity.status(400).body("ERROR: No priority found");
                }
            }
            log.info("✓ Priority found: {} (level={})", priority.get().getName(), priority.get().getLevel());


            Optional<TicketStatus> status = ticketStatusRepository.findById(1L);
            if (!status.isPresent()) {
                status = ticketStatusRepository.findAll().stream().findFirst();
                if (!status.isPresent()) {
                    return ResponseEntity.status(400).body("ERROR: No status found");
                }
            }
            log.info("✓ Status found: {}", status.get().getName());


            Ticket ticket = new Ticket();
            ticket.setCustomer(customer.get());
            ticket.setTitle("Debug Test Ticket");
            ticket.setDescription("Bu ticket debug amaçlı oluşturulmuştur test description");
            ticket.setPriority(priority.get());
            ticket.setStatus(status.get());
            ticket.setCreatedAt(LocalDateTime.now());
            ticket.setUpdatedAt(LocalDateTime.now());

            log.info("✓ Ticket object created, saving...");


            ticket = ticketRepository.save(ticket);
            log.info("✓ Ticket saved with ID: {}", ticket.getId());


            TicketResponse response = TicketResponse.basic(
                    ticket.getId(),
                    ticket.getTitle(),
                    ticket.getDescription(),
                    ticket.getStatus().getName(),
                    ticket.getPriority().getName(),
                    ticket.getCustomer().getName(),
                    ticket.getCreatedAt(),
                    ticket.getUpdatedAt()
            );

            log.info("✓ TicketResponse created successfully");

            return ResponseEntity.ok(String.format(
                    "SUCCESS: Ticket created with ID=%d, Title='%s', Status='%s', Priority='%s'",
                    response.id(), response.title(), response.status(), response.priority()
            ));

        } catch (Exception e) {
            log.error("Simple create test error: ", e);
            return ResponseEntity.status(500).body("ERROR: " + e.getMessage() + " | Type: " + e.getClass().getSimpleName());
        }
    }

    @PostMapping("/debug/raw")
    public ResponseEntity<Map<String, Object>> debugRawRequest(@RequestBody Map<String, Object> rawRequest) {
        log.info("=== DEBUG RAW REQUEST ===");
        log.info("Raw request: {}", rawRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("received", rawRequest);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/debug/seed-data")
    public ResponseEntity<Map<String, Object>> seedTestData() {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("=== SEEDING TEST DATA ===");


            if (customerRepository.count() == 0) {
                Customer customer = new Customer("Test Müşteri", "test@example.com", "05551234567");
                customerRepository.save(customer);
                log.info("✓ Customer created");
            }


            if (ticketStatusRepository.count() == 0) {
                TicketStatus[] statuses = {
                        new TicketStatus("OPEN", "Açık durumda"),
                        new TicketStatus("ASSIGNED", "Atanmış durumda"),
                        new TicketStatus("IN_PROGRESS", "İşlem yapılıyor"),
                        new TicketStatus("CLOSED", "Kapatılmış durumda")
                };

                for (TicketStatus status : statuses) {
                    ticketStatusRepository.save(status);
                }
                log.info("✓ Statuses created");
            }


            if (ticketPriorityRepository.count() == 0) {
                TicketPriority[] priorities = {
                        new TicketPriority("Düşük", 1),
                        new TicketPriority("Normal", 2),
                        new TicketPriority("Yüksek", 3),
                        new TicketPriority("Acil", 4)
                };

                for (TicketPriority priority : priorities) {
                    ticketPriorityRepository.save(priority);
                }
                log.info("✓ Priorities created");
            }


            if (agentRepository.count() == 0) {
                Agent agent = new Agent("Test Agent", "agent@example.com", "05559876543");
                agentRepository.save(agent);
                log.info("✓ Agent created");
            }


            result.put("customerCount", customerRepository.count());
            result.put("statusCount", ticketStatusRepository.count());
            result.put("priorityCount", ticketPriorityRepository.count());
            result.put("agentCount", agentRepository.count());
            result.put("message", "Test data seeded successfully!");
            result.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error seeding data: ", e);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(result);
        }
    }
}