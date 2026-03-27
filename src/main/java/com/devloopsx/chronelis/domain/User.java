package com.devloopsx.chronelis.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
public class User extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	String userId;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "role_users", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	@JsonIgnoreProperties(value = { "users" })
	List<Role> roles;

	@Column(unique = true)
	String email;

	String password;
	String firstName;
	String lastName;
	String nickname;

	@Column(unique = true)
	String phoneNumber;

	@Column(columnDefinition = "TEXT")
	String avatarUrl;

	@Column(columnDefinition = "TEXT")
	String biography;

	String city;
	String nationality;

	@Column(columnDefinition = "TEXT")
	String refreshToken;

	@Column(nullable = false)
	@Builder.Default
	Boolean isVerified = false;
}
