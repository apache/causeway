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
package org.apache.isis.extensions.secman.applib;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.config.IsisConfiguration;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;

@AutoConfigureOrder(OrderPrecedence.LAST)
@Configuration
public class SecmanAutoConfiguration {

    /**
     * Provides a default implementation of {@link SecmanConfiguration}.
     */
    @Bean
    @ConditionalOnMissingBean(SecmanConfiguration.class)
    public SecmanConfiguration bean(final IsisConfiguration isisConfiguration) {
        val secman = isisConfiguration.getExtensions().getSecman();
        return SecmanConfiguration.builder()
                .adminUserName(secman.getSeed().getAdmin().getUserName())
                .adminPassword(secman.getSeed().getAdmin().getPassword())
                .adminRoleName(secman.getSeed().getAdmin().getRoleName())
                .adminStickyNamespacePermissions(secman.getSeed().getAdmin().getNamespacePermissions().getSticky().toArray(new String[]{}))
                .adminAdditionalNamespacePermissions(secman.getSeed().getAdmin().getNamespacePermissions().getAdditional())
                .regularUserRoleName(secman.getSeed().getRegularUser().getRoleName())
                .autoUnlockIfDelegatedAndAuthenticated(secman.getDelegatedUsers().getAutoCreatePolicy() == IsisConfiguration.Extensions.Secman.DelegatedUsers.AutoCreatePolicy.AUTO_CREATE_AS_UNLOCKED)
                .build();
    }

}
