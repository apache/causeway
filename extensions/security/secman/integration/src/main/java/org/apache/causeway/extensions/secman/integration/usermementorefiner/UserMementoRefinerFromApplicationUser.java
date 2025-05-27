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
package org.apache.causeway.extensions.secman.integration.usermementorefiner;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.core.security.authentication.manager.UserMementoRefiner;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;

@Service
//@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class UserMementoRefinerFromApplicationUser implements UserMementoRefiner {

    final Provider<ApplicationUserRepository> applicationUserRepositoryProvider;

    @Override
    public UserMemento refine(final UserMemento userMemento) {
        var applicationUserIfAny = applicationUserRepositoryProvider.get().findByUsername(userMemento.name());
        if (applicationUserIfAny.isEmpty()) {
            return userMemento;
        }
        var applicationUser = applicationUserIfAny.get();

        UserMemento refinedUserMemento =
                userMemento.withMultiTenancyToken(applicationUser.getAtPath())
                           .withLanguageLocale(applicationUser.getLanguage())
                           .withNumberFormatLocale(applicationUser.getNumberFormat())
                           .withTimeFormatLocale(applicationUser.getTimeFormat());

        for (var role : applicationUser.getRoles()) {
            refinedUserMemento = refinedUserMemento.withRoleAdded(role.getName());
        }

        return refinedUserMemento;
    }
}
