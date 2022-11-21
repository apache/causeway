/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.core.runtimeservices.userreg;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.email.EmailService;
import org.apache.causeway.applib.services.userreg.EmailNotificationService;
import org.apache.causeway.applib.services.userreg.events.EmailEventAbstract;
import org.apache.causeway.applib.services.userreg.events.EmailRegistrationEvent;
import org.apache.causeway.applib.services.userreg.events.PasswordResetEvent;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".EmailNotificationServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class EmailNotificationServiceDefault implements EmailNotificationService {

    private static final long serialVersionUID = 1L;

    @Inject private EmailService emailService;

    // -- CONSTANTS

    private static final Pattern EMAIL_PATTERN = compile(quote("${email}"));
    private static final Pattern CONFIRMATION_URL_PATTERN = compile(quote("${confirmationUrl}"));
    private static final Pattern APPLICATION_NAME_PATTERN = compile(quote("${applicationName}"));

    private String passwordResetTemplate;
    private String emailVerificationTemplate;


    // -- INIT

    private boolean initialized;

    /**
     * Loads responsive email templates borrowed from http://zurb.com/ink/templates.php (Basic)
     */
    @PostConstruct
    public void init() {

        if(initialized) {
            return;
        }

        emailVerificationTemplate = loadResource("EmailVerificationTemplate.html");
        passwordResetTemplate = loadResource("PasswordResetTemplate.html");

        initialized = true;
    }

    protected String loadResource(final String resourceName) {
        try {
            return _Resources.loadAsStringUtf8(EmailNotificationServiceDefault.class, resourceName);
        } catch (Exception e) {
            throw _Exceptions.illegalState(e, "Unable to read resource '%s' relative to %s",
                    resourceName,
                    EmailNotificationServiceDefault.class.getName());
        }
    }

    // -- isConfigured

    @Override
    public boolean isConfigured() {
        return emailService != null && emailService.isConfigured();
    }

    // -- SEND

    @Override
    public boolean send(final EmailRegistrationEvent emailRegistrationEvent) {
        ensureConfigured();
        final String body = replace(emailVerificationTemplate, emailRegistrationEvent);
        return sendEmail(emailRegistrationEvent, body);
    }

    @Override
    public boolean send(final PasswordResetEvent passwordResetEvent) {
        ensureConfigured();
        final String body = replace(passwordResetTemplate, passwordResetEvent);
        return sendEmail(passwordResetEvent, body);
    }

    // -- helper methods for send(...)

    private void ensureConfigured() {
        if(!isConfigured()) {
            throw new IllegalStateException("Not configured");
        }
    }

    protected boolean sendEmail(final EmailEventAbstract emailEvent, final String body) {

        final String subject = buildSubject(emailEvent);
        final String to = emailEvent.getEmail();

        return sendEmail(to, subject, body);
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

    protected boolean sendEmail(final String to, final String subject, final String body) {

        final List<String> toList = Collections.singletonList(to);
        final List<String> cc = Collections.emptyList();
        final List<String> bcc = Collections.emptyList();
        return emailService.send(toList, cc, bcc, subject, body);
    }

    protected String replace(final String template, final EmailEventAbstract emailEvent) {
        String message = template;
        message = EMAIL_PATTERN.matcher(message).replaceFirst(emailEvent.getEmail());
        message = CONFIRMATION_URL_PATTERN.matcher(message).replaceAll(emailEvent.getConfirmationUrl());
        message = APPLICATION_NAME_PATTERN.matcher(message).replaceAll(emailEvent.getApplicationName());
        return message;
    }


}
