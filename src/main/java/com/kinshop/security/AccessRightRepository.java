package com.kinshop.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccessRightRepository extends JpaRepository<AccessRight, Long> {

    Optional<AccessRight> findByAppUserAndPageKey(AppUser appUser, String pageKey);

    List<AccessRight> findByAppUserOrderByPageKeyAsc(AppUser appUser);
}
