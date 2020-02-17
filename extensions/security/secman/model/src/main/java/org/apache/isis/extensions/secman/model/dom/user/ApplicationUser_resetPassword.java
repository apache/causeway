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

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.value.Password;
import org.apache.isis.extensions.secman.api.encryption.PasswordEncryptionService;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUser.ResetPasswordDomainEvent;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = ResetPasswordDomainEvent.class, 
        associateWith = "hasPassword",
        associateWithSequence = "20")
@RequiredArgsConstructor
public class ApplicationUser_resetPassword {
    
    @Inject private PasswordEncryptionService passwordEncryptionService;
    @Inject private FactoryService factory;
    
    private final ApplicationUser holder;

    @Model
    public ApplicationUser act(
            @ParameterLayout(named="New password")
            final Password newPassword,
            @ParameterLayout(named="Repeat password")
            final Password newPasswordRepeat) {
        
        factory.mixin(ApplicationUser_updatePassword.class, holder)
        .updatePassword(newPassword.getPassword());
        
        
        return holder;
    }

    @Model
    public boolean hideAct() {
        return holder.isDelegateAccountOrPasswordEncryptionNotAvailable(passwordEncryptionService);
    }

    @Model
    public String validateAct(
            final Password newPassword,
            final Password newPasswordRepeat) {
        if(holder.isDelegateAccountOrPasswordEncryptionNotAvailable(passwordEncryptionService)) {
            return null;
        }
        if (!Objects.equals(newPassword, newPasswordRepeat)) {
            return "Passwords do not match";
        }

        return null;
    }


}
