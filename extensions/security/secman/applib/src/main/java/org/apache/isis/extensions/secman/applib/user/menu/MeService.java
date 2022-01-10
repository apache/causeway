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

import javax.inject.Inject;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Domain;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ObjectSupport;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.userui.UserMenu;
import org.apache.isis.core.config.IsisConfiguration;
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
    public static abstract class ActionDomainEvent<T> extends IsisModuleExtSecmanApplib.ActionDomainEvent<T> {}

    final ApplicationUserRepository applicationUserRepository;
    final UserService userService;
    final javax.inject.Provider<QueryResultsCache> queryResultsCacheProvider;


    @ObjectSupport public String iconName() {
        return "applicationUser";
    }


    @Action(
            domainEvent = me.ActionEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(
            cssClassFa = "fa-user",
            describedAs = "Looks up ApplicationUser entity corresponding to your user account",
            //group = "Security",
            sequence = "100"
            )
    public class me{

        public class ActionEvent extends ActionDomainEvent<me> {}

        @MemberSupport public ApplicationUser act() {
            return queryResultsCacheProvider.get().execute(
                    (Callable<ApplicationUser>) this::fetchMe, MeService.class, "me");
        }

        @Domain.Exclude protected ApplicationUser fetchMe() {
            final String myName = userService.currentUserNameElseNobody();
            return fetchMe(myName);
        }

        @Domain.Exclude protected ApplicationUser fetchMe(final String myName) {
            return applicationUserRepository.findOrCreateUserByUsername(myName);
        }

    }


    @Component
    @RequiredArgsConstructor
    public static class UserMenuMeActionAdvisor {

        final IsisConfiguration isisConfiguration;

        @EventListener(UserMenu.me.ActionEvent.class)
        public void on(final UserMenu.me.ActionEvent event) {
            switch (isisConfiguration.getExtensions().getSecman().getUserMenuMeActionPolicy()) {
                case HIDE:
                    event.hide();
                    break;
                case DISABLE:
                    event.disable("Use security manager's action to view the current user");
                    break;
                case ENABLE:
                    break;
            }
        }
    }

}
