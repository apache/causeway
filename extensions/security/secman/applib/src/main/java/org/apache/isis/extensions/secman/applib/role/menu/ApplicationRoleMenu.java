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

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApi;
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
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApplicationRoleMenu {

    public static final String LOGICAL_TYPE_NAME = IsisModuleExtSecmanApi.NAMESPACE + ".ApplicationRoleMenu";

    // -- domain event classes
    public static class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationRoleMenu, T> {}
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationRoleMenu, T> {}
    public static abstract class ActionDomainEvent extends IsisModuleExtSecmanApi.ActionDomainEvent<ApplicationRoleMenu> {}

    private final ApplicationRoleRepository applicationRoleRepository;


    // -- iconName
    public String iconName() {
        return "applicationRole";
    }


    // -- findRoles
    public static class FindRolesDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = FindRolesDomainEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(sequence = "100.20.1")
    public Collection<? extends ApplicationRole> findRoles(
            @Parameter(maxLength = ApplicationRole.Name.MAX_LENGTH)
            @ParameterLayout(named = "Search", typicalLength = ApplicationRole.Name.TYPICAL_LENGTH)
            final String search) {
        return applicationRoleRepository.findNameContaining(search);
    }


    // -- newRole
    public static class NewRoleDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = NewRoleDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
            )
    @ActionLayout(sequence = "100.20.2")
    public ApplicationRole newRole(
            @Parameter(maxLength = ApplicationRole.Name.MAX_LENGTH)
            @ParameterLayout(named="Name", typicalLength= ApplicationRole.Name.TYPICAL_LENGTH)
            final String name,
            @Parameter(maxLength = ApplicationRole.Description.MAX_LENGTH, optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Description", typicalLength= ApplicationRole.Description.TYPICAL_LENGTH)
            final String description) {
        return applicationRoleRepository.newRole(name, description);
    }


    // -- allRoles
    public static class AllRolesDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = AllRolesDomainEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(sequence = "100.20.3")
    public Collection<? extends ApplicationRole> allRoles() {
        return applicationRoleRepository.allRoles();
    }


}
