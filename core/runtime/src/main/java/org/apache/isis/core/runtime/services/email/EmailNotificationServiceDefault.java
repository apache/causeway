package org.apache.isis.core.runtime.services.email;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Pattern;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.inject.name.Named;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.services.email.EmailNotificationService;
import org.apache.isis.applib.services.email.events.EmailRegistrationEvent;
import org.apache.isis.applib.services.email.events.PasswordResetEvent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.context.IsisContext;

import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;

/**
 * A service that sends email notifications when specific events occur
 */
@DomainService
public class EmailNotificationServiceDefault implements EmailNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationServiceDefault.class);

    private static final Pattern EMAIL_PATTERN = compile(quote("${email}"));
    private static final Pattern CONFIRMATION_URL_PATTERN = compile(quote("${confirmationUrl}"));
    private static final Pattern APPLICATION_NAME_PATTERN = compile(quote("${applicationName}"));

    private final String passwordResetTemplate;
    private final String emailVerificationTemplate;

    /**
     * Constructor.
     *
     * Loads responsive email templates borrowed from http://zurb.com/ink/templates.php (Basic)
     * @throws IOException
     */
    public EmailNotificationServiceDefault() throws IOException {
        URL passwordResetTemplateUrl = Resources.getResource(EmailNotificationServiceDefault.class, "PasswordResetTemplate.html");
        this.passwordResetTemplate = Resources.toString(passwordResetTemplateUrl, Charsets.UTF_8);

        URL emailVerificationTemplateUrl = Resources.getResource(EmailNotificationServiceDefault.class, "EmailVerificationTemplate.html");
        this.emailVerificationTemplate = Resources.toString(emailVerificationTemplateUrl, Charsets.UTF_8);
    }

    @Override
    public boolean send(EmailRegistrationEvent emailRegistrationEvent) {
        return send(calculateSubject(emailRegistrationEvent), loadMessage(emailRegistrationEvent));
    }

    @Override
    public boolean send(PasswordResetEvent passwordResetEvent) {
        return send(calculateSubject(passwordResetEvent), loadMessage(passwordResetEvent));
    }

    protected boolean send(String subject, String body) {

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

            email.setSubject(subject);
            email.setMsg(body);

            // TODO ISIS-987 use the provided email once development/testing is done
            email.addTo("martin.grigorov@gmail.com");
            email.send();

        } catch (EmailException ex) {
            LOGGER.error("An error occurred while trying to send an email about user email verification", ex);
            mailSent = false;
        }

        return mailSent;
    }

    protected String loadMessage(EmailRegistrationEvent emailRegistrationEvent) {
        String message = EMAIL_PATTERN.matcher(emailVerificationTemplate).replaceFirst(emailRegistrationEvent.getEmail());
        message = CONFIRMATION_URL_PATTERN.matcher(message).replaceFirst(emailRegistrationEvent.getConfirmationUrl());
        message = APPLICATION_NAME_PATTERN.matcher(message).replaceAll(applicationName);
        return message;
    }

    protected String loadMessage(PasswordResetEvent passwordResetEvent) {
        String message = EMAIL_PATTERN.matcher(passwordResetTemplate).replaceFirst(passwordResetEvent.getEmail());
        message = CONFIRMATION_URL_PATTERN.matcher(message).replaceFirst(passwordResetEvent.getConfirmationUrl());
        message = APPLICATION_NAME_PATTERN.matcher(message).replaceAll(applicationName);
        return message;
    }

    protected String calculateSubject(Object event) {
        String subject;
        if (event instanceof EmailRegistrationEvent) {
            subject = "["+applicationName+"] Please confirm your identity";
        } else if (event instanceof PasswordResetEvent) {
            subject = "["+applicationName+"] Password reset request";
        } else {
            subject = "[" + applicationName + "]";
        }
        return subject;
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

    @com.google.inject.Inject
    @Named("applicationName")
    protected String applicationName;
}
