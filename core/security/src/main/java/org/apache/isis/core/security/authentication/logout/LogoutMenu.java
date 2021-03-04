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

package org.apache.isis.core.security.authentication.logout;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.applib.value.OpenUrlStrategy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.security.authentication.Authentication;
import org.apache.isis.core.security.authentication.AuthenticationContext;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Named(LogoutMenu.OBJECT_TYPE)
@DomainService(objectType = LogoutMenu.OBJECT_TYPE)
@DomainServiceLayout(menuBar = DomainServiceLayout.MenuBar.TERTIARY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class LogoutMenu {

    public static final String OBJECT_TYPE = "isis.security.LogoutMenu"; // referenced by secman seeding
    
    private final List<LogoutHandler> logoutHandler;
    private final AuthenticationContext authenticationTracker;
    private final IsisConfiguration configuration;

    public static class LogoutDomainEvent
        extends IsisModuleApplib.ActionDomainEvent<LogoutMenu> {}

    @Action(
            domainEvent = LogoutDomainEvent.class,
            semantics = SemanticsOf.SAFE
            )
    @ActionLayout(
            cssClassFa = "fa-sign-out-alt"
            )
    @MemberOrder(sequence = "999")
    public Object logout(){
        _NullSafe.stream(logoutHandler)
            .filter(LogoutHandler::isHandlingCurrentThread)
            .forEach(LogoutHandler::logout);
        
        return getRedirect();
    }

    private Object getRedirect() {
        val redirect =  authenticationTracker.currentAuthentication()
        .map(authentication->
            authentication.getType() == Authentication.Type.EXTERNAL
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
    @DomainObject(
            nature = Nature.VIEW_MODEL, 
            objectType = LoginRedirect.OBJECT_TYPE)  
    public static class LoginRedirect {
        public final static String OBJECT_TYPE = "isis.security.LoginRedirect";
    }
    
    private LocalResourcePath createLogoutRedirect() {
        val logoutRedirect = "/logout"; 
        
        //TODO make this a config option (or use the spring option, if there is any)
        //configuration.getSecurity().getSpring().getLogoutRedirect(); 
        
        return new LocalResourcePath(logoutRedirect, OpenUrlStrategy.SAME_WINDOW);
    }
    
    
}

