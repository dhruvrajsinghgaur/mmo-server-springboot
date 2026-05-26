package com.mmo.mmo_server;

import jakarta.persistence.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Players, Long> {
    @Column(unique = true)
    Optional<Players> findByUsername(String username);
}
