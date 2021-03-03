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

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy;
import org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancy.CollectionDomainEvent;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;

@Collection(
        domainEvent = ApplicationTenancy_users.UsersDomainEvent.class)
@CollectionLayout(
        defaultView="table"
        )
@RequiredArgsConstructor
public class ApplicationTenancy_users {
    
    @Inject private ApplicationUserRepository<? extends ApplicationUser> applicationUserRepository;
    
    private final ApplicationTenancy target;

    // -- users (collection)

    public static class UsersDomainEvent extends CollectionDomainEvent<ApplicationUser> {}

    public java.util.Collection<? extends ApplicationUser> coll() {
        return applicationUserRepository.findByAtPath(target.getPath());
    }

}
