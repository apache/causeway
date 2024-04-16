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
package org.apache.causeway.extensions.secman.integration.authenticator;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.core.security.authentication.Authenticator;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;

/**
 * @since 2.0 {@index}
 */
@AutoConfigureOrder(PriorityPrecedence.LATE)
@Configuration
public class AuthenticatorSecmanAutoConfiguration  {

    /**
     * The name of the bean that is required to be declared in order that
     * {@link #authenticatorSecman(ApplicationUserRepository, PasswordEncoder)} can furnish an {@link Authenticator}.
     */
    public static final String PASSWORD_ENCODER_BEAN_NAME = CausewayModuleExtSecmanApplib.NAMESPACE + "PasswordEncoderForSecman";

    @Bean(CausewayModuleExtSecmanApplib.NAMESPACE + ".AuthenticatorSecman")
    @ConditionalOnMissingBean(Authenticator.class)
    @ConditionalOnBean(value = PasswordEncoder.class,  name = PASSWORD_ENCODER_BEAN_NAME)
    public Authenticator authenticatorSecman(
            final ApplicationUserRepository applicationUserRepository,
            final @Qualifier(PASSWORD_ENCODER_BEAN_NAME) PasswordEncoder passwordEncoder) {
        return new AuthenticatorSecman(
                applicationUserRepository, passwordEncoder);
    }

    /**
     * The {@link PasswordEncoder} that will be provided automatically unless the application chooses to provide some other implementation.
     */
    @Bean(PASSWORD_ENCODER_BEAN_NAME)
    @ConditionalOnMissingBean(value = PasswordEncoder.class,  name = PASSWORD_ENCODER_BEAN_NAME)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
