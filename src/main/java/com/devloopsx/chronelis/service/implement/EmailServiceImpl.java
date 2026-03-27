package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.service.EmailService;
import com.devloopsx.chronelis.utils.SecurityUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailServiceImpl implements EmailService {
	JavaMailSender javaMailSender;
	SpringTemplateEngine templateEngine;
	SecurityUtils securityUtils;

	@NonFinal
	@Value("${chronelis.frontend.base-url}")
	protected String FRONTEND_BASEURL;

	@Override
	public void sendVerifyActiveAccountEmail(User user) {
		sendEmail("verify-email", "Xác thực tài khoản của bạn", user, "verifyEmailUrl",
				FRONTEND_BASEURL + "/auth/verify-active-account");
	}

	@Override
	public void sendVerifyChangeEmail(User user) {
		sendEmail("verify-email", "Xác thực tài khoản của bạn", user, "verifyEmailUrl",
				FRONTEND_BASEURL + "/auth/verify-change-email");
	}

	@Override
	public void sendForgotPasswordEmail(User user) {
		sendEmail("forgot-password", "Đặt lại mật khẩu tài khoản của bạn", user, "resetPasswordUrl",
				FRONTEND_BASEURL + "/auth/reset-password");
	}

	private void sendEmail(String templateName, String subject, User user, String actionUrlVariable, String actionUrl) {
		String token = securityUtils.generateAccessToken(user);

		// Set variables for the email template
		Context context = new Context();
		String fullActionUrl = actionUrl + "?token=" + token;
		context.setVariable(actionUrlVariable, fullActionUrl);
		context.setVariable("name", user.getFirstName() + " " + user.getLastName());
		String content = templateEngine.process(templateName, context);

		sendEmailSync(user.getEmail(), subject, content, false, true);
	}

	private void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
		// Prepare message using a Spring helper
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		try {
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
			message.setTo(to);
			message.setSubject(subject);
			message.setText(content, isHtml);
			javaMailSender.send(mimeMessage);
		} catch (MailException | MessagingException e) {
			System.out.println(ErrorCode.SEND_EMAIL_ERROR.getMessage() + e);
		}
	}
}
