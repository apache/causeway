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
package org.apache.isis.extensions.security.manager.jdo.dom.user;

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
import org.apache.isis.applib.value.Password;
import org.apache.isis.extensions.security.manager.api.SecurityModule;
import org.apache.isis.extensions.security.manager.api.SecurityRealmCharacteristic;
import org.apache.isis.extensions.security.manager.api.SecurityRealmService;
import org.apache.isis.extensions.security.manager.jdo.dom.role.ApplicationRole;
import org.apache.isis.extensions.security.manager.jdo.dom.role.ApplicationRoleRepository;
import org.apache.isis.extensions.security.manager.jdo.seed.scripts.IsisModuleSecurityRegularUserRoleAndPermissions;

import lombok.val;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "isissecurity.ApplicationUserMenu"
)
@DomainServiceLayout(
        named = "Security",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
public class ApplicationUserMenu {
    
    
    public static abstract class PropertyDomainEvent<T> 
    extends SecurityModule.PropertyDomainEvent<ApplicationUserMenu, T> {
        private static final long serialVersionUID = 1L;
    }

    public static abstract class CollectionDomainEvent<T>
    extends SecurityModule.CollectionDomainEvent<ApplicationUserMenu, T> {
        private static final long serialVersionUID = 1L;
    }

    public static abstract class ActionDomainEvent 
    extends SecurityModule.ActionDomainEvent<ApplicationUserMenu> {
        private static final long serialVersionUID = 1L;
    }

    public static class FindUsersByNameDomainEvent
    extends ActionDomainEvent {
        private static final long serialVersionUID = 1L;
    }

    public String iconName() {
        return "applicationUser";
    }

    @Action(
            domainEvent = FindUsersByNameDomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @MemberOrder(sequence = "100.10.2")
    public List<ApplicationUser> findUsers(
            final @ParameterLayout(named = "Search") String search) {
        return applicationUserRepository.find(search);
    }

    public static class NewDelegateUserDomainEvent extends ActionDomainEvent {
        private static final long serialVersionUID = 1L;
    }

    @Action(
            domainEvent = NewDelegateUserDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT
    )
    @MemberOrder(sequence = "100.10.3")
    public ApplicationUser newDelegateUser(
            @Parameter(maxLength = ApplicationUser.MAX_LENGTH_USERNAME)
            @ParameterLayout(named = "Name")
            final String username,
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named = "Initial role")
            final ApplicationRole initialRole,
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named = "Enabled?")
            final Boolean enabled) {
        return applicationUserRepository.newDelegateUser(username, initialRole, enabled);
    }

    public boolean hideNewDelegateUser() {
        return hasNoDelegateAuthenticationRealm();
    }

    public ApplicationRole default1NewDelegateUser() {
        return applicationRoleRepository.findByNameCached(IsisModuleSecurityRegularUserRoleAndPermissions.ROLE_NAME);
    }

    public static class NewLocalUserDomainEvent extends ActionDomainEvent {
        private static final long serialVersionUID = 1L;
    }

    @Action(
            domainEvent = NewLocalUserDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(sequence = "100.10.4")
    public ApplicationUser newLocalUser(
            @Parameter(maxLength = ApplicationUser.MAX_LENGTH_USERNAME)
            @ParameterLayout(named = "Name")
            final String username,
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named = "Password")
            final Password password,
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named = "Repeat password")
            final Password passwordRepeat,
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named = "Initial role")
            final ApplicationRole initialRole,
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named = "Enabled?")
            final Boolean enabled,
            @Parameter(optionality = Optionality.OPTIONAL)
            @ParameterLayout(named = "Email Address")
            final String emailAddress) {
        return applicationUserRepository.newLocalUser(username, password, passwordRepeat, initialRole, enabled, emailAddress);
    }

    public String validateNewLocalUser(
            final String username,
            final Password password,
            final Password passwordRepeat,
            final ApplicationRole initialRole,
            final Boolean enabled,
            final String emailAddress) {
        return applicationUserRepository.validateNewLocalUser(username, password, passwordRepeat, initialRole, enabled, emailAddress);
    }

    public ApplicationRole default3NewLocalUser() {
        return applicationRoleRepository.findByNameCached(IsisModuleSecurityRegularUserRoleAndPermissions.ROLE_NAME);
    }

    public static class AllUsersDomainEvent extends ActionDomainEvent {
        private static final long serialVersionUID = 1L;
    }

    @Action(
            domainEvent = AllUsersDomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @MemberOrder(sequence = "100.10.5")
    public List<ApplicationUser> allUsers() {
        return applicationUserRepository.allUsers();
    }

    private boolean hasNoDelegateAuthenticationRealm() {
    	val realm = securityRealmService.getCurrentRealm();
        return realm == null || !realm.getCharacteristics().contains(SecurityRealmCharacteristic.DELEGATING);
    }

    // -- DEPENDENCIES
    
    @Inject ApplicationRoleRepository applicationRoleRepository;
    @Inject ApplicationUserRepository applicationUserRepository;
    @Inject SecurityRealmService securityRealmService;


}
