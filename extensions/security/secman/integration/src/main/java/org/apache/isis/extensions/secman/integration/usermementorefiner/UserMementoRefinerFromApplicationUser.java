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
package org.apache.isis.extensions.secman.integration.usermementorefiner;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.core.security.authentication.manager.UserMementoRefiner;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;

@Service
//@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class UserMementoRefinerFromApplicationUser implements UserMementoRefiner {

    final Provider<ApplicationUserRepository> applicationUserRepositoryProvider;

    @Override
    public UserMemento refine(final UserMemento userMemento) {
        return applicationUserRepositoryProvider.get().findByUsername(userMemento.getName())
                .map(applicationUser ->
                    userMemento.asBuilder()
                    .multiTenancyToken(applicationUser.getAtPath())
                    .languageLocale(applicationUser.getLanguage())
                    .numberFormatLocale(applicationUser.getNumberFormat())
                    .timeFormatLocale(applicationUser.getTimeFormat())
                    .build()
                )
                .orElse(userMemento);
    }
}
