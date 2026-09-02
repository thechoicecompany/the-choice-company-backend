package com.thechoicecompany.repository;

import com.thechoicecompany.entity.SampleProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SampleProductImageRepository extends JpaRepository<SampleProductImage, Long> {

    List<SampleProductImage> findBySampleProductIdOrderBySortOrderAsc(Long sampleProductId);

    Optional<SampleProductImage> findByIdAndSampleProductId(Long id, Long sampleProductId);

    void deleteBySampleProductId(Long sampleProductId);
}