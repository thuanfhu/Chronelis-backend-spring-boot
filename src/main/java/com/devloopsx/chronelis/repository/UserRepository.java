package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
	Optional<User> findByEmail(String email);

	Optional<User> findByPhoneNumber(String phoneNumber);

	boolean existsByEmail(String email);

	boolean existsByPhoneNumber(String phoneNumber);

	List<User> findByIsVerifiedFalse();

	// Dashboard queries
	Long countByCreatedAtBetween(Instant start, Instant end);
}
