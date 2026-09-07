package com.thechoicecompany.repository;

import com.thechoicecompany.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<BlogPost, Long> {

    // ── Public (published only) ──────────────────────────────────────────
    Page<BlogPost> findByIsPublishedTrue(Pageable pageable);
    Page<BlogPost> findByCategoryAndIsPublishedTrue(String category, Pageable pageable);
    Optional<BlogPost> findBySlugAndIsPublishedTrue(String slug);

    @Query("select b.slug from BlogPost b where b.isPublished = true")
    List<String> findAllPublishedSlugs();

    @Query("select distinct b.category from BlogPost b where b.isPublished = true order by b.category")
    List<String> findDistinctCategories();

    // ── Admin (all statuses) ─────────────────────────────────────────────
    Page<BlogPost> findByIsPublishedFalse(Pageable pageable);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
}