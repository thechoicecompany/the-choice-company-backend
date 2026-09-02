package com.thechoicecompany.repository;

import com.thechoicecompany.entity.CatalogueRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CatalogueRequestRepository extends JpaRepository<CatalogueRequest, Long> {

    Page<CatalogueRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    boolean existsByEmailAndCreatedAtAfter(String email, LocalDateTime since);

    Long countByCreatedAtAfter(LocalDateTime since);

    Long countBySource(String source);

    // Source breakdown for dashboard chart
    @Query("SELECT c.source, COUNT(c) FROM CatalogueRequest c GROUP BY c.source ORDER BY COUNT(c) DESC")
    List<Object[]> countBySourceGrouped();

    // Monthly trend data
    @Query(value = """
        SELECT DATE_TRUNC('month', created_at) as month, COUNT(*) as total
        FROM catalogue_requests
        WHERE created_at >= :since
        GROUP BY DATE_TRUNC('month', created_at)
        ORDER BY month ASC
        """, nativeQuery = true)
    List<Object[]> monthlyTrend(@Param("since") LocalDateTime since);
}
