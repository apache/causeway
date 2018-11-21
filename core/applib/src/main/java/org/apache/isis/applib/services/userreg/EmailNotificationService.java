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

import javax.annotation.PostConstruct;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.userreg.events.EmailRegistrationEvent;
import org.apache.isis.applib.services.userreg.events.PasswordResetEvent;

/**
 * Supporting service for the user-registration functionality.
 *
 * <p>
 *     The framework provides a default implementation which in turn uses the
 *     {@link org.apache.isis.applib.services.email.EmailService}, namely <code>EmailServiceDefault</code>.
 * </p>
 */
public interface EmailNotificationService extends Serializable {

    @PostConstruct
    @Programmatic
    public void init() ;

    @Programmatic
    boolean send(EmailRegistrationEvent ev);

    @Programmatic
    boolean send(PasswordResetEvent ev);

    /**
     * Whether this service has been configured and thus available for use.
     */
    @Programmatic
    boolean isConfigured();
}
