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
package org.apache.causeway.core.runtimeservices.email;

import java.util.Collections;
import java.util.List;

import javax.activation.DataSource;
import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.email.EmailService;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.extern.log4j.Log4j2;

/**
 * Default implementation of {@link EmailService}, that uses Spring Boot's {@link JavaMailSender} API service
 * to send out emails.
 *
 * <p>
 *     Note that this implementation requires that an implementation of Spring's {@link JavaMailSender}
 *     is configured.
 * </p>
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".EmailServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class EmailServiceDefault implements EmailService {

    private static final long serialVersionUID = 1L;
    public static class EmailServiceException extends RuntimeException {
        static final long serialVersionUID = 1L;
        public EmailServiceException(final Exception cause) {
            super(cause);
        }
    }

    @Inject private CausewayConfiguration configuration;

    @Inject private Provider<JavaMailSender> emailSenderProvider;

    // -- INIT

    private boolean initialized;

    /**
     * Loads responsive email templates borrowed from http://zurb.com/ink/templates.php (Basic)
     */
    @Override
    @PostConstruct
    public void init() {

        if (initialized) {
            return;
        }

        initialized = true;

        if (!isConfigured()) {
            log.warn("NOT configured");
        } else {
            log.debug("configured");
        }
    }

    protected String getSenderEmailUsername() {
        return configuration.getCore().getRuntimeServices().getEmail().getSender().getUsername();
    }

    protected String getSenderEmailAddress() {
        return configuration.getCore().getRuntimeServices().getEmail().getSender().getAddress();
    }

    protected String getSenderEmailPassword() {
        return configuration.getCore().getRuntimeServices().getEmail().getSender().getPassword();
    }

    protected String getSenderEmailHostName() {
        return configuration.getCore().getRuntimeServices().getEmail().getSender().getHostname();
    }

    protected Integer getSenderEmailPort() {
        return configuration.getCore().getRuntimeServices().getEmail().getPort();
    }

    protected Boolean getSenderEmailTlsEnabled() {
        return configuration.getCore().getRuntimeServices().getEmail().getTls().isEnabled();
    }

    protected boolean isThrowExceptionOnFail() {
        return configuration.getCore().getRuntimeServices().getEmail().isThrowExceptionOnFail();
    }

    protected int getSocketTimeout() {
        return configuration.getCore().getRuntimeServices().getEmail().getSocketTimeout();
    }

    protected int getSocketConnectionTimeout() {
        return configuration.getCore().getRuntimeServices().getEmail().getSocketConnectionTimeout();
    }

    protected String getEmailOverrideTo() {
        return configuration.getCore().getRuntimeServices().getEmail().getOverride().getTo();
    }

    protected String getEmailOverrideCc() {
        return configuration.getCore().getRuntimeServices().getEmail().getOverride().getCc();
    }

    protected String getEmailOverrideBcc() {
        return configuration.getCore().getRuntimeServices().getEmail().getOverride().getBcc();
    }

    @Override
    public boolean isConfigured() {
        final String senderEmailAddress = getSenderEmailAddress();
        final String senderEmailPassword = getSenderEmailPassword();
        return !_Strings.isNullOrEmpty(senderEmailAddress) && !_Strings.isNullOrEmpty(senderEmailPassword);
    }

    @Override
    public boolean send(
            final List<String> toList,
            final List<String> ccList,
            final List<String> bccList,
            final String subject,
            final String body,
            final DataSource... attachments) {

        try {

            var javaMailSender = emailSenderProvider.get();

            var email = javaMailSender.createMimeMessage();
            var emailHelper = new MimeMessageHelper(email, true);

            emailHelper.setFrom(getSenderEmailAddress());

            emailHelper.setSubject(subject);
            boolean html = true;
            emailHelper.setText(body, html);

            if (attachments != null) {
                for (DataSource attachment : attachments) {
                    emailHelper.addAttachment(attachment.getName(), attachment);
                }
            }

            final String overrideToList = getEmailOverrideTo();
            final String overrideCc = getEmailOverrideCc();
            final String overrideBcc = getEmailOverrideBcc();

            final String[] toListElseOverride = originalUnlessOverridden(toList, overrideToList);
            if (notEmpty(toListElseOverride)) {
                emailHelper.setTo(toListElseOverride);
            }
            final String[] ccListElseOverride = originalUnlessOverridden(ccList, overrideCc);
            if (notEmpty(ccListElseOverride)) {
                emailHelper.setCc(ccListElseOverride);
            }
            final String[] bccListElseOverride = originalUnlessOverridden(bccList, overrideBcc);
            if (notEmpty(bccListElseOverride)) {
                emailHelper.setBcc(bccListElseOverride);
            }

            javaMailSender.send(email);

        } catch (MessagingException e) {
            log.error("An error occurred while trying to send an email", e);
            final Boolean throwExceptionOnFail = isThrowExceptionOnFail();
            if (throwExceptionOnFail) {
                throw new EmailServiceException(e);
            }
            return false;
        }

        return true;
    }

    // -- HELPER

    static String[] originalUnlessOverridden(final List<String> original, final String overrideIfAny) {
        final List<String> addresses = _Strings.isNullOrEmpty(overrideIfAny)
                ? original == null
                ? Collections.<String>emptyList()
                        : original
                        : Collections.singletonList(overrideIfAny);
        return addresses.toArray(new String[addresses.size()]);
    }

    static boolean notEmpty(final String[] addresses) {
        return addresses != null && addresses.length > 0;
    }

}
