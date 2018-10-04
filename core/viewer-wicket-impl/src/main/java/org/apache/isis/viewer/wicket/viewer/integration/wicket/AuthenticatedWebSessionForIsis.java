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

package org.apache.isis.viewer.wicket.viewer.integration.wicket;

import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.session.SessionLoggingService;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.wicket.model.models.BookmarkedPagesModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModelProvider;
import org.apache.isis.viewer.wicket.ui.pages.BookmarkedPagesModelProvider;
import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * Viewer-specific implementation of {@link AuthenticatedWebSession}, which
 * delegates to the Isis' configured {@link AuthenticationManager}, and which
 * also tracks thread usage (so that multiple concurrent requests are all
 * associated with the same session).
 */
public class AuthenticatedWebSessionForIsis extends AuthenticatedWebSession implements BreadcrumbModelProvider, BookmarkedPagesModelProvider {

    private static final long serialVersionUID = 1L;

    public static final String USER_ROLE = "org.apache.isis.viewer.wicket.roles.USER";

    public static AuthenticatedWebSessionForIsis get() {
        return (AuthenticatedWebSessionForIsis) Session.get();
    }

    private final BookmarkedPagesModel bookmarkedPagesModel = new BookmarkedPagesModel();
    private final BreadcrumbModel breadcrumbModel = new BreadcrumbModel();

    private AuthenticationSession authenticationSession;

    public AuthenticatedWebSessionForIsis(final Request request) {
        super(request);
    }

    @Override
    public synchronized boolean authenticate(final String username, final String password) {
        AuthenticationRequest authenticationRequest = new AuthenticationRequestPassword(username, password);
        authenticationRequest.setRoles(Arrays.asList(USER_ROLE));
        authenticationSession = getAuthenticationManager().authenticate(authenticationRequest);
        if (authenticationSession != null) {
            log(SessionLoggingService.Type.LOGIN, username, null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized void invalidateNow() {

        // similar code in Restful Objects viewer (UserResourceServerside#logout)
        // this needs to be done here because Wicket will expire the HTTP session
        // while the Shiro authenticator uses the session to obtain the details of the principals for it to logout
        //
        //        org.apache.catalina.session.StandardSession.getAttribute(StandardSession.java:1195)
        //        org.apache.catalina.session.StandardSessionFacade.getAttribute(StandardSessionFacade.java:108)
        //        org.apache.shiro.web.session.HttpServletSession.getAttribute(HttpServletSession.java:146)
        //        org.apache.shiro.session.ProxiedSession.getAttribute(ProxiedSession.java:121)
        //        org.apache.shiro.subject.support.DelegatingSubject.getRunAsPrincipalsStack(DelegatingSubject.java:469)
        //        org.apache.shiro.subject.support.DelegatingSubject.getPrincipals(DelegatingSubject.java:153)
        //        org.apache.shiro.mgt.DefaultSecurityManager.logout(DefaultSecurityManager.java:547)
        //        org.apache.shiro.subject.support.DelegatingSubject.logout(DelegatingSubject.java:363)
        //        org.apache.isis.security.shiro.ShiroAuthenticatorOrAuthorizor.logout(ShiroAuthenticatorOrAuthorizor.java:179)
        //        org.apache.isis.core.runtime.authentication.standard.AuthenticationManagerStandard.closeSession(AuthenticationManagerStandard.java:141)

        getAuthenticationManager().closeSession(authenticationSession);
        getIsisSessionFactory().closeSession();

        super.invalidateNow();
    }

    @Override
    public synchronized void onInvalidate() {
        super.onInvalidate();

        SessionLoggingService.CausedBy causedBy = RequestCycle.get() != null
                ? SessionLoggingService.CausedBy.USER
                        : SessionLoggingService.CausedBy.SESSION_EXPIRATION;

        String userName = null;
        if (authenticationSession != null) {
            userName = authenticationSession.getUserName();
        }

        log(SessionLoggingService.Type.LOGOUT, userName, causedBy);
    }

    public synchronized AuthenticationSession getAuthenticationSession() {
        return authenticationSession;
    }

    @Override
    public synchronized Roles getRoles() {
        if (!isSignedIn()) {
            return null;
        }
        final List<String> roles = authenticationSession.getRoles();
        return new Roles(roles.toArray(new String[roles.size()]));
    }

    @Override
    public synchronized void detach() {
        breadcrumbModel.detach();
        super.detach();
    }

    // /////////////////////////////////////////////////
    // Breadcrumbs and Bookmarks support
    // /////////////////////////////////////////////////

    @Override
    public BreadcrumbModel getBreadcrumbModel() {
        return breadcrumbModel;
    }

    @Override
    public BookmarkedPagesModel getBookmarkedPagesModel() {
        return bookmarkedPagesModel;
    }


    // /////////////////////////////////////////////////
    // Dependencies
    // /////////////////////////////////////////////////

    protected AuthenticationManager getAuthenticationManager() {
        return getIsisSessionFactory().getAuthenticationManager();
    }

    // /////////////////////////////////////////////////
    // *Provider impl.
    // /////////////////////////////////////////////////

    private void log(
            final SessionLoggingService.Type type,
            final String username,
            final SessionLoggingService.CausedBy causedBy) {


        final IsisSessionFactory isisSessionFactory = getIsisSessionFactoryIfAny();
        final SessionLoggingService sessionLoggingService = getSessionLoggingService();

        final Runnable loggingTask = ()->{
            // use hashcode as session identifier, to avoid re-binding http sessions if using Session#getId()
            int sessionHashCode = System.identityHashCode(AuthenticatedWebSessionForIsis.this);
            sessionLoggingService.log(type, username, Clock.getTimeAsDateTime().toDate(), causedBy, Integer.toString(sessionHashCode));
        };

        if(isisSessionFactory!=null) {
            isisSessionFactory.doInSession(loggingTask);
        } else {
            loggingTask.run();
        }

    }

    protected @NotNull SessionLoggingService getSessionLoggingService() {
        try {
            final SessionLoggingService service = getIsisSessionFactory().getServicesInjector()
                    .lookupService(SessionLoggingService.class)
                    .orElseGet(SessionLoggingService.Stderr::new);
            return service;
        } catch (Exception e) {
            // fallback to System.err
            return new SessionLoggingService.Stderr();
        }

    }

    @Override
    public synchronized void replaceSession() {
        // do nothing here because this will lead to problems with Shiro
        // see https://issues.apache.org/jira/browse/ISIS-1018
    }

    // -- HELPER

    private IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

    private IsisSessionFactory getIsisSessionFactoryIfAny() {
        try {
            return getIsisSessionFactory();
        } catch (Exception e) {
            return null;
        }
    }


}
