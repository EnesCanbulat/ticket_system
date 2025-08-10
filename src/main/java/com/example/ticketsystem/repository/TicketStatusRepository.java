package com.example.ticketsystem.repository;

import com.example.ticketsystem.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface TicketStatusRepository extends JpaRepository<TicketStatus, Long> {


    Optional<TicketStatus> findByName(String name);
    boolean existsByName(String name);


    @Query("""
        SELECT ts FROM TicketStatus ts 
        WHERE ts.name IN ('Açık', 'Atanmış', 'İşlemde', 'Beklemede') 
        ORDER BY ts.id
        """)
    List<TicketStatus> findActiveStatuses();

    @Query("""
        SELECT ts FROM TicketStatus ts 
        WHERE ts.name IN ('Çözüldü', 'Kapalı') 
        ORDER BY ts.id
        """)
    List<TicketStatus> findClosedStatuses();


    @Query("SELECT ts FROM TicketStatus ts WHERE ts.name = 'Açık'")
    Optional<TicketStatus> findOpenStatus();

    @Query("SELECT ts FROM TicketStatus ts WHERE ts.name = 'Atanmış'")
    Optional<TicketStatus> findAssignedStatus();

    @Query("SELECT ts FROM TicketStatus ts WHERE ts.name = 'İşlemde'")
    Optional<TicketStatus> findInProgressStatus();

    @Query("SELECT ts FROM TicketStatus ts WHERE ts.name = 'Beklemede'")
    Optional<TicketStatus> findWaitingStatus();

    @Query("SELECT ts FROM TicketStatus ts WHERE ts.name = 'Çözüldü'")
    Optional<TicketStatus> findResolvedStatus();

    @Query("SELECT ts FROM TicketStatus ts WHERE ts.name = 'Kapalı'")
    Optional<TicketStatus> findClosedStatus();


    @Query("""
        SELECT ts.name, COUNT(t) FROM TicketStatus ts 
        LEFT JOIN Ticket t ON t.status = ts 
        GROUP BY ts.id, ts.name 
        ORDER BY ts.id
        """)
    List<Object[]> getStatusUsageStatistics();
}