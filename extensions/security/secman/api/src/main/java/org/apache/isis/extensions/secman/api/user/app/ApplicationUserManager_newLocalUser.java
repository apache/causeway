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
package org.apache.isis.extensions.secman.api.user.app;

import java.util.Objects;

import javax.inject.Inject;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Password;
import org.apache.isis.extensions.secman.api.SecmanConfiguration;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserRepository;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserStatus;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_updateEmailAddress;

/**
 * @apiNote This mixin requires concrete implementations associated with JPA and JDO,
 * since action's type parameters are inspected for their compile time types
 * and the ApplicationRole here is just an interface that the framework has not much
 * meta-model information to derive UI behavior from.
 *
 * @implNote due to current limitations, both the main and its supporting methods have to be
 * overridden with the concrete subclasses.
 *
 */
public abstract class ApplicationUserManager_newLocalUser<R extends ApplicationRole> {

    @Inject private ApplicationRoleRepository<R> applicationRoleRepository;
    @Inject private ApplicationUserRepository<? extends ApplicationUser> applicationUserRepository;
    @Inject private SecmanConfiguration configBean;
    @Inject private FactoryService factory;
    @Inject private RepositoryService repository;

    protected ApplicationUser doAct(
            final String username,
            final Password password,
            final Password passwordRepeat,
            final R initialRole,
            final Boolean enabled,
            final String emailAddress) {

        ApplicationUser user = applicationUserRepository.findByUsername(username).orElse(null);
        if (user == null) {
            user = applicationUserRepository
                    .newLocalUser(username, password, ApplicationUserStatus.parse(enabled));
        }
        if (initialRole != null) {
            applicationRoleRepository.addRoleToUser(initialRole, user);
        }
        if (emailAddress != null) {
            factory.mixin(ApplicationUser_updateEmailAddress.class, user)
            .act(emailAddress);
        }
        repository.persist(user);
        return user;
    }

    protected String doValidate(
            final String username,
            final Password newPassword,
            final Password newPasswordRepeat,
            final R initialRole,
            final Boolean enabled,
            final String emailAddress) {

        if (!Objects.equals(newPassword, newPasswordRepeat)) {
            return "Passwords do not match";
        }

        return null;
    }

    protected R doDefault3() {
        return applicationRoleRepository
                .findByNameCached(configBean.getRegularUserRoleName())
                .orElse(null);
    }


}
