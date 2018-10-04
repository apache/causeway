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
package org.apache.isis.core.runtime.services.email;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.activation.DataSource;
import javax.annotation.PostConstruct;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.ImageHtmlEmail;
import org.apache.commons.mail.resolver.DataSourceClassPathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.email.EmailService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.commons.config.IsisConfiguration;

/**
 * A service that sends email notifications when specific events occur
 */
@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class EmailServiceDefault implements EmailService {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(EmailServiceDefault.class);

    public static class EmailServiceException extends RuntimeException {
        static final long serialVersionUID = 1L;
        public EmailServiceException(final EmailException cause) {
            super(cause);
        }
    }

    // region > constants
    private static final String ISIS_SERVICE_EMAIL_SENDER_USERNAME = "isis.service.email.sender.username";
    private static final String ISIS_SERVICE_EMAIL_SENDER_ADDRESS = "isis.service.email.sender.address";
    private static final String ISIS_SERVICE_EMAIL_SENDER_PASSWORD = "isis.service.email.sender.password";

    private static final String ISIS_SERVICE_EMAIL_SENDER_HOSTNAME = "isis.service.email.sender.hostname";
    private static final String ISIS_SERVICE_EMAIL_SENDER_HOSTNAME_DEFAULT = "smtp.gmail.com";

    private static final String ISIS_SERVICE_EMAIL_PORT = "isis.service.email.port";
    private static final int ISIS_SERVICE_EMAIL_PORT_DEFAULT = 587;

    private static final String ISIS_SERVICE_EMAIL_TLS_ENABLED = "isis.service.email.tls.enabled";
    private static final boolean ISIS_SERVICE_EMAIL_TLS_ENABLED_DEFAULT = true;

    private static final String ISIS_SERVICE_EMAIL_THROW_EXCEPTION_ON_FAIL = "isis.service.email.throwExceptionOnFail";
    private static final boolean ISIS_SERVICE_EMAIL_THROW_EXCEPTION_ON_FAIL_DEFAULT = true;

    private static final String ISIS_SERVICE_EMAIL_SOCKET_TIMEOUT = "isis.service.email.socketTimeout";
    private static final int ISIS_SERVICE_EMAIL_SOCKET_TIMEOUT_DEFAULT = 2000;

    private static final String ISIS_SERVICE_EMAIL_SOCKET_CONNECTION_TIMEOUT = "isis.service.email.socketConnectionTimeout";
    private static final int ISIS_SERVICE_EMAIL_SOCKET_CONNECTION_TIMEOUT_DEFAULT = 2000;

    private static final String ISIS_SERVICE_EMAIL_OVERRIDE_TO = "isis.service.email.override.to";
    private static final String ISIS_SERVICE_EMAIL_OVERRIDE_CC = "isis.service.email.override.cc";
    private static final String ISIS_SERVICE_EMAIL_OVERRIDE_BCC = "isis.service.email.override.bcc";

    // endregion

    // region > init
    private boolean initialized;

    /**
     * Loads responsive email templates borrowed from http://zurb.com/ink/templates.php (Basic)
     */
    @Override
    @PostConstruct
    @Programmatic
    public void init() {

        if (initialized) {
            return;
        }

        initialized = true;

        if (!isConfigured()) {
            LOG.warn("NOT configured");
        } else {
            LOG.debug("configured");
        }
    }

    protected String getSenderEmailUsername() {
        return configuration.getString(ISIS_SERVICE_EMAIL_SENDER_USERNAME);
    }

    protected String getSenderEmailAddress() {
        return configuration.getString(ISIS_SERVICE_EMAIL_SENDER_ADDRESS);
    }

    protected String getSenderEmailPassword() {
        return configuration.getString(ISIS_SERVICE_EMAIL_SENDER_PASSWORD);
    }

    protected String getSenderEmailHostName() {
        return configuration.getString(ISIS_SERVICE_EMAIL_SENDER_HOSTNAME, ISIS_SERVICE_EMAIL_SENDER_HOSTNAME_DEFAULT);
    }

    protected Integer getSenderEmailPort() {
        return configuration.getInteger(ISIS_SERVICE_EMAIL_PORT, ISIS_SERVICE_EMAIL_PORT_DEFAULT);
    }

    protected Boolean getSenderEmailTlsEnabled() {
        return configuration.getBoolean(ISIS_SERVICE_EMAIL_TLS_ENABLED, ISIS_SERVICE_EMAIL_TLS_ENABLED_DEFAULT);
    }

    protected Boolean isThrowExceptionOnFail() {
        return configuration.getBoolean(ISIS_SERVICE_EMAIL_THROW_EXCEPTION_ON_FAIL, ISIS_SERVICE_EMAIL_THROW_EXCEPTION_ON_FAIL_DEFAULT);
    }

    protected int getSocketTimeout() {
        return configuration.getInteger(ISIS_SERVICE_EMAIL_SOCKET_TIMEOUT, ISIS_SERVICE_EMAIL_SOCKET_TIMEOUT_DEFAULT);
    }

    protected int getSocketConnectionTimeout() {
        return configuration.getInteger(ISIS_SERVICE_EMAIL_SOCKET_CONNECTION_TIMEOUT, ISIS_SERVICE_EMAIL_SOCKET_CONNECTION_TIMEOUT_DEFAULT);
    }

    protected String getEmailOverrideTo() {
        return configuration.getString(ISIS_SERVICE_EMAIL_OVERRIDE_TO);
    }

    protected String getEmailOverrideCc() {
        return configuration.getString(ISIS_SERVICE_EMAIL_OVERRIDE_CC);
    }

    protected String getEmailOverrideBcc() {
        return configuration.getString(ISIS_SERVICE_EMAIL_OVERRIDE_BCC);
    }

    // endregion

    // region > isConfigured

    @Override
    public boolean isConfigured() {
        final String senderEmailAddress = getSenderEmailAddress();
        final String senderEmailPassword = getSenderEmailPassword();
        return !_Strings.isNullOrEmpty(senderEmailAddress) && !_Strings.isNullOrEmpty(senderEmailPassword);
    }
    // endregion

    // region > send

    @Override
    public boolean send(final List<String> toList, final List<String> ccList, final List<String> bccList, final String subject, final String body,
            final DataSource... attachments) {

        try {
            final ImageHtmlEmail email = new ImageHtmlEmail();

            final String senderEmailUsername = getSenderEmailUsername();
            final String senderEmailAddress = getSenderEmailAddress();
            final String senderEmailPassword = getSenderEmailPassword();
            final String senderEmailHostName = getSenderEmailHostName();
            final Integer senderEmailPort = getSenderEmailPort();
            final Boolean senderEmailTlsEnabled = getSenderEmailTlsEnabled();
            final int socketTimeout = getSocketTimeout();
            final int socketConnectionTimeout = getSocketConnectionTimeout();

            if (senderEmailUsername != null) {
                email.setAuthenticator(new DefaultAuthenticator(senderEmailUsername, senderEmailPassword));
            } else {
                email.setAuthenticator(new DefaultAuthenticator(senderEmailAddress, senderEmailPassword));
            }
            email.setHostName(senderEmailHostName);
            email.setSmtpPort(senderEmailPort);
            email.setStartTLSEnabled(senderEmailTlsEnabled);
            email.setDataSourceResolver(new DataSourceClassPathResolver("/", true));

            email.setSocketTimeout(socketTimeout);
            email.setSocketConnectionTimeout(socketConnectionTimeout);

            final Properties properties = email.getMailSession().getProperties();

            properties.put("mail.smtps.auth", "true");
            properties.put("mail.debug", "true");
            properties.put("mail.smtps.port", "" + senderEmailPort);
            properties.put("mail.smtps.socketFactory.port", "" + senderEmailPort);
            properties.put("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtps.socketFactory.fallback", "false");
            properties.put("mail.smtp.starttls.enable", "" + senderEmailTlsEnabled);

            email.setFrom(senderEmailAddress);

            email.setSubject(subject);
            email.setHtmlMsg(body);

            if (attachments != null && attachments.length > 0) {
                for (DataSource attachment : attachments) {
                    email.attach(attachment, attachment.getName(), "");
                }
            }


            final String overrideTo = getEmailOverrideTo();
            final String overrideCc = getEmailOverrideCc();
            final String overrideBcc = getEmailOverrideBcc();

            final String[] toListElseOverride = actually(toList, overrideTo);
            if (notEmpty(toListElseOverride)) {
                email.addTo(toListElseOverride);
            }
            final String[] ccListElseOverride = actually(ccList, overrideCc);
            if (notEmpty(ccListElseOverride)) {
                email.addCc(ccListElseOverride);
            }
            final String[] bccListElseOverride = actually(bccList, overrideBcc);
            if (notEmpty(bccListElseOverride)) {
                email.addBcc(bccListElseOverride);
            }

            email.send();

        } catch (EmailException ex) {
            LOG.error("An error occurred while trying to send an email", ex);
            final Boolean throwExceptionOnFail = isThrowExceptionOnFail();
            if (throwExceptionOnFail) {
                throw new EmailServiceException(ex);
            }
            return false;
        }

        return true;
    }
    // endregion


    // region > helper methods

    static String[] actually(final List<String> original, final String overrideIfAny) {
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
    // endregion


    // endregion

    @javax.inject.Inject
    IsisConfiguration configuration;

}