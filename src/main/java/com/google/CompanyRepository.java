package com.google;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    // Здесь уже "из коробки" есть методы save(), findAll(), findById(), deleteById()
}