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
package org.apache.isis.extensions.secman.model.dom.user;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.value.Password;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.extensions.secman.api.encryption.PasswordEncryptionService;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUser.UpdatePasswordDomainEvent;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(
        domainEvent = UpdatePasswordDomainEvent.class, 
        associateWith = "hasPassword",
        associateWithSequence = "10")
@RequiredArgsConstructor
public class ApplicationUser_updatePassword {
    
    @Inject private ApplicationUserRepository<? extends ApplicationUser> applicationUserRepository;
    @Inject private Optional<PasswordEncryptionService> passwordEncryptionService; // empty if no candidate is available
    
    private final ApplicationUser target;

    @Model
    public ApplicationUser act(
            @ParameterLayout(named="Existing password")
            final Password existingPassword,
            @ParameterLayout(named="New password")
            final Password newPassword,
            @ParameterLayout(named="Re-enter password")
            final Password newPasswordRepeat) {
        
        applicationUserRepository.updatePassword(target, newPassword.getPassword());
        return target;
    }

    @Model
    public boolean hideAct() {
        return !applicationUserRepository.isPasswordFeatureEnabled(target);
    }

    @Model
    public String disableAct() {

        if(!target.isForSelfOrRunAsAdministrator()) {
            return "Can only update password for your own user account.";
        }
        if (!target.isHasPassword()) {
            return "Password must be reset by administrator.";
        }
        return null;
    }

    @Model
    public String validateAct(
            final Password existingPassword,
            final Password newPassword,
            final Password newPasswordRepeat) {

        if(!applicationUserRepository.isPasswordFeatureEnabled(target)) {
            return "Password feature is not available for this User";
        }

        val encrypter = passwordEncryptionService.orElseThrow(_Exceptions::unexpectedCodeReach);
        
        val encryptedPassword = target.getEncryptedPassword();
        
        if(target.getEncryptedPassword() != null) {
            if (!encrypter.matches(existingPassword.getPassword(), encryptedPassword)) {
                return "Existing password is incorrect";
            }
        }

        if (!Objects.equals(newPassword, newPasswordRepeat)) {
            return "Passwords do not match";
        }

        return null;
    }
    

}
