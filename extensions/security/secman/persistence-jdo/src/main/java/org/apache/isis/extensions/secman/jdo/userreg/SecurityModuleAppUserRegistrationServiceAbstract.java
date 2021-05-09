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
package org.apache.isis.extensions.secman.jdo.userreg;

import java.util.Set;

import javax.inject.Inject;

import org.apache.isis.applib.services.userreg.UserDetails;
import org.apache.isis.applib.services.userreg.UserRegistrationService;
import org.apache.isis.applib.value.Password;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.dom.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserRepository;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserStatus;

/**
 * An abstract implementation of {@link org.apache.isis.applib.services.userreg.UserRegistrationService}
 * with a single abstract method for the initial role of newly created local users
 *
 * @since 2.0 {@index}
 */
public abstract class SecurityModuleAppUserRegistrationServiceAbstract implements UserRegistrationService {

    @Inject private ApplicationUserRepository applicationUserRepository;
    @Inject private ApplicationRoleRepository applicationRoleRepository;

    @Override
    public boolean usernameExists(final String username) {
        return applicationUserRepository.findByUsername(username).isPresent();
    }

    @Override
    public void registerUser(
            final UserDetails userDetails) {

        final Password password = new Password(userDetails.getPassword());
        final ApplicationRole initialRole = getInitialRole();

        final String username = userDetails.getUsername();
        final String emailAddress = userDetails.getEmailAddress();
        final ApplicationUser applicationUser = (ApplicationUser) applicationUserRepository
                .newLocalUser(username, password, ApplicationUserStatus.ENABLED);

        if(_Strings.isNotEmpty(emailAddress)) {
            applicationUser.setEmailAddress(emailAddress);
        }
        if(initialRole!=null) {
            applicationRoleRepository.addRoleToUser(initialRole, applicationUser);
        }

        final Set<ApplicationRole> additionalRoles = getAdditionalInitialRoles();
        if(additionalRoles != null) {
            for (final ApplicationRole additionalRole : additionalRoles) {
                applicationRoleRepository.addRoleToUser(additionalRole, applicationUser);
            }
        }

    }

    @Override
    public boolean emailExists(final String emailAddress) {
        return applicationUserRepository.findByEmailAddress(emailAddress).isPresent();
    }

    @Override
    public boolean updatePasswordByEmail(final String emailAddress, final String password) {
        boolean passwordUpdated = false;
        final ApplicationUser user = applicationUserRepository.findByEmailAddress(emailAddress)
                .orElse(null);
        if (user != null) {
            passwordUpdated = applicationUserRepository.updatePassword(user, password);;
        }
        return passwordUpdated;
    }

    /**
     * @return The role to use for newly created local users
     */
    protected abstract ApplicationRole getInitialRole();

    /**
     * @return Additional roles for newly created local users
     */
    protected abstract Set<ApplicationRole> getAdditionalInitialRoles();

}
