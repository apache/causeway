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
package org.apache.isis.extensions.secman.api.tenancy.dom.mixins;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancy;
import org.apache.isis.extensions.secman.api.tenancy.dom.mixins.ApplicationTenancy_delete.DomainEvent;
import org.apache.isis.extensions.secman.api.tenancy.dom.ApplicationTenancyRepository;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserRepository;
import org.apache.isis.extensions.secman.api.user.dom.mixins.ApplicationUser_updateAtPath;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(domainEvent =
        DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE)
@ActionLayout(sequence = "1")
@RequiredArgsConstructor
public class ApplicationTenancy_delete {

    public static class DomainEvent
            extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationTenancy_delete> {}

    @Inject private ApplicationTenancyRepository applicationTenancyRepository;
    @Inject private ApplicationUserRepository applicationUserRepository;
    @Inject private FactoryService factoryService;
    @Inject private RepositoryService repository;

    private final ApplicationTenancy target;


    @MemberSupport
    public Collection<? extends ApplicationTenancy> act() {
        for (val user : applicationUserRepository.findByTenancy(target)) {
            val updateAtPathMixin = factoryService.mixin(ApplicationUser_updateAtPath.class, user);
            updateAtPathMixin.act(null);
        }
        repository.removeAndFlush(this);
        return applicationTenancyRepository.allTenancies();
    }

}
