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

import jakarta.inject.Inject;

import org.apache.causeway.applib.services.user.RoleMemento;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class UserMementoProviderDefault implements UserMementoProvider {

    @SuppressWarnings("CdiInjectInspection")
    @Inject private final CausewayConfiguration causewayConfiguration;


    @Override
    public UserMemento userMemento() {

        val fallbackUsername = causewayConfiguration.getViewer().getGqlv().getAuthentication().getFallback().getUsername();
        if (fallbackUsername == null) {
            return null;
        }

        val fallbackRoles = causewayConfiguration.getViewer().getGqlv().getAuthentication().getFallback().getRoles();
        val roles = Can.ofStream(fallbackRoles.stream().map(roleName -> RoleMemento.builder().name(roleName).build()));
        return UserMemento.builder()
                .name(fallbackUsername)
                .roles(roles)
                .build();

    }
}
