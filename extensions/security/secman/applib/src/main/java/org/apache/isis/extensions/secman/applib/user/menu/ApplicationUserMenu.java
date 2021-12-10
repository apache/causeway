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
package org.apache.isis.extensions.secman.applib.user.menu;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;
import org.apache.isis.extensions.secman.applib.user.app.ApplicationUserManager;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;

@DomainService(
        nature = NatureOfService.VIEW,
        logicalTypeName = ApplicationUserMenu.LOGICAL_TYPE_NAME
)
@DomainServiceLayout(
        named = "Security",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApplicationUserMenu {

    public static final String LOGICAL_TYPE_NAME = IsisModuleExtSecmanApplib.NAMESPACE + ".ApplicationUserMenu";

    public static abstract class ActionDomainEvent<T> extends IsisModuleExtSecmanApplib.ActionDomainEvent<T> { }

    private final ApplicationUserRepository applicationUserRepository;
    private final FactoryService factory;

    // -- ICON

    @ObjectSupport public String iconName() {
        return "applicationUser";
    }

    // -- FIND USERS

    @Action(
            domainEvent = findUsers.ActionEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(sequence = "100.10.2")
    public class findUsers{

        public class ActionEvent
                extends ActionDomainEvent<findUsers> { }

        @MemberSupport public Collection<? extends ApplicationUser> act(
                final @ParameterLayout(named = "Search") String search) {
            return applicationUserRepository.find(search);
        }

    }

    // -- USER MANAGER

    @Action(
            domainEvent = userManager.ActionEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
            sequence = "100.10.3",
            cssClassFa = "user-plus"
    )
    public class userManager{

        public class ActionEvent extends ActionDomainEvent<userManager> { }

        @MemberSupport public ApplicationUserManager act(){
            return factory.viewModel(new ApplicationUserManager());
        }

    }

}
