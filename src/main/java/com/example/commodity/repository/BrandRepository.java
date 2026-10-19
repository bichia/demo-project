package com.example.commodity.repository;

import com.example.commodity.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    List<Brand> findAllByOrderBySortOrderAscIdAsc();

    Optional<Brand> findByName(String name);
}
