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
package org.apache.causeway.extensions.secman.applib.role.menu;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRoleRepository;
import org.apache.causeway.extensions.secman.applib.role.man.ApplicationRoleManager;

import lombok.RequiredArgsConstructor;

/**
 *
 * @since 2.0 {@index}
 */
@Named(ApplicationRoleMenu.LOGICAL_TYPE_NAME)
@DomainService(
        nature = NatureOfService.VIEW
)
@DomainServiceLayout(
        named = "Security",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApplicationRoleMenu {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtSecmanApplib.NAMESPACE + ".ApplicationRoleMenu";

    public static abstract class ActionDomainEvent<T> extends CausewayModuleExtSecmanApplib.ActionDomainEvent<T> {}

    private final ApplicationRoleRepository applicationRoleRepository;
    private final FactoryService factory;


    @ObjectSupport public String iconName() {
        return "applicationRole";
    }


    // -- ROLE MANAGER

    @Action(
            domainEvent = roleManager.ActionDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
            sequence = "100.20.1",
            cssClassFa = "user-tag"
    )
    public class roleManager{

        public class ActionDomainEvent extends ApplicationRoleMenu.ActionDomainEvent<roleManager> { }

        @MemberSupport public ApplicationRoleManager act(){
            return factory.viewModel(new ApplicationRoleManager());
        }

    }

    // -- FIND ROLES

    @Action(
            domainEvent = findRoles.ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(sequence = "100.20.2")
    public class findRoles {

        public class ActionDomainEvent extends ApplicationRoleMenu.ActionDomainEvent<findRoles> {}

        @MemberSupport public Collection<? extends ApplicationRole> act(
                @Parameter(maxLength = ApplicationRole.Name.MAX_LENGTH)
                @ParameterLayout(named = "Search", typicalLength = ApplicationRole.Name.TYPICAL_LENGTH)
                final String search) {
            return applicationRoleRepository.findNameContaining(search);
        }

    }

}
