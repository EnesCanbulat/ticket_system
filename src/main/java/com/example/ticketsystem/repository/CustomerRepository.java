package com.example.ticketsystem.repository;

import com.example.ticketsystem.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Customer Repository - JDK 24 Compatible
 * Fixed version without circular dependency and orderable issues
 * Enhanced with modern query methods and performance optimizations
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Basic queries
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Customer> findByNameContainingIgnoreCase(String name);

    // Advanced queries with modern JPA features
    @Query("""
        SELECT c FROM Customer c 
        WHERE c.email = :email 
        AND c.createdAt > :since
        """)
    Optional<Customer> findByEmailAndCreatedAfter(@Param("email") String email,
                                                  @Param("since") LocalDateTime since);

    // Customer with details (without tickets join)
    @Query("""
        SELECT c FROM Customer c 
        WHERE c.id = :customerId
        """)
    Optional<Customer> findByIdWithDetails(@Param("customerId") Long customerId);

    // Search customers
    @Query("""
  SELECT c FROM Customer c
  WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
     OR LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
  ORDER BY c.createdAt DESC
""")
    List<Customer> searchCustomers(@Param("searchTerm") String searchTerm);


    // Date range queries
    @Query("""
        SELECT c FROM Customer c 
        WHERE c.createdAt BETWEEN :startDate AND :endDate
        ORDER BY c.createdAt DESC
        """)
    List<Customer> findCustomersCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    // Statistics queries - FIXED
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt > :since")
    long countCustomersCreatedAfter(@Param("since") LocalDateTime since);

    // Active customers count (customers with tickets)
    @Query("""
        SELECT COUNT(DISTINCT t.customer.id) FROM Ticket t 
        WHERE t.createdAt > :since
        """)
    long countActiveCustomersAfter(@Param("since") LocalDateTime since);

    // Email domain queries
    @Query("""
        SELECT c FROM Customer c 
        WHERE c.email LIKE %:domain%
        ORDER BY c.createdAt DESC
        """)
    List<Customer> findByEmailDomain(@Param("domain") String domain);

    // Phone queries
    @Query("""
        SELECT c FROM Customer c 
        WHERE c.phone IS NOT NULL 
        AND c.phone != ''
        ORDER BY c.name ASC
        """)
    List<Customer> findCustomersWithPhone();

    @Query("""
        SELECT c FROM Customer c 
        WHERE c.phone IS NULL 
        OR c.phone = ''
        ORDER BY c.createdAt DESC
        """)
    List<Customer> findCustomersWithoutPhone();

    // VIP customer queries (customers with many tickets) - FIXED
    @Query("""
        SELECT t.customer FROM Ticket t 
        GROUP BY t.customer.id, t.customer.name, t.customer.email, t.customer.phone, t.customer.createdAt, t.customer.updatedAt
        HAVING COUNT(t) >= :minTicketCount
        ORDER BY COUNT(t) DESC
        """)
    List<Customer> findVipCustomers(@Param("minTicketCount") long minTicketCount);

    // Recent active customers - FIXED
    @Query("""
        SELECT DISTINCT t.customer FROM Ticket t 
        WHERE t.createdAt > :since
        ORDER BY t.customer.createdAt DESC
        """)
    List<Customer> findRecentActiveCustomers(@Param("since") LocalDateTime since);

    // Customers without any tickets
    @Query("""
        SELECT c FROM Customer c 
        WHERE c.id NOT IN (
            SELECT DISTINCT t.customer.id FROM Ticket t
        )
        ORDER BY c.createdAt DESC
        """)
    List<Customer> findCustomersWithoutTickets();

    // Satisfied customers - FIXED
    @Query("""
        SELECT DISTINCT t.customer FROM Ticket t 
        WHERE t.status.name = 'Çözüldü'
        AND t.closedAt > :since
        ORDER BY t.customer.createdAt DESC
        """)
    List<Customer> findSatisfiedCustomers(@Param("since") LocalDateTime since);

    // Customers with many open tickets - FIXED
    @Query("""
        SELECT t.customer, COUNT(t) FROM Ticket t 
        WHERE t.status.name IN ('Açık', 'Atanmış', 'İşlemde', 'Beklemede')
        GROUP BY t.customer.id, t.customer.name, t.customer.email, t.customer.phone, t.customer.createdAt, t.customer.updatedAt
        HAVING COUNT(t) > :threshold
        ORDER BY COUNT(t) DESC
        """)
    List<Object[]> findCustomersWithManyOpenTickets(@Param("threshold") long threshold);

    // Modern derived query methods
    List<Customer> findTop10ByOrderByCreatedAtDesc();
    List<Customer> findByCreatedAtAfter(LocalDateTime date);
    List<Customer> findByEmailContainingIgnoreCase(String emailPart);
    List<Customer> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name);

    // Phone number queries
    List<Customer> findByPhoneContaining(String phoneDigits);
    List<Customer> findByPhoneStartingWith(String phonePrefix);

    // Advanced search with multiple criteria
    @Query("""
        SELECT c FROM Customer c 
        WHERE (:name IS NULL OR c.name ILIKE %:name%) 
        AND (:email IS NULL OR c.email ILIKE %:email%)
        AND (:phone IS NULL OR c.phone LIKE %:phone%)
        AND (:createdAfter IS NULL OR c.createdAt > :createdAfter)
        ORDER BY c.createdAt DESC
        """)
    List<Customer> findByMultipleCriteria(
            @Param("name") String name,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("createdAfter") LocalDateTime createdAfter
    );

    // Customer engagement statistics - SIMPLIFIED
    @Query("""
        SELECT COUNT(c) FROM Customer c
        """)
    long getTotalCustomerCount();

    @Query("""
        SELECT COUNT(DISTINCT t.customer.id) FROM Ticket t 
        WHERE t.createdAt > :since
        """)
    long getActiveCustomerCount(@Param("since") LocalDateTime since);

    // Customer lifetime value - FIXED
    @Query("""
        SELECT t.customer, COUNT(t), MIN(t.createdAt), MAX(t.createdAt)
        FROM Ticket t 
        GROUP BY t.customer.id, t.customer.name, t.customer.email, t.customer.phone, t.customer.createdAt, t.customer.updatedAt
        ORDER BY COUNT(t) DESC
        """)
    List<Object[]> getCustomerLifetimeStats();

    // Recent customer activity - SIMPLIFIED
    @Query("""
        SELECT c FROM Customer c 
        WHERE c.updatedAt > :since 
        ORDER BY c.updatedAt DESC
        """)
    List<Customer> findRecentlyActiveCustomers(@Param("since") LocalDateTime since);

    // Bulk operations support
    @Query("SELECT c FROM Customer c WHERE c.id IN :customerIds")
    List<Customer> findByIdIn(@Param("customerIds") List<Long> customerIds);

    // Customer segmentation queries - FIXED
    @Query("""
        SELECT c FROM Customer c 
        WHERE c.id IN (
            SELECT t.customer.id FROM Ticket t 
            GROUP BY t.customer.id 
            HAVING COUNT(t) BETWEEN :minTickets AND :maxTickets
        )
        ORDER BY c.name ASC
        """)
    List<Customer> findCustomersByTicketRange(@Param("minTickets") long minTickets,
                                              @Param("maxTickets") long maxTickets);

    // Geographic queries
    @Query("""
        SELECT c FROM Customer c 
        WHERE c.phone LIKE :phonePattern
        ORDER BY c.createdAt DESC
        """)
    List<Customer> findCustomersByPhonePattern(@Param("phonePattern") String phonePattern);

    // Customer retention queries - FIXED
    @Query("""
        SELECT c FROM Customer c 
        WHERE c.createdAt < :oldDate 
        AND c.id IN (
            SELECT DISTINCT t.customer.id FROM Ticket t 
            WHERE t.createdAt > :recentDate
        )
        ORDER BY c.createdAt ASC
        """)
    List<Customer> findRetainedCustomers(@Param("oldDate") LocalDateTime oldDate,
                                         @Param("recentDate") LocalDateTime recentDate);

    // Performance-optimized count queries
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt > :date")
    long countNewCustomersSince(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.email LIKE %:domain%")
    long countCustomersByEmailDomain(@Param("domain") String domain);

    @Query("""
        SELECT COUNT(DISTINCT t.customer.id) FROM Ticket t 
        WHERE t.status.name IN ('Çözüldü', 'Kapalı')
        """)
    long countCustomersWithResolvedTickets();

    // Daily/Monthly statistics - FIXED for MySQL compatibility
    @Query(value = """
        SELECT DATE(c.created_at) as registration_date, COUNT(c.id) as customer_count 
        FROM customers c 
        WHERE c.created_at > :since 
        GROUP BY DATE(c.created_at) 
        ORDER BY DATE(c.created_at) DESC
        """, nativeQuery = true)
    List<Object[]> getDailyCustomerRegistrationStats(@Param("since") LocalDateTime since);

    @Query(value = """
        SELECT YEAR(c.created_at) as year, 
               MONTH(c.created_at) as month, 
               COUNT(c.id) as customer_count 
        FROM customers c 
        WHERE c.created_at > :since 
        GROUP BY YEAR(c.created_at), MONTH(c.created_at)
        ORDER BY year DESC, month DESC
        """, nativeQuery = true)
    List<Object[]> getMonthlyCustomerRegistrationStats(@Param("since") LocalDateTime since);

    // Simple aggregation queries
    @Query("SELECT COUNT(c) FROM Customer c")
    long countAllCustomers();

    @Query("SELECT MIN(c.createdAt) FROM Customer c")
    LocalDateTime findEarliestCustomerDate();

    @Query("SELECT MAX(c.createdAt) FROM Customer c")
    LocalDateTime findLatestCustomerDate();

    // Email verification queries
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.email = :email")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    // Customer activity summary
    @Query("""
        SELECT c, 
               (SELECT COUNT(t) FROM Ticket t WHERE t.customer.id = c.id) as ticketCount
        FROM Customer c 
        WHERE c.id = :customerId
        """)
    Object[] getCustomerSummary(@Param("customerId") Long customerId);

    // Recently registered customers
    @Query("""
        SELECT c FROM Customer c 
        WHERE c.createdAt > :since
        ORDER BY c.createdAt DESC
        """)
    List<Customer> findRecentlyRegisteredCustomers(@Param("since") LocalDateTime since);

    // Customer search by partial match
    @Query("""
        SELECT c FROM Customer c 
        WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        ORDER BY c.name ASC
        """)
    List<Customer> findByNameOrEmailContaining(@Param("searchTerm") String searchTerm);
}