package org.apache.isis.core.runtime.services.email;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import com.google.common.base.Strings;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.email.EmailService;
import org.apache.isis.applib.services.userreg.events.EmailEventAbstract;
import org.apache.isis.applib.services.userreg.events.EmailRegistrationEvent;
import org.apache.isis.applib.services.userreg.events.PasswordResetEvent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.runtime.system.context.IsisContext;

import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;

/**
 * A service that sends email notifications when specific events occur
 */
@com.google.inject.Singleton // necessary because is registered in and injected by google guice
@DomainService
public class EmailServiceDefault implements EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailServiceDefault.class);

    private static final Pattern EMAIL_PATTERN = compile(quote("${email}"));
    private static final Pattern CONFIRMATION_URL_PATTERN = compile(quote("${confirmationUrl}"));
    private static final Pattern APPLICATION_NAME_PATTERN = compile(quote("${applicationName}"));

    private static final String ISIS_SERVICE_EMAIL_SENDER_ADDRESS = "isis.service.email.sender.address";
    private static final String ISIS_SERVICE_EMAIL_SENDER_PASSWORD = "isis.service.email.sender.password";
    private static final String ISIS_SERVICE_EMAIL_SENDER_HOSTNAME = "isis.service.email.sender.hostname";
    private static final String ISIS_SERVICE_EMAIL_SENDER_HOSTNAME_DEFAULT = "smtp.gmail.com";
    private static final String ISIS_SERVICE_EMAIL_PORT = "isis.service.email.port";
    private static final String ISIS_SERVICE_EMAIL_TLS_ENABLED = "isis.service.email.tls.enabled";

    private String senderEmailAddress;
    private String senderEmailPassword;
    private Integer senderEmailPort;

    private boolean initialized;

    /**
     * Loads responsive email templates borrowed from http://zurb.com/ink/templates.php (Basic)
     */
    @PostConstruct
    @Programmatic
    public void init() {

        if(initialized) {
            return;
        }

        senderEmailAddress = getSenderEmailAddress();
        senderEmailPassword = getSenderEmailPassword();

        senderEmailPort = getSenderEmailPort();

        initialized = true;
    }


    @Override
    public boolean send(final List<String> toList, final List<String> ccList, final List<String> bccList, final String subject, final String body) {

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

            if(notEmpty(toList)) {
                email.addTo(toList.toArray(new String[toList.size()]));
            }
            if(notEmpty(ccList)) {
                email.addCc(ccList.toArray(new String[ccList.size()]));
            }
            if(notEmpty(bccList)) {
                email.addBcc(bccList.toArray(new String[bccList.size()]));
            }

            email.send();

        } catch (EmailException ex) {
            LOG.error("An error occurred while trying to send an email about user email verification", ex);
            return false;
        }

        return true;
    }

    protected boolean notEmpty(final List<String> toList) {
        return toList != null && !toList.isEmpty();
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
        return getConfigurationPropertyElseThrow(ISIS_SERVICE_EMAIL_SENDER_ADDRESS);
    }

    protected String getSenderEmailPassword() {
        return getConfigurationPropertyElseThrow(ISIS_SERVICE_EMAIL_SENDER_PASSWORD);
    }

    protected String getSenderEmailHostName() {
        return getConfiguration().getString(ISIS_SERVICE_EMAIL_SENDER_HOSTNAME, ISIS_SERVICE_EMAIL_SENDER_HOSTNAME_DEFAULT);
    }

    protected Integer getSenderEmailPort() {
        return getConfiguration().getInteger(ISIS_SERVICE_EMAIL_PORT, 587);
    }

    protected Boolean getSenderEmailTlsEnabled() {
        return getConfiguration().getBoolean(ISIS_SERVICE_EMAIL_TLS_ENABLED, true);
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
