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
package org.apache.isis.extensions.secman.api.user.menu;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.user.app.ApplicationUserManager;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = ApplicationUserMenu.OBJECT_TYPE
        )
@DomainServiceLayout(
        named = "Security",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
    )
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApplicationUserMenu {

    public static final String OBJECT_TYPE = IsisModuleExtSecmanApi.NAMESPACE + ".ApplicationUserMenu";

    public static abstract class ActionDomainEvent
        extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationUserMenu> { }

    private final ApplicationUserRepository applicationUserRepository;
    private final FactoryService factory;


    public String iconName() {
        return "applicationUser";
    }



    public static class FindUsersByNameDomainEvent
        extends ActionDomainEvent { }

    @Action(
            domainEvent = FindUsersByNameDomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(sequence = "100.10.2")
    public Collection<? extends ApplicationUser> findUsers(
            final @ParameterLayout(named = "Search") String search) {
        return applicationUserRepository.find(search);
    }



    public static class UserManagerDomainEvent extends ActionDomainEvent { }

    @Action(
            domainEvent = UserManagerDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
            sequence = "100.10.3",
            cssClassFa = "user-plus"
    )
    public ApplicationUserManager userManager() {
        return factory.viewModel(new ApplicationUserManager());
    }

}
