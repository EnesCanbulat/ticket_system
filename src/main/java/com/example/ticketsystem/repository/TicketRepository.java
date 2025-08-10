package com.example.ticketsystem.repository;

import com.example.ticketsystem.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {


    List<Ticket> findByCustomerId(Long customerId);
    List<Ticket> findByAgentId(Long agentId);
    Optional<Ticket> findByIdAndCustomerId(Long id, Long customerId);

    @Query("SELECT t FROM Ticket t WHERE t.status.id = :statusId ORDER BY t.createdAt DESC")
    List<Ticket> findByStatusId(@Param("statusId") Long statusId);

    @Query("SELECT t FROM Ticket t WHERE t.agent IS NULL ORDER BY t.priority.level DESC, t.createdAt ASC")
    List<Ticket> findUnassignedTickets();

    @Query("""
        SELECT t FROM Ticket t 
        WHERE t.agent.id = :agentId 
        AND t.status.id IN (1,2,3,4) 
        ORDER BY t.priority.level DESC, t.updatedAt DESC
        """)
    List<Ticket> findOpenTicketsByAgent(@Param("agentId") Long agentId);


    @Query("""
        SELECT t FROM Ticket t 
        LEFT JOIN FETCH t.customer 
        LEFT JOIN FETCH t.agent 
        LEFT JOIN FETCH t.status 
        LEFT JOIN FETCH t.priority 
        WHERE t.id = :ticketId
        """)
    Optional<Ticket> findTicketWithDetails(@Param("ticketId") Long ticketId);

    @Query("""
        SELECT t FROM Ticket t 
        LEFT JOIN FETCH t.customer 
        LEFT JOIN FETCH t.agent 
        LEFT JOIN FETCH t.status 
        LEFT JOIN FETCH t.priority 
        WHERE t.id = :ticketId
        """)
    Optional<Ticket> findTicketWithAllDetails(@Param("ticketId") Long ticketId);


    @Query("""
        SELECT t FROM Ticket t 
        JOIN t.customer c 
        WHERE (
            UPPER(t.title) LIKE UPPER(CONCAT('%', :searchTerm, '%'))
            OR UPPER(t.description) LIKE UPPER(CONCAT('%', :searchTerm, '%'))
            OR UPPER(c.name) LIKE UPPER(CONCAT('%', :searchTerm, '%'))
        )
        ORDER BY t.updatedAt DESC
        """)
    List<Ticket> searchTickets(@Param("searchTerm") String searchTerm);

    @Query("""
        SELECT t FROM Ticket t 
        WHERE t.priority.level >= :minPriority 
        AND t.status.id IN :statusIds 
        ORDER BY t.priority.level DESC, t.createdAt ASC
        """)
    List<Ticket> findByPriorityAndStatusIn(@Param("minPriority") Integer minPriority,
                                           @Param("statusIds") List<Long> statusIds);


    @Query("""
        SELECT t FROM Ticket t 
        WHERE t.createdAt BETWEEN :startDate AND :endDate 
        ORDER BY t.createdAt DESC
        """)
    List<Ticket> findTicketsCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT t FROM Ticket t 
        WHERE t.createdAt < :threshold 
        AND t.status.id IN (1, 2, 3, 4) 
        ORDER BY t.createdAt ASC
        """)
    List<Ticket> findOldOpenTickets(@Param("threshold") LocalDateTime threshold);

    @Query("""
        SELECT t FROM Ticket t 
        WHERE t.updatedAt < :threshold 
        AND t.status.id IN (2, 3, 4) 
        ORDER BY t.priority.level DESC, t.updatedAt ASC
        """)
    List<Ticket> findStaleTickets(@Param("threshold") LocalDateTime threshold);


    Page<Ticket> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    Page<Ticket> findByAgentIdOrderByUpdatedAtDesc(Long agentId, Pageable pageable);
    Page<Ticket> findByStatusIdOrderByCreatedAtDesc(Long statusId, Pageable pageable);
    Page<Ticket> findByAgentIsNullOrderByCreatedAtAsc(Pageable pageable);


    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status.id = :statusId")
    long countByStatusId(@Param("statusId") Long statusId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.agent.id = :agentId AND t.status.id IN (2,3,4)")
    long countActiveTicketsByAgent(@Param("agentId") Long agentId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.customer.id = :customerId")
    long countByCustomerId(@Param("customerId") Long customerId);


    @Query(value = """
        SELECT AVG(DATEDIFF(closed_at, created_at) * 24) 
        FROM ticket 
        WHERE closed_at IS NOT NULL 
        AND created_at > ?1
        """, nativeQuery = true)
    Double getAverageResolutionTimeInHours(@Param("since") LocalDateTime since);


    @Query("""
        SELECT t.priority.name, COUNT(t) FROM Ticket t 
        GROUP BY t.priority.name, t.priority.level 
        ORDER BY t.priority.level DESC
        """)
    List<Object[]> getTicketCountByPriority();

    @Query("""
        SELECT t.status.name, COUNT(t) FROM Ticket t 
        GROUP BY t.status.name, t.status.id 
        ORDER BY t.status.id
        """)
    List<Object[]> getTicketCountByStatus();



    @Query(value = """
        SELECT DATE(created_at), COUNT(*) FROM ticket 
        WHERE created_at > :since 
        GROUP BY DATE(created_at) 
        ORDER BY DATE(created_at) DESC
        """, nativeQuery = true)
    List<Object[]> getDailyTicketCreationStats(@Param("since") LocalDateTime since);
}