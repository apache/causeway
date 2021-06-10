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
package org.apache.isis.applib.services.userreg;

import java.io.Serializable;

import org.apache.isis.applib.services.userreg.events.EmailRegistrationEvent;
import org.apache.isis.applib.services.userreg.events.PasswordResetEvent;

/**
 * Supporting service for the user-registration functionality.
 * <p>
 * The framework provides a default implementation which in turn uses the
 * {@link org.apache.isis.applib.services.email.EmailService}, namely <code>EmailServiceDefault</code>.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface EmailNotificationService
        extends Serializable {

    /**
     * Sends an email to verify an email address as part of the initial user registration
     *
     * @param ev
     */
    boolean send(EmailRegistrationEvent ev);

    /**
     * Sends an email to reset a password for an already-registered user
     *
     * @param ev
     */
    boolean send(PasswordResetEvent ev);

    /**
     * Whether this service has been configured and thus available for use.
     *
     * <p>
     *     If this returns false then it is _not_ valid to call
     *     {@link #send(EmailRegistrationEvent)} (and doing so will result in
     *     an {@link IllegalStateException} being thrown).
     * </p>
     */
    boolean isConfigured();

}
