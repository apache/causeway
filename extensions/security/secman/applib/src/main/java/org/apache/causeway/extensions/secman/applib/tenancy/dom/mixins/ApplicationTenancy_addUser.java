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
package org.apache.causeway.extensions.secman.applib.tenancy.dom.mixins;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.MinLength;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancyRepository;
import org.apache.causeway.extensions.secman.applib.tenancy.dom.mixins.ApplicationTenancy_addUser.DomainEvent;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;

/**
 *
 * @since 2.0 {@index}
 */
@Action(
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        associateWith = "users",
        named = "Add",
        sequence = "1"
)
@RequiredArgsConstructor
public class ApplicationTenancy_addUser {

    public static class DomainEvent
            extends CausewayModuleExtSecmanApplib.ActionDomainEvent<ApplicationTenancy_addUser> {}

    @Inject private ApplicationTenancyRepository applicationTenancyRepository;
    @Inject private ApplicationUserRepository applicationUserRepository;

    private final ApplicationTenancy target;

    @MemberSupport public ApplicationTenancy act(final ApplicationUser applicationUser) {
        applicationTenancyRepository.setTenancyOnUser(target, applicationUser);
        return target;
    }

    @MemberSupport public List<? extends ApplicationUser> autoCompleteApplicationUser(
            @MinLength(1)
            final String search) {

        final Collection<? extends ApplicationUser> matchingSearch = applicationUserRepository.find(search);
        final List<? extends ApplicationUser> list = _Lists.newArrayList(matchingSearch);
        list.removeAll(applicationUserRepository.findByTenancy(target));
        return list;
    }

}
