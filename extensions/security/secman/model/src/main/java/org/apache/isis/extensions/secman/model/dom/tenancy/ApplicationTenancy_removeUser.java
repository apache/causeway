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
package org.apache.isis.extensions.secman.model.dom.tenancy;

import java.util.Collection;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy;
import org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy.RemoveUserDomainEvent;
import org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancyRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = RemoveUserDomainEvent.class, 
        associateWith = "users")
@ActionLayout(named="Remove", sequence = "2")
@RequiredArgsConstructor
public class ApplicationTenancy_removeUser {
    
    @Inject private ApplicationTenancyRepository<? extends ApplicationTenancy> applicationTenancyRepository;
    @Inject private ApplicationUserRepository<? extends ApplicationUser> applicationUserRepository;
    
    private final ApplicationTenancy target;
    
    @Model
    public ApplicationTenancy act(final ApplicationUser applicationUser) {
        applicationTenancyRepository.clearTenancyOnUser(applicationUser);
        return target;
    }
    
    @Model
    public Collection<? extends ApplicationUser> choices0Act() {
        return applicationUserRepository.findByTenancy(target);
    }
    
    @Model
    public String disableAct() {
        return choices0Act().isEmpty()? "No users to remove": null;
    }

}
