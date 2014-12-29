package org.apache.isis.core.runtime.services.email;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.email.EmailNotificationService;
import org.apache.isis.applib.services.email.events.EmailRegistrationEvent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.context.IsisContext;

/**
 * TODO ISIS-987 Javadoc
 */
@DomainService
public class EmailNotificationServiceDefault implements EmailNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationServiceDefault.class);

    @Override
    public boolean send(EmailRegistrationEvent emailRegistrationEvent) {

        boolean mailSent = true;
        try {
            String senderEmailAddress = getSenderEmailAddress();
            String senderEmailPasswd  = getSenderEmailPassword();

            Email email = new HtmlEmail();
            email.setAuthenticator(new DefaultAuthenticator(senderEmailAddress, senderEmailPasswd));
            email.setHostName(getSenderEmailHostName());
            email.setSmtpPort(getSenderEmailPort());
            email.setStartTLSEnabled(getSenderEmailTlsEnabled());
            Properties properties = email.getMailSession().getProperties();
            // TODO mgrigorov: check whether all these are required and extract as configuration settings
            properties.put("mail.smtps.auth", "true");
            properties.put("mail.debug", "true");
            properties.put("mail.smtps.port", "587");
            properties.put("mail.smtps.socketFactory.port", "587");
            properties.put("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtps.socketFactory.fallback", "false");
            properties.put("mail.smtp.starttls.enable", "true");

            email.setFrom(senderEmailAddress);

            email.setSubject("SignUp verification");
            email.setMsg(loadMessage(emailRegistrationEvent));

            // TODO ISIS-987 use the provided email once development/testing is done
            email.addTo("martin.grigorov@gmail.com");
            email.send();

        } catch (EmailException ex) {
            LOGGER.error("An error occurred while trying to send an email about user email verification", ex);
            mailSent = false;
        }

        return mailSent;
    }

    private String loadMessage(EmailRegistrationEvent emailRegistrationEvent) {
        String message;
        try {
            URL emailVerificationTemplateUrl = Resources.getResource(EmailNotificationServiceDefault.class, "EmailVerificationTemplate.html");
            String emailVerificationTemplate = Resources.toString(emailVerificationTemplateUrl, Charsets.UTF_8);
            message = emailVerificationTemplate.replace("${email}", emailRegistrationEvent.getEmail());
            message = message.replace("${confirmationUrl}", emailRegistrationEvent.getConfirmationUrl());
        } catch (IOException e) {
            e.printStackTrace();
            message = "Problem: " + e.getMessage();
        }

        return message;
    }

    protected String getSenderEmailAddress() {
        return getConfiguration().getString("isis.notification.email.sender.address");
    }

    protected String getSenderEmailPassword() {
        return getConfiguration().getString("isis.notification.email.sender.password");
    }

    protected String getSenderEmailHostName() {
        return getConfiguration().getString("isis.notification.email.sender.hostname", "smtp.gmail.com");
    }

    protected Integer getSenderEmailPort() {
        return getConfiguration().getInteger("isis.notification.email.port", 587);
    }

    protected Boolean getSenderEmailTlsEnabled() {
        return getConfiguration().getBoolean("isis.notification.email.tls.enabled", true);
    }

    private IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }
}
