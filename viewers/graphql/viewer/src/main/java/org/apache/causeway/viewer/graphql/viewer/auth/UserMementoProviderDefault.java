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
package org.apache.causeway.viewer.graphql.viewer.auth;

import javax.inject.Inject;

import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategyParameters;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.causeway.applib.services.user.RoleMemento;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.graphql.applib.auth.UserMementoProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class UserMementoProviderDefault implements UserMementoProvider {

    @Configuration
    public static class AutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(UserMementoProvider.class)
        public UserMementoProvider defaultIdentityProvider(final CausewayConfiguration causewayConfiguration) {
            return new UserMementoProviderDefault(causewayConfiguration);
        }
    }

    @Inject private final CausewayConfiguration causewayConfiguration;

    @Override
    public UserMemento userMemento(
            final ExecutionContext executionContext,
            final ExecutionStrategyParameters parameters) {

        var fallbackUsername = causewayConfiguration.getViewer().getGraphql().getAuthentication().getFallback().getUsername();
        if (fallbackUsername == null) {
            return null;
        }

        var fallbackRoles = causewayConfiguration.getViewer().getGraphql().getAuthentication().getFallback().getRoles();
        var roles = Can.ofStream(fallbackRoles.stream().map(roleName -> RoleMemento.builder().name(roleName).build()));
        return UserMemento.builder()
                .name(fallbackUsername)
                .roles(roles)
                .build();

    }
}
