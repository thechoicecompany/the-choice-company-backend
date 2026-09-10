package com.thechoicecompany.repository;

import com.thechoicecompany.entity.HeroBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeroBannerRepository extends JpaRepository<HeroBanner, Long> {

    /** Public site: only active banners, ordered by displayOrder ASC */
    List<HeroBanner> findByActiveTrueOrderByDisplayOrderAsc();

    /** Admin: all banners, ordered by displayOrder ASC */
    List<HeroBanner> findAllByOrderByDisplayOrderAsc();
}