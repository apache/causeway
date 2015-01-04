package org.apache.isis.core.runtime.services.email;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.userreg.EmailNotificationService;
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
public class EmailNotificationServiceDefault implements EmailNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailNotificationServiceDefault.class);

    private static final Pattern EMAIL_PATTERN = compile(quote("${email}"));
    private static final Pattern CONFIRMATION_URL_PATTERN = compile(quote("${confirmationUrl}"));
    private static final Pattern APPLICATION_NAME_PATTERN = compile(quote("${applicationName}"));

    private String passwordResetTemplate;
    private String emailVerificationTemplate;

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

        emailVerificationTemplate = loadResource("EmailVerificationTemplate.html");
        passwordResetTemplate = loadResource("PasswordResetTemplate.html");

        initialized = true;
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

        return send(to, subject, body);
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

    protected boolean send(final String to, final String subject, final String body) {

        final List<String> toList = Collections.singletonList(to);
        final List<String> cc = Collections.emptyList();
        final List<String> bcc = Collections.emptyList();
        return emailService.send(toList, cc, bcc, subject, body);
    }

    protected String replace(final String template, final EmailEventAbstract emailEvent) {
        String message = template;
        message = EMAIL_PATTERN.matcher(message).replaceFirst(emailEvent.getEmail());
        message = CONFIRMATION_URL_PATTERN.matcher(message).replaceFirst(emailEvent.getConfirmationUrl());
        message = APPLICATION_NAME_PATTERN.matcher(message).replaceAll(emailEvent.getApplicationName());
        return message;
    }

    protected IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

    @Inject
    private EmailServiceDefault emailService;

}
