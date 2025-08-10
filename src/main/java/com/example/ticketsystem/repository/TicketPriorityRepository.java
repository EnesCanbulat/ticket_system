package com.example.ticketsystem.repository;

import com.example.ticketsystem.entity.TicketPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TicketPriorityRepository extends JpaRepository<TicketPriority, Long> {


    Optional<TicketPriority> findByName(String name);
    Optional<TicketPriority> findByLevel(Integer level);
    boolean existsByName(String name);
    boolean existsByLevel(Integer level);


    List<TicketPriority> findAllByOrderByLevelAsc();
    List<TicketPriority> findAllByOrderByLevelDesc();


    @Query("""
        SELECT tp FROM TicketPriority tp 
        WHERE tp.level BETWEEN :minLevel AND :maxLevel 
        ORDER BY tp.level ASC
        """)
    List<TicketPriority> findByLevelRange(@Param("minLevel") Integer minLevel,
                                          @Param("maxLevel") Integer maxLevel);

    @Query("""
        SELECT tp FROM TicketPriority tp 
        WHERE tp.level >= :minLevel 
        ORDER BY tp.level ASC
        """)
    List<TicketPriority> findByLevelGreaterThanEqual(@Param("minLevel") Integer minLevel);


    @Query("SELECT tp FROM TicketPriority tp WHERE tp.name = 'Düşük'")
    Optional<TicketPriority> findLowPriority();

    @Query("SELECT tp FROM TicketPriority tp WHERE tp.name = 'Normal'")
    Optional<TicketPriority> findNormalPriority();

    @Query("SELECT tp FROM TicketPriority tp WHERE tp.name = 'Yüksek'")
    Optional<TicketPriority> findHighPriority();

    @Query("SELECT tp FROM TicketPriority tp WHERE tp.name = 'Acil'")
    Optional<TicketPriority> findUrgentPriority();

    @Query("SELECT tp FROM TicketPriority tp WHERE tp.name = 'Kritik'")
    Optional<TicketPriority> findCriticalPriority();


    Optional<TicketPriority> findFirstByOrderByLevelAsc();
    Optional<TicketPriority> findFirstByOrderByLevelDesc();



    default Optional<TicketPriority> findLowestPriority() {
        return findFirstByOrderByLevelAsc();
    }

    default Optional<TicketPriority> findHighestPriority() {
        return findFirstByOrderByLevelDesc();
    }


    @Query("""
        SELECT tp.name, tp.level, COUNT(t) FROM TicketPriority tp 
        LEFT JOIN Ticket t ON t.priority = tp 
        GROUP BY tp.id, tp.name, tp.level 
        ORDER BY tp.level DESC
        """)
    List<Object[]> getPriorityUsageStatistics();
}
