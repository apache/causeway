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
package org.apache.isis.extensions.secman.model.dom.role;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.types.DescriptionType;
import org.apache.isis.extensions.secman.api.SecurityModule;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.ApplicationRoleRepository;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "isissecurity.ApplicationRoleMenu"
)
@DomainServiceLayout(
        named = "Security",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
public class  ApplicationRoleMenu {

    // -- domain event classes
    public static class PropertyDomainEvent<T> extends SecurityModule.PropertyDomainEvent<ApplicationRoleMenu, T> {
		private static final long serialVersionUID = 1L;}

    public static abstract class CollectionDomainEvent<T> extends SecurityModule.CollectionDomainEvent<ApplicationRoleMenu, T> {
		private static final long serialVersionUID = 1L;}

    public static abstract class ActionDomainEvent extends SecurityModule.ActionDomainEvent<ApplicationRoleMenu> {
		private static final long serialVersionUID = 1L;}
    

    // -- iconName
    public String iconName() {
        return "applicationRole";
    }
    

    // -- findRoles
    public static class FindRolesDomainEvent extends ActionDomainEvent {
		private static final long serialVersionUID = 1L;}

    @Action(
            domainEvent = FindRolesDomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @MemberOrder(sequence = "100.20.1")
    public List<? extends ApplicationRole> findRoles(
            @Parameter(maxLength = ApplicationRole.MAX_LENGTH_NAME)
            @ParameterLayout(named = "Search", typicalLength = ApplicationRole.TYPICAL_LENGTH_NAME)
            final String search) {
        return applicationRoleRepository.findNameContaining(search);
    }
    

    // -- newRole
    public static class NewRoleDomainEvent extends ActionDomainEvent {
		private static final long serialVersionUID = 1L;}

    @Action(
            domainEvent = NewRoleDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(sequence = "100.20.2")
    public ApplicationRole newRole(
            @Parameter(maxLength = ApplicationRole.MAX_LENGTH_NAME)
            @ParameterLayout(named="Name", typicalLength=ApplicationRole.TYPICAL_LENGTH_NAME)
            final String name,
            @Parameter(maxLength = DescriptionType.Meta.MAX_LEN, optionality = Optionality.OPTIONAL)
            @ParameterLayout(named="Description", typicalLength=ApplicationRole.TYPICAL_LENGTH_DESCRIPTION)
            final String description) {
        return applicationRoleRepository.newRole(name, description);
    }
    

    // -- allRoles
    public static class AllRolesDomainEvent extends ActionDomainEvent {
		private static final long serialVersionUID = 1L;}

    @Action(
            domainEvent = AllRolesDomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @MemberOrder(sequence = "100.20.3")
    public List<? extends ApplicationRole> allRoles() {
        return applicationRoleRepository.allRoles();
    }
    
    // -- DEPENDENCIES

    @Inject ApplicationRoleRepository applicationRoleRepository;
}
