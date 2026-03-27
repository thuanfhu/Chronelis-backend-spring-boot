package com.devloopsx.chronelis.configuration;

import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScheduledTasks {
	UserRepository userRepository;
	private static final long UNVERIFIED_USER_TIMEOUT_DAYS = 2;

	@Scheduled(cron = "0 0 0 * * *")
	@Transactional
	public void cleanupUnverifiedUsers() {
		try {
			List<User> unverifiedUsers = userRepository.findByIsVerifiedFalse();

			for (User user : unverifiedUsers) {
				Instant now = Instant.now();
				Instant createdAt = user.getCreatedAt();

				if (createdAt != null && now.isAfter(createdAt.plus(UNVERIFIED_USER_TIMEOUT_DAYS, ChronoUnit.DAYS))) {
					user.getRoles().clear();
					userRepository.delete(user);
				}
			}
		} catch (Exception e) {
			log.error("Failed to cleanup unverified users: {}", e.getMessage());
		}
	}
}
