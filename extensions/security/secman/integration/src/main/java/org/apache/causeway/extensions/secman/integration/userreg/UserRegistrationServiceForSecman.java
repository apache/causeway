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
package org.apache.causeway.extensions.secman.integration.userreg;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.userreg.UserDetails;
import org.apache.causeway.applib.services.userreg.UserRegistrationService;
import org.apache.causeway.applib.value.Password;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserStatus;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * An implementation of {@link org.apache.causeway.applib.services.userreg.UserRegistrationService}
 * to allow users to be automatically created with the configured initial
 * role(s).
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleExtSecmanApplib.NAMESPACE + ".UserRegistrationServiceForSecman")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("SecMan")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class UserRegistrationServiceForSecman implements UserRegistrationService {

    private final ApplicationUserRepository applicationUserRepository;
    private final ApplicationRoleRepository applicationRoleRepository;
    private final CausewayConfiguration causewayConfiguration;

    @Override
    public boolean usernameExists(final String username) {
        return applicationUserRepository.findByUsername(username).isPresent();
    }

    @Override
    public void registerUser(
            final UserDetails userDetails) {

        final Password password = new Password(userDetails.getPassword());

        final String username = userDetails.getUsername();
        final String emailAddress = userDetails.getEmailAddress();
        final ApplicationUser applicationUser = applicationUserRepository
                .newLocalUser(username, password, ApplicationUserStatus.UNLOCKED);

        if(_Strings.isNotEmpty(emailAddress)) {
            applicationUser.setEmailAddress(emailAddress);
        }

        causewayConfiguration.getExtensions().getSecman().getUserRegistration().getInitialRoleNames().stream()
                .map(applicationRoleRepository::findByName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(role -> applicationRoleRepository.addRoleToUser(role, applicationUser));
    }

    @Override
    public boolean emailExists(final String emailAddress) {
        return applicationUserRepository.findByEmailAddress(emailAddress).isPresent();
    }

    @Override
    public boolean updatePasswordByEmail(final String emailAddress, final String password) {
        return applicationUserRepository.findByEmailAddress(emailAddress)
                .map(user -> {
                    val passwordWasUpdated = applicationUserRepository.updatePassword(user, password);
                    return passwordWasUpdated;
                })
                .orElse(false);
    }

}
