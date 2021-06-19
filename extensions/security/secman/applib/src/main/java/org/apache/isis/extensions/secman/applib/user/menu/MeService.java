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

import java.util.concurrent.Callable;

import javax.annotation.Priority;
import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * @since 2.0 {@index}
 */
@DomainService(
        nature = NatureOfService.VIEW,
        logicalTypeName = MeService.LOGICAL_TYPE_NAME
)
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.TERTIARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class MeService {

    public static final String LOGICAL_TYPE_NAME = IsisModuleExtSecmanApplib.NAMESPACE + ".MeService";

    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApplib.PropertyDomainEvent<MeService, T> {}
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApplib.CollectionDomainEvent<MeService, T> {}
    public static abstract class ActionDomainEvent extends IsisModuleExtSecmanApplib.ActionDomainEvent<MeService> {}

    final ApplicationUserRepository applicationUserRepository;
    final UserService userService;
    final javax.inject.Provider<QueryResultsCache> queryResultsCacheProvider;

    // -- iconName
    public String iconName() {
        return "applicationUser";
    }

    // -- me (action)
    public static class MeDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = MeDomainEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(
            cssClassFa = "fa-user",
            describedAs = "Looks up ApplicationUser entity corresponding to your user account",
            //group = "Security",
            sequence = "100"
            )

    public ApplicationUser me() {
        return queryResultsCacheProvider.get().execute(new Callable<ApplicationUser>() {
            @Override
            public ApplicationUser call() throws Exception {
                return doMe();
            }
        }, MeService.class, "me");
    }

    protected ApplicationUser doMe() {
        final String myName = userService.currentUserNameElseNobody();
        return applicationUserRepository.findOrCreateUserByUsername(myName);
    }

    protected ApplicationUser doMe(final String myName) {
        return applicationUserRepository.findOrCreateUserByUsername(myName);
    }




}
