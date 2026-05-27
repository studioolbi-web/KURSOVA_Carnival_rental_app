package com.oliinyk.costumes.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * Реалізація сервісу EmailService, яка відправляє реальні листи через SMTP.
 * Використовує асинхронний виклик для запобігання блокуванню UI.
 */
public class SmtpEmailServiceImpl implements EmailService {

    private Properties mailProps;
    private String username;
    private String password;
    private String fromEmail;

    public SmtpEmailServiceImpl() {
        loadConfig();
    }

    private void loadConfig() {
        mailProps = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.err.println("Sorry, unable to find application.properties");
                return;
            }
            Properties appProps = new Properties();
            appProps.load(input);

            mailProps.put("mail.smtp.host", appProps.getProperty("mail.smtp.host"));
            mailProps.put("mail.smtp.port", appProps.getProperty("mail.smtp.port"));
            mailProps.put("mail.smtp.auth", appProps.getProperty("mail.smtp.auth"));
            mailProps.put("mail.smtp.starttls.enable", appProps.getProperty("mail.smtp.starttls.enable"));

            this.username = appProps.getProperty("mail.smtp.username");
            this.password = appProps.getProperty("mail.smtp.password");
            this.fromEmail = appProps.getProperty("mail.from", "noreply@carnivalrental.com");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void sendVerificationEmail(String email, String verificationCode) {
        CompletableFuture.runAsync(() -> {
            Session session = Session.getInstance(mailProps, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(fromEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                message.setSubject("Підтвердження реєстрації - Carnival Rental");

                String htmlContent = "<h2>Ласкаво просимо!</h2>"
                        + "<p>Ваш код підтвердження реєстрації:</p>"
                        + "<h1 style='color: #4CAF50; letter-spacing: 5px;'>" + verificationCode + "</h1>"
                        + "<p>Будь ласка, введіть цей код у додатку для завершення реєстрації.</p>";

                message.setContent(htmlContent, "text/html; charset=utf-8");

                Transport.send(message);
                System.out.println("Email sent successfully to " + email);

            } catch (MessagingException e) {
                System.err.println("Failed to send email to " + email);
                e.printStackTrace();
            }
        });
    }
}
