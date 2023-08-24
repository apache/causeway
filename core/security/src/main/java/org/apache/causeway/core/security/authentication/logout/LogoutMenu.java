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
package org.apache.causeway.core.security.authentication.logout;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.iactn.InteractionProvider;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.value.LocalResourcePath;
import org.apache.causeway.applib.value.OpenUrlStrategy;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.security.CausewayModuleCoreSecurity;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Named(LogoutMenu.LOGICAL_TYPE_NAME)
@DomainService(
        nature = NatureOfService.VIEW
)
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.TERTIARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class LogoutMenu {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleCoreSecurity.NAMESPACE + ".LogoutMenu"; // referenced by secman seeding

    final List<LogoutHandler> logoutHandler;
    final InteractionProvider interactionProvider;

    public static class LogoutDomainEvent
        extends CausewayModuleApplib.ActionDomainEvent<LogoutMenu> {}

    @Action(
            domainEvent = LogoutDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            commandPublishing = Publishing.DISABLED,
            executionPublishing = Publishing.DISABLED
    )
    @ActionLayout(
            cssClassFa = "fa-sign-out-alt",
            sequence = "999"
    )
    public Object logout(){
        _NullSafe.stream(logoutHandler)
            .forEach(LogoutHandler::logout);

        return getRedirect();
    }

    private Object getRedirect() {
        val redirect =  interactionProvider.currentInteractionContext()
        .map(InteractionContext::getUser)
        .map(userMemento->
            userMemento.getAuthenticationSource().isExternal()
                ? "logout"
                : "login"
        )
        .orElse("login");
        switch(redirect) {
        case "login": return new LoginRedirect();
        case "logout": return createLogoutRedirect();
        default: return null; // redirects to the homepage
        }
    }

    /** A pseudo model used to redirect to the login page.*/
    @Named(LoginRedirect.LOGICAL_TYPE_NAME)
    @DomainObject(nature = Nature.VIEW_MODEL)
    public static class LoginRedirect {
        public final static String LOGICAL_TYPE_NAME = "causeway.security.LoginRedirect";
    }

    private LocalResourcePath createLogoutRedirect() {
        val logoutRedirect = "/logout";

        //TODO make this a config option (or use the spring option, if there is any)
        //configuration.getSecurity().getSpring().getLogoutRedirect();

        return new LocalResourcePath(logoutRedirect, OpenUrlStrategy.SAME_WINDOW);
    }

}

