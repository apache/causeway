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
package org.apache.causeway.extensions.secman.applib.user.menu;

import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Domain;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.queryresultscache.QueryResultsCache;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.applib.services.userui.UserMenu;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * @since 2.0 {@index}
 */
@Named(MeService.LOGICAL_TYPE_NAME)
@DomainService(
        nature = NatureOfService.VIEW
)
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.TERTIARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class MeService {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtSecmanApplib.NAMESPACE + ".MeService";

    public static abstract class PropertyDomainEvent<T> extends CausewayModuleExtSecmanApplib.PropertyDomainEvent<MeService, T> {}
    public static abstract class CollectionDomainEvent<T> extends CausewayModuleExtSecmanApplib.CollectionDomainEvent<MeService, T> {}
    public static abstract class ActionDomainEvent<T> extends CausewayModuleExtSecmanApplib.ActionDomainEvent<T> {}

    final ApplicationUserRepository applicationUserRepository;
    final UserService userService;
    final Provider<QueryResultsCache> queryResultsCacheProvider;


    @ObjectSupport public String iconName() {
        return "applicationUser";
    }


    @Action(
            domainEvent = me.ActionDomainEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(
            cssClassFa = "fa-user",
            describedAs = "Looks up ApplicationUser entity corresponding to your user account",
            //group = "Security",
            sequence = "100"
            )
    public class me{

        public class ActionDomainEvent extends MeService.ActionDomainEvent<me> {}

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

        final CausewayConfiguration causewayConfiguration;

        @EventListener(UserMenu.me.ActionDomainEvent.class)
        public void on(final UserMenu.me.ActionDomainEvent event) {
            switch (causewayConfiguration.getExtensions().getSecman().getUserMenuMeActionPolicy()) {
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
