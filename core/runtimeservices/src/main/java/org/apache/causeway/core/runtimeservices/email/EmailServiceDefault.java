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

import jakarta.activation.DataSource;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.email.EmailService;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.EmailConfiguration;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class EmailServiceDefault implements EmailService {

    @Getter private final EmailConfiguration configuration;
    private final Provider<JavaMailSender> emailSenderProvider;

    @Getter(onMethod_={@Override}) @Accessors(fluent=true)
    private final boolean isConfigured;

    @Inject
    public EmailServiceDefault(
        EmailConfiguration configuration,
        Provider<JavaMailSender> emailSenderProvider) {

        this.configuration = configuration;
        this.emailSenderProvider = emailSenderProvider;

        this.isConfigured = StringUtils.hasLength(configuration.senderAddress())
            && StringUtils.hasLength(configuration.senderPassword());
        if (!isConfigured()) log.warn("NOT configured");
    }

    @SneakyThrows
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

            emailHelper.setFrom(configuration.senderAddress());

            emailHelper.setSubject(subject);
            boolean html = true;
            emailHelper.setText(body, html);

            if (attachments != null) {
                for (DataSource attachment : attachments) {
                    emailHelper.addAttachment(attachment.getName(), attachment);
                }
            }

            final String overrideToList = configuration.overrideTo();
            final String overrideCc = configuration.overrideCc();
            final String overrideBcc = configuration.overrideBcc();

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
            if (configuration.isThrowExceptionOnFail()) throw e;
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
