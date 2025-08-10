package com.example.ticketsystem.repository;

import com.example.ticketsystem.entity.TicketMessage;
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
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {


    List<TicketMessage> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
    List<TicketMessage> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
    long countByTicketId(Long ticketId);


    Page<TicketMessage> findByTicketIdOrderByCreatedAtAsc(Long ticketId, Pageable pageable);


    List<TicketMessage> findBySenderTypeAndSenderIdOrderByCreatedAtDesc(
            TicketMessage.SenderType senderType, Long senderId);

    @Query("""
        SELECT tm FROM TicketMessage tm 
        WHERE tm.ticket.id = :ticketId 
        AND tm.senderType = :senderType 
        ORDER BY tm.createdAt DESC
        """)
    List<TicketMessage> findByTicketAndSenderType(@Param("ticketId") Long ticketId,
                                                  @Param("senderType") TicketMessage.SenderType senderType);


    Optional<TicketMessage> findTopByTicketIdOrderByCreatedAtDesc(Long ticketId);





    Optional<TicketMessage> findTopByTicketIdAndSenderTypeOrderByCreatedAtDesc(
            Long ticketId,
            TicketMessage.SenderType senderType
    );


    @Query("""
        SELECT tm FROM TicketMessage tm 
        WHERE tm.ticket.id = :ticketId 
        AND tm.createdAt > :since 
        ORDER BY tm.createdAt ASC
        """)
    List<TicketMessage> findRecentMessagesByTicket(@Param("ticketId") Long ticketId,
                                                   @Param("since") LocalDateTime since);

    @Query("""
        SELECT tm FROM TicketMessage tm 
        WHERE tm.createdAt BETWEEN :startDate AND :endDate 
        ORDER BY tm.createdAt DESC
        """)
    List<TicketMessage> findMessagesCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);


    @Query("""
        SELECT tm FROM TicketMessage tm 
        WHERE tm.ticket.id = :ticketId 
        AND LOWER(tm.message) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        ORDER BY tm.createdAt DESC
        """)
    List<TicketMessage> searchMessagesInTicket(@Param("ticketId") Long ticketId,
                                               @Param("searchTerm") String searchTerm);

    @Query("""
    SELECT tm FROM TicketMessage tm 
    JOIN tm.ticket t 
    WHERE t.customer.id = :customerId 
      AND LOWER(tm.message) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
    ORDER BY tm.createdAt DESC
    """)
    List<TicketMessage> searchCustomerMessages(@Param("customerId") Long customerId,
                                               @Param("searchTerm") String searchTerm);



    @Query("""
        SELECT tm.senderType, COUNT(tm) FROM TicketMessage tm 
        WHERE tm.ticket.id = :ticketId 
        GROUP BY tm.senderType
        """)
    List<Object[]> getMessageCountBySenderType(@Param("ticketId") Long ticketId);

    @Query("""
        SELECT COUNT(tm) FROM TicketMessage tm 
        WHERE tm.senderId = :senderId 
        AND tm.senderType = :senderType 
        AND tm.createdAt > :since
        """)
    long countMessagesBySenderAfter(@Param("senderId") Long senderId,
                                    @Param("senderType") TicketMessage.SenderType senderType,
                                    @Param("since") LocalDateTime since);

    @Query("""
    SELECT FUNCTION('date', tm.createdAt), COUNT(tm)
    FROM TicketMessage tm
    WHERE tm.createdAt > :since
    GROUP BY FUNCTION('date', tm.createdAt)
    ORDER BY FUNCTION('date', tm.createdAt) DESC
    """)
    List<Object[]> getDailyMessageStats(@Param("since") LocalDateTime since);


    // Advanced queries
    @Query("""
        SELECT tm FROM TicketMessage tm 
        WHERE tm.ticket.id IN (
            SELECT t.id FROM Ticket t 
            WHERE t.agent.id = :agentId
        ) 
        ORDER BY tm.createdAt DESC
        """)
    List<TicketMessage> findMessagesByAgentTickets(@Param("agentId") Long agentId);

    @Query("""
        SELECT DISTINCT tm.ticket.id FROM TicketMessage tm 
        WHERE tm.senderType = 'CUSTOMER' 
        AND tm.createdAt > :since 
        AND tm.ticket.id NOT IN (
            SELECT DISTINCT tm2.ticket.id FROM TicketMessage tm2 
            WHERE tm2.senderType = 'AGENT' 
            AND tm2.createdAt > tm.createdAt
        )
        """)
    List<Long> findTicketsWithUnrepliedCustomerMessages(@Param("since") LocalDateTime since);
}