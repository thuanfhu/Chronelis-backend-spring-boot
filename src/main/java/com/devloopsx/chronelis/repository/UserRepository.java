package com.devloopsx.chronelis.repository;

import com.devloopsx.chronelis.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
	Optional<User> findByEmail(String email);

	Optional<User> findByEmailIgnoreCase(String email);

	Optional<User> findByPhoneNumber(String phoneNumber);

	boolean existsByEmail(String email);

	boolean existsByPhoneNumber(String phoneNumber);

	List<User> findByIsVerifiedFalse();

	@Query("SELECT u.userId FROM User u WHERE u.email = :email")
	Optional<String> findUserIdByEmail(@Param("email") String email);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Transactional
	@Query("UPDATE User u SET u.refreshToken = :refreshToken WHERE u.userId = :userId")
	int updateRefreshTokenByUserId(@Param("userId") String userId, @Param("refreshToken") String refreshToken);

	// Dashboard queries
	Long countByCreatedAtBetween(Instant start, Instant end);
}
