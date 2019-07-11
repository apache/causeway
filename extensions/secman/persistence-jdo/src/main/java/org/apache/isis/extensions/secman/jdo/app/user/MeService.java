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
package org.apache.isis.extensions.secman.jdo.app.user;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.extensions.secman.api.SecurityModule;
import org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser;
import org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUserRepository;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "isissecurity.MeService"
)
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.TERTIARY
)
public class MeService {

    public static abstract class PropertyDomainEvent<T> extends SecurityModule.PropertyDomainEvent<MeService, T> {
		private static final long serialVersionUID = 1L;}

    public static abstract class CollectionDomainEvent<T> extends SecurityModule.CollectionDomainEvent<MeService, T> {
		private static final long serialVersionUID = 1L;}

    public static abstract class ActionDomainEvent extends SecurityModule.ActionDomainEvent<MeService> {
		private static final long serialVersionUID = 1L;}

    // -- iconName
    public String iconName() {
        return "applicationUser";
    }

    // -- me (action)
    public static class MeDomainEvent extends ActionDomainEvent {
		private static final long serialVersionUID = 1L;}

    @Action(
            domainEvent = MeDomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            cssClassFa = "fa-user",
            describedAs = "Looks up ApplicationUser entity corresponding to your user account"
    )
    @MemberOrder(name = "Security", sequence = "100")
    public ApplicationUser me() {
        return queryResultsCache.execute(new Callable<ApplicationUser>() {
            @Override
            public ApplicationUser call() throws Exception {
                return doMe();
            }
        }, MeService.class, "me");
    }

    protected ApplicationUser doMe() {
        final String myName = userService.getUser().getName();
        return applicationUserRepository.findOrCreateUserByUsername(myName);
    }

    protected ApplicationUser doMe(final String myName) {
        return applicationUserRepository.findOrCreateUserByUsername(myName);
    }

    // -- DEPENDENCIES
    @Inject ApplicationUserRepository applicationUserRepository;
    @Inject UserService userService;
    @Inject QueryResultsCache queryResultsCache;
    

}
