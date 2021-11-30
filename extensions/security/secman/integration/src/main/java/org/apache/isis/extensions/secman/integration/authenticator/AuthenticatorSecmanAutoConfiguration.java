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
package org.apache.isis.extensions.secman.integration.authenticator;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.core.security.authentication.Authenticator;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserRepository;

/**
 * @since 2.0 {@index}
 */
@AutoConfigureOrder(PriorityPrecedence.LATE)
@Configuration
public class AuthenticatorSecmanAutoConfiguration  {

    @Bean("isis.ext.secman.AuthenticatorSecman")
    @ConditionalOnMissingBean(Authenticator.class)
    @Qualifier("Secman")
    public Authenticator authenticatorSecman(
            final ApplicationUserRepository applicationUserRepository,
            final @Qualifier("secman") PasswordEncoder passwordEncoder) {
        return new AuthenticatorSecman(applicationUserRepository, passwordEncoder);
    }

}
