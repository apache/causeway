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
package org.apache.isis.extensions.secman.applib.role.menu;

import java.util.Collection;

import javax.inject.Inject;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.DomainService;
import org.apache.isis.applib.annotations.DomainServiceLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.NatureOfService;
import org.apache.isis.applib.annotations.ObjectSupport;
import org.apache.isis.applib.annotations.Optionality;
import org.apache.isis.applib.annotations.Parameter;
import org.apache.isis.applib.annotations.ParameterLayout;
import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;
import org.apache.isis.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.isis.extensions.secman.applib.role.dom.ApplicationRoleRepository;

import lombok.RequiredArgsConstructor;

@DomainService(
        nature = NatureOfService.VIEW,
        logicalTypeName = ApplicationRoleMenu.LOGICAL_TYPE_NAME
)
@DomainServiceLayout(
        named = "Security",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApplicationRoleMenu {

    public static final String LOGICAL_TYPE_NAME = IsisModuleExtSecmanApplib.NAMESPACE + ".ApplicationRoleMenu";

    public static abstract class ActionDomainEvent<T> extends IsisModuleExtSecmanApplib.ActionDomainEvent<T> {}

    private final ApplicationRoleRepository applicationRoleRepository;


    @ObjectSupport public String iconName() {
        return "applicationRole";
    }



    @Action(
            domainEvent = findRoles.ActionEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(sequence = "100.20.1")
    public class findRoles {

        public class ActionEvent extends ActionDomainEvent<findRoles> {}

        @MemberSupport public Collection<? extends ApplicationRole> act(
                @Parameter(maxLength = ApplicationRole.Name.MAX_LENGTH)
                @ParameterLayout(named = "Search", typicalLength = ApplicationRole.Name.TYPICAL_LENGTH)
                final String search) {
            return applicationRoleRepository.findNameContaining(search);
        }

    }


    @Action(
            domainEvent = newRole.ActionEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @ActionLayout(sequence = "100.20.2")
    public class newRole{

        public class ActionEvent extends ActionDomainEvent<newRole> {}

        @MemberSupport public ApplicationRole act (
                @Parameter(maxLength = ApplicationRole.Name.MAX_LENGTH)
                @ParameterLayout(named="Name", typicalLength= ApplicationRole.Name.TYPICAL_LENGTH)
                final String name,
                @Parameter(maxLength = ApplicationRole.Description.MAX_LENGTH, optionality = Optionality.OPTIONAL)
                @ParameterLayout(named="Description", typicalLength= ApplicationRole.Description.TYPICAL_LENGTH)
                final String description) {
            return applicationRoleRepository.newRole(name, description);
        }
    }



    @Action(
            domainEvent = allRoles.ActionEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(sequence = "100.20.3")
    public class allRoles {

        public class ActionEvent extends ActionDomainEvent<allRoles> {}

        @MemberSupport public Collection<? extends ApplicationRole> act() {
            return applicationRoleRepository.allRoles();
        }

    }


}
