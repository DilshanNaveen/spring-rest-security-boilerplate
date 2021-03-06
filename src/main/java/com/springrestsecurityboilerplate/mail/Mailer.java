package com.springrestsecurityboilerplate.mail;

import java.io.Serializable;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.springrestsecurityboilerplate.password.PasswordResetToken;
import com.springrestsecurityboilerplate.registration.OnRegistrationCompleteEvent;
import com.springrestsecurityboilerplate.registration.RegistrationToken;
import com.springrestsecurityboilerplate.registration.ResendToken;
import com.springrestsecurityboilerplate.registration.VerificationToken;
import com.springrestsecurityboilerplate.user.AppUser;

import static com.springrestsecurityboilerplate.SpringRestSecurityBoilerplateApplication.SEND_MAIL;

public class Mailer implements Serializable {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private MessageSource messages;

	@Autowired
	private Environment env;

	@RabbitListener(queues = "#{resendTokenMailQueue.name}")
	public void onResendVerificationToken(ResendToken resendToken) {
		// System.out.println("onResendVerificationToken is executed");
		if (resendToken != null)
			resendVerificationToken(resendToken.getUser(), resendToken.getOldToken());
	}

	public void resendVerificationToken(AppUser user, VerificationToken token) {

		final SimpleMailMessage email = constructResendVerificationTokenEmail(user, token);
		System.out.println("New token is : " + token.getToken());
		if (SEND_MAIL == true)
			mailSender.send(email);

		System.out.println(email);

	}

	private final SimpleMailMessage constructResendVerificationTokenEmail(final AppUser user,
			final VerificationToken newToken) {

		SimpleMailMessage email = new SimpleMailMessage();
		email.setTo(user.getEmail());
		email.setSubject("Resend Registration Token");
		email.setText("New token is " + newToken.getToken());
		email.setFrom(env.getProperty("support.email"));
		return email;

	}

	@RabbitListener(queues = "#{registrationTokenMailQueue.name}")
	public void onRegistrationToken(RegistrationToken registrationToken) throws InterruptedException {
		// System.out.println("onRegistrationToken is executed");
		registrationTokenEmail(registrationToken.getEvent(), registrationToken.getUser(), registrationToken.getToken());
	}

	public void registrationTokenEmail(OnRegistrationCompleteEvent event, AppUser user, String token) {

		final SimpleMailMessage email = constructRegistrationEmailMessage(event, user, token);

		if (SEND_MAIL == true)
			mailSender.send(email);

		System.out.println(email);

	}

	private final SimpleMailMessage constructRegistrationEmailMessage(final OnRegistrationCompleteEvent event,
			final AppUser user, final String token) {

		final String recipientAddress = user.getEmail();
		final String subject = "Registration Confirmation";
		// final String confirmationUrl = event.getAppUrl() +
		// "/registrationConfirm.html?token=" + token;
		// final String message = messages.getMessage("message.regSucc", null,
		// event.getLocale());
		final SimpleMailMessage email = new SimpleMailMessage();
		email.setTo(recipientAddress);
		email.setSubject(subject);
		email.setText("Token is " + token); // just sending token
		email.setFrom(env.getProperty("support.email"));
		return email;
	}

	@RabbitListener(queues = "#{resetPasswordTokenMailQueue.name}")
	public void onResetPasswordToken(PasswordResetToken passwordResetToken) throws InterruptedException {
		// System.out.println("onRegistrationToken is executed");
		resetPasswordTokenEmail(passwordResetToken.getUser(), passwordResetToken.getToken());
	}

	public void resetPasswordTokenEmail(AppUser user, String resetPasswordToken) {

		final SimpleMailMessage email = constructResetTokenEmail(user, resetPasswordToken);

		if (SEND_MAIL == true)
			mailSender.send(email);

	}

	private final SimpleMailMessage constructResetTokenEmail(final AppUser user, final String passwordToken) {

		SimpleMailMessage email = new SimpleMailMessage();
		email.setTo(user.getEmail());
		email.setSubject("Reset Password Token");
		email.setText("New token is " + passwordToken);
		email.setFrom(env.getProperty("support.email"));
		return email;

	}

}
