package com.kinshop.repository;

import com.kinshop.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    List<Brand> findAllByOrderByNameAsc();

    List<Brand> findByActiveTrueOrderByNameAsc();
}
