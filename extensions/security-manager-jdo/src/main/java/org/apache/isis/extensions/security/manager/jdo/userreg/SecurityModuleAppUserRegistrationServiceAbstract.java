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
package org.apache.isis.extensions.security.manager.jdo.userreg;

import java.util.Set;

import javax.inject.Inject;

import org.apache.isis.applib.services.userreg.UserDetails;
import org.apache.isis.applib.services.userreg.UserRegistrationService;
import org.apache.isis.applib.value.Password;
import org.apache.isis.extensions.security.manager.jdo.dom.role.ApplicationRole;
import org.apache.isis.extensions.security.manager.jdo.dom.user.ApplicationUser;
import org.apache.isis.extensions.security.manager.jdo.dom.user.ApplicationUserRepository;

/**
 * An abstract implementation of {@link org.apache.isis.applib.services.userreg.UserRegistrationService}
 * with a single abstract method for the initial role of newly created local users
 */
public abstract class SecurityModuleAppUserRegistrationServiceAbstract implements UserRegistrationService {

    @Override
    public boolean usernameExists(final String username) {
        return applicationUserRepository.findByUsername(username) != null;
    }

    @Override
    public void registerUser(
        final UserDetails userDetails) {

        final Password password = new Password(userDetails.getPassword());
        final ApplicationRole initialRole = getInitialRole();
        final Boolean enabled = true;
        final String username = userDetails.getUsername();
        final String emailAddress = userDetails.getEmailAddress();
        final ApplicationUser applicationUser = applicationUserRepository.newLocalUser(username, password, password, initialRole, enabled, emailAddress);

        final Set<ApplicationRole> additionalRoles = getAdditionalInitialRoles();
        if(additionalRoles != null) {
            for (final ApplicationRole additionalRole : additionalRoles) {
                applicationUser.addRole(additionalRole);
            }
        }

    }

    @Override
    public boolean emailExists(final String emailAddress) {
        return applicationUserRepository.findByEmailAddress(emailAddress) != null;
    }

    @Override
    public boolean updatePasswordByEmail(final String emailAddress, final String password) {
        boolean passwordUpdated = false;
        final ApplicationUser user = applicationUserRepository.findByEmailAddress(emailAddress);
        if (user != null) {
            user.updatePassword(password);
            passwordUpdated = true;
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

    @Inject
    private ApplicationUserRepository applicationUserRepository;
}
