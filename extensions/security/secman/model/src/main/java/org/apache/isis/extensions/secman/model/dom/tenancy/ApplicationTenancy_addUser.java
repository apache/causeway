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
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy;
import org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy.AddUserDomainEvent;
import org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancyRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;

@Action(domainEvent = AddUserDomainEvent.class, associateWith = "users")
@ActionLayout(named="Add")
@RequiredArgsConstructor
public class ApplicationTenancy_addUser {
    
    @Inject private ApplicationTenancyRepository applicationTenancyRepository;
    @Inject private ApplicationUserRepository applicationUserRepository;
    
    private final ApplicationTenancy holder;

    @MemberOrder(sequence = "1")
    public ApplicationTenancy act(final ApplicationUser applicationUser) {
        applicationTenancyRepository.setTenancyOnUser(holder, applicationUser);
        return holder;
    }

    public List<ApplicationUser> autoComplete0Act(final String search) {
        final Collection<ApplicationUser> matchingSearch = applicationUserRepository.find(search);
        final List<ApplicationUser> list = _Lists.newArrayList(matchingSearch);
        list.removeAll(applicationTenancyRepository.getUsers(holder));
        return list;
    }
}
