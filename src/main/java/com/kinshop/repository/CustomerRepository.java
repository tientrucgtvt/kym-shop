package com.kinshop.repository;

import com.kinshop.model.Customer;
import com.kinshop.model.CustomerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findAllByOrderByNameAsc();

    List<Customer> findByActiveTrueOrderByNameAsc();

    Optional<Customer> findFirstByTaxNumberIgnoreCase(String taxNumber);

    Optional<Customer> findFirstByPhoneNumber(String phoneNumber);

    Optional<Customer> findFirstByTypeAndNameIgnoreCase(CustomerType type, String name);
}
