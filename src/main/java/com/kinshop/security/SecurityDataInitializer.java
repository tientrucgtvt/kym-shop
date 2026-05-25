package com.kinshop.security;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityDataInitializer {

    @Bean
    ApplicationRunner seedAdminUser(
            AppUserRepository appUserRepository,
            AccessRightRepository accessRightRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            AppUser admin = appUserRepository.findByUsername("admin").orElseGet(() -> {
                AppUser user = new AppUser();
                user.setUsername("admin");
                user.setFullName("Administrator");
                user.setPassword(passwordEncoder.encode("admin123"));
                user.setActive(true);
                return appUserRepository.save(user);
            });

            for (AppPage page : AppPage.values()) {
                accessRightRepository.findByAppUserAndPageKey(admin, page.name()).orElseGet(() -> {
                    AccessRight right = new AccessRight();
                    right.setAppUser(admin);
                    right.setPageKey(page.name());
                    right.setCanView(true);
                    right.setCanCreate(true);
                    right.setCanUpdate(true);
                    right.setCanDelete(true);
                    right.setCanCancel(true);
                    right.setCanReturn(true);
                    right.setCanStock(true);
                    return accessRightRepository.save(right);
                });
            }
        };
    }
}
