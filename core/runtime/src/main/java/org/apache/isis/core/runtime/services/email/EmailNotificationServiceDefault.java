package org.apache.isis.core.runtime.services.email;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.email.EmailNotificationService;
import org.apache.isis.applib.services.email.events.EmailEventAbstract;
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

    private static final Logger LOG = LoggerFactory.getLogger(EmailNotificationServiceDefault.class);

    private static final Pattern EMAIL_PATTERN = compile(quote("${email}"));
    private static final Pattern CONFIRMATION_URL_PATTERN = compile(quote("${confirmationUrl}"));
    private static final Pattern APPLICATION_NAME_PATTERN = compile(quote("${applicationName}"));

    private static final String ISIS_NOTIFICATION_EMAIL_SENDER_ADDRESS = "isis.notification.email.sender.address";
    private static final String ISIS_NOTIFICATION_EMAIL_SENDER_PASSWORD = "isis.notification.email.sender.password";
    private static final String ISIS_NOTIFICATION_EMAIL_SENDER_HOSTNAME = "isis.notification.email.sender.hostname";
    private static final String ISIS_NOTIFICATION_EMAIL_SENDER_HOSTNAME_DEFAULT = "smtp.gmail.com";
    private static final String ISIS_NOTIFICATION_EMAIL_PORT = "isis.notification.email.port";
    private static final String ISIS_NOTIFICATION_EMAIL_TLS_ENABLED = "isis.notification.email.tls.enabled";

    private String passwordResetTemplate;
    private String emailVerificationTemplate;
    private String senderEmailAddress;
    private String senderEmailPassword;
    private Integer senderEmailPort;


    /**
     * Loads responsive email templates borrowed from http://zurb.com/ink/templates.php (Basic)
     */
    @PostConstruct
    @Programmatic
    public void init() {

        emailVerificationTemplate = loadResource("EmailVerificationTemplate.html");
        passwordResetTemplate = loadResource("PasswordResetTemplate.html");

        senderEmailAddress = getSenderEmailAddress();
        senderEmailPassword = getSenderEmailPassword();

        senderEmailPort = getSenderEmailPort();
    }

    protected String loadResource(final String resourceName) {
        final URL templateUrl = Resources.getResource(EmailNotificationServiceDefault.class, resourceName);
        try {
            return Resources.toString(templateUrl, Charsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(String.format("Unable to read resource URL '%s'", templateUrl));
        }
    }


    @Override
    public boolean send(final EmailRegistrationEvent emailRegistrationEvent) {
        final String body = replace(emailVerificationTemplate, emailRegistrationEvent);
        return send(emailRegistrationEvent, body);
    }

    @Override
    public boolean send(final PasswordResetEvent passwordResetEvent) {
        final String body = replace(passwordResetTemplate, passwordResetEvent);
        return send(passwordResetEvent, body);
    }

    protected boolean send(final EmailEventAbstract emailEvent, final String body) {

        final String subject = buildSubject(emailEvent);
        final String to = emailEvent.getEmail();

        return send(subject, to, body);
    }

    private boolean send(final String subject, final String to, final String body) {
        try {

            final Email email = new HtmlEmail();
            email.setAuthenticator(new DefaultAuthenticator(senderEmailAddress, senderEmailPassword));
            email.setHostName(getSenderEmailHostName());
            email.setSmtpPort(senderEmailPort);
            email.setStartTLSEnabled(getSenderEmailTlsEnabled());

            final Properties properties = email.getMailSession().getProperties();

            // TODO ISIS-987: check whether all these are required and extract as configuration settings
            properties.put("mail.smtps.auth", "true");
            properties.put("mail.debug", "true");
            properties.put("mail.smtps.port", "" + senderEmailPort);
            properties.put("mail.smtps.socketFactory.port", "" + senderEmailPort);
            properties.put("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtps.socketFactory.fallback", "false");
            properties.put("mail.smtp.starttls.enable", "" + getSenderEmailTlsEnabled());

            email.setFrom(senderEmailAddress);

            email.setSubject(subject);
            email.setMsg(body);

            email.addTo(to);
            email.send();

        } catch (EmailException ex) {
            LOG.error("An error occurred while trying to send an email about user email verification", ex);
            return false;
        }

        return true;
    }

    private String replace(final String template, final EmailEventAbstract emailEvent) {
        String message = template;
        message = EMAIL_PATTERN.matcher(message).replaceFirst(emailEvent.getEmail());
        message = CONFIRMATION_URL_PATTERN.matcher(message).replaceFirst(emailEvent.getConfirmationUrl());
        message = APPLICATION_NAME_PATTERN.matcher(message).replaceAll(emailEvent.getApplicationName());
        return message;
    }

    protected String buildSubject(final EmailEventAbstract emailEvent) {
        String subject = "["+emailEvent.getApplicationName()+"]";
        if (emailEvent instanceof EmailRegistrationEvent) {
            subject += " Please confirm your identity";
        } else if (emailEvent instanceof PasswordResetEvent) {
            subject += " Password reset request";
        }
        return subject;
    }

    protected String getSenderEmailAddress() {
        return getConfigurationPropertyElseThrow(ISIS_NOTIFICATION_EMAIL_SENDER_ADDRESS);
    }

    protected String getSenderEmailPassword() {
        return getConfigurationPropertyElseThrow(ISIS_NOTIFICATION_EMAIL_SENDER_PASSWORD);
    }

    protected String getSenderEmailHostName() {
        return getConfiguration().getString(ISIS_NOTIFICATION_EMAIL_SENDER_HOSTNAME, ISIS_NOTIFICATION_EMAIL_SENDER_HOSTNAME_DEFAULT);
    }

    protected Integer getSenderEmailPort() {
        return getConfiguration().getInteger(ISIS_NOTIFICATION_EMAIL_PORT, 587);
    }

    protected Boolean getSenderEmailTlsEnabled() {
        return getConfiguration().getBoolean(ISIS_NOTIFICATION_EMAIL_TLS_ENABLED, true);
    }

    protected IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

    private String getConfigurationPropertyElseThrow(final String configProperty) {
        final String configuredValue = getConfiguration().getString(configProperty);
        if(Strings.isNullOrEmpty(configuredValue)) {
            throw new IllegalStateException(configProperty + " not specified");
        }
        return configuredValue;
    }


}
