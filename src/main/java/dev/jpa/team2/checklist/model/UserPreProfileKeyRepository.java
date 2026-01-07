package dev.jpa.team2.checklist.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreProfileKeyRepository extends JpaRepository<UserPreProfileKey, Long> {

  Optional<UserPreProfileKey> findByKeyHash(String keyHash);
}
