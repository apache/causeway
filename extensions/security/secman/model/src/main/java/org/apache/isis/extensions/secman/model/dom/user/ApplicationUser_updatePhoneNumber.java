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

import javax.enterprise.inject.Model;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUser.UpdatePhoneNumberDomainEvent;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = UpdatePhoneNumberDomainEvent.class, 
        associateWith = "phoneNumber")
@ActionLayout(sequence = "1")
@RequiredArgsConstructor
public class ApplicationUser_updatePhoneNumber {
    
    private final ApplicationUser target;

    @Model
    public ApplicationUser act(
            @ParameterLayout(named="Phone")
            @Parameter(maxLength = ApplicationUser.MAX_LENGTH_PHONE_NUMBER, optionality = Optionality.OPTIONAL)
            final String phoneNumber) {
        target.setPhoneNumber(phoneNumber);
        return target;
    }

    @Model
    public String disableAct() {
        return target.isForSelfOrRunAsAdministrator()? null: "Can only update your own user record.";
    }
    
    @Model
    public String default0Act() {
        return target.getPhoneNumber();
    }
}
