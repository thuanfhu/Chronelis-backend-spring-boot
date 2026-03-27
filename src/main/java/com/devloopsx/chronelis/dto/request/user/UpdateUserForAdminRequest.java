package com.devloopsx.chronelis.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateUserForAdminRequest {
	@Size(min = 2, max = 50, message = "FIRSTNAME_INVALID_LENGTH")
	String firstName;

	@Size(min = 2, max = 50, message = "LASTNAME_INVALID_LENGTH")
	String lastName;

	@Email(message = "EMAIL_INVALID")
	@Pattern(regexp = "^[\\w._%+-]+@(gmail\\.com|yopmail\\.com)$", message = "EMAIL_PROVIDER_INVALID")
	String email;

	@Pattern(regexp = "^(\\+84|0)(3[2-9]|5[6|8|9]|7[0|6-9]|8[1-9]|9[0-9])[0-9]{7}$", message = "PHONE_NUMBER_VN_INVALID")
	String phoneNumber;

	@Size(min = 2, max = 50, message = "NICKNAME_INVALID_LENGTH")
	String nickname;

	String avatarUrl;
	String biography;
	String city;
	String nationality;

	Boolean isVerified;
	List<String> roleIds;
}
