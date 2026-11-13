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
package org.apache.causeway.core.config;

public record EmailConfiguration(
    /**
     * Specifies the username to use to connect to the SMTP service.
     * <p>
     * If not specified, then the sender's {@link #getAddress() email address} will be used instead.
     * <p>
     * <code>spring.mail.username</code>
     */
    String senderUsername,
    /**
     * Specifies the password (corresponding to the {@link #getUsername() username} to connect to the
     * SMTP service.
     * <p>
     * This configuration property is mandatory (for the default implementation of the
     * {@link org.apache.causeway.applib.services.email.EmailService}, at least).
     * <p>
     * <code>spring.mail.password</code>
     */
    String senderPassword,
    /**
     * Specifies the host running the SMTP service.
     * <p>
     * If not specified, then the value used depends upon the email implementation.
     * The default implementation will use the <code>mail.smtp.host</code> system
     * property.
     * <p>
     * <code>spring.mail.host</code>
     */
    String senderHostName,
    /**
     * The port to use for sending email.
     * <p>
     * <code>spring.mail.port</code>
     */
    int senderPort,
    /**
     * Whether TLS encryption should be started (that is, <code>STARTTLS</code>).
     * <p>
     * <code>spring.mail.javamail.properties.mail.smtp.starttls.enable</code>
     */
    boolean isSenderTlsEnabled,
    /**
     * The maximum number of milliseconds to wait to obtain a socket before timing
     * out.
     * <p>
     * <code>spring.mail.properties.mail.smtp.timeout</code>
     */
    int socketTimeout,
    /**
     * The maximum number of milliseconds to wait to obtain a socket connection
     * before timing out.
     * <p>
     * <code>spring.mail.properties.mail.smtp.connectiontimeout</code>
     */
    int socketConnectionTimeout,
    /**
     * If an email fails to send, whether to propagate the exception (meaning that potentially the end-user
     * might see the exception), or whether instead to just indicate failure through the return value of
     * the method ({@link org.apache.causeway.applib.services.email.EmailService#send(List, List, List, String, String, DataSource...)}
     * that's being called.
     * <p>
     * <code>causeway.core.runtime-services.email.throwExceptionOnFail</code>
     */
    boolean isThrowExceptionOnFail,
    /**
     * Specifies the email address of the user sending the email.
     * <p>
     * If the {@link #getUsername() username} is not specified, is also used as the username to
     * connect to the SMTP service.
     * <p>
     * This configuration property is mandatory (for the default implementation of the
     * {@link org.apache.causeway.applib.services.email.EmailService}, at least).
     * <p>
     * <code>causeway.core.runtime-services.email.sender.address</code>
     */
    String senderAddress,
    /**
     * Intended for testing purposes only, if set then the requested <code>to:</code> of the email will
     * be ignored, and instead sent to this email address instead.
     * <p>
     * <code>causeway.core.runtime-services.email.override.to</code>
     */
    String overrideTo,
    /**
     * Intended for testing purposes only, if set then the requested <code>cc:</code> of the email will
     * be ignored, and instead sent to this email address instead.
     * <p>
     * <code>causeway.core.runtime-services.email.override.cc</code>
     */
    String overrideCc,
    /**
     * Intended for testing purposes only, if set then the requested <code>bcc:</code> of the email will
     * be ignored, and instead sent to this email address instead.
     * <p>
     * <code>causeway.core.runtime-services.email.override.bcc</code>
     */
    String overrideBcc) {
}
