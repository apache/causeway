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

package org.apache.isis.viewer.wicket.viewer.integration;

import java.util.Date;
import java.util.Objects;

import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.session.SessionLoggingService;
import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.core.security.authentication.AuthenticationRequest;
import org.apache.isis.core.security.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.viewer.wicket.model.models.BookmarkedPagesModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModel;
import org.apache.isis.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModelProvider;
import org.apache.isis.viewer.wicket.ui.pages.BookmarkedPagesModelProvider;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.core.webapp.wormhole.AuthenticationSessionWormhole;

import lombok.Getter;
import lombok.val;

/**
 * Viewer-specific implementation of {@link AuthenticatedWebSession}, which
 * delegates to the Isis' configured {@link AuthenticationManager}, and which
 * also tracks thread usage (so that multiple concurrent requests are all
 * associated with the same session).
 */
public class AuthenticatedWebSessionForIsis extends AuthenticatedWebSession 
implements BreadcrumbModelProvider, BookmarkedPagesModelProvider, IsisWebAppCommonContext.Delegating {

    private static final long serialVersionUID = 1L;

    public static final String USER_ROLE = "org.apache.isis.viewer.wicket.roles.USER";

    public static AuthenticatedWebSessionForIsis get() {
        return (AuthenticatedWebSessionForIsis) Session.get();
    }

    @Getter protected transient IsisWebAppCommonContext commonContext; 
    
    private BreadcrumbModel breadcrumbModel;
    private BookmarkedPagesModel bookmarkedPagesModel;

    /**
     * As populated in {@link #signIn(String, String)}.
     *
     * <p>
     * However, if a valid session has been set up previously and stored in {@link AuthenticationSessionWormhole}, then
     * it will be used instead.
     * </p>
     */
    private AuthenticationSession authenticationSession;

    public AuthenticatedWebSessionForIsis(Request request) {
        super(request);
    }

    public void init(IsisWebAppCommonContext commonContext) {
        this.commonContext = commonContext;
        bookmarkedPagesModel = new BookmarkedPagesModel(commonContext);
        breadcrumbModel = new BreadcrumbModel(commonContext);

    }

    @Override
    public synchronized boolean authenticate(final String username, final String password) {
        AuthenticationRequest authenticationRequest = new AuthenticationRequestPassword(username, password);
        authenticationRequest.addRole(USER_ROLE);
        this.authenticationSession = getAuthenticationManager().authenticate(authenticationRequest);
        if (this.authenticationSession != null) {
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
        //        org.apache.isis.runtime.authentication.standard.AuthenticationManagerStandard.closeSession(AuthenticationManagerStandard.java:141)

        getAuthenticationManager().closeSession(getAuthenticationSession());
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
        if (getAuthenticationSession() != null) {
            userName = getAuthenticationSession().getUserName();
        }

        log(SessionLoggingService.Type.LOGOUT, userName, causedBy);
    }

    public synchronized AuthenticationSession getAuthenticationSession() {
        final AuthenticationSession authenticationSession = AuthenticationSessionWormhole.sessionByThread.get();
        if (authenticationSession != null) {
            if (getAuthenticationManager().isSessionValid(authenticationSession)) {
                if (this.authenticationSession != null) {
                    if (Objects.equals(authenticationSession.getUserName(), this.authenticationSession.getUserName())) {
                        // ok, same session so far as Wicket is concerned
                        if (isSignedIn()) {
                            // nothing to do...
                        } else {
                            // force as signed in (though not sure if this case can occur)
                            signIn(true);
                            this.authenticationSession = authenticationSession;
                        }
                    } else {
                        // different user name
                        if (isSignedIn()) {
                            // invalidate previous session
                            super.invalidate();
                        }

                        // either way, the current one is now signed in
                        signIn(true);
                        this.authenticationSession = authenticationSession;
                    }
                } else {
                    signIn(true);
                    this.authenticationSession = authenticationSession;
                }
            }
        }
        return this.authenticationSession;
    }

    /**
     * This is a no-op if the {@link #getAuthenticationSession() authentication session}'s
     * {@link AuthenticationSession#getType() type} is {@link AuthenticationSession.Type#EXTERNAL external} (eg as
     * managed by keycloak).
     */
    public void invalidate() {
        if(this.authenticationSession.getType() == AuthenticationSession.Type.EXTERNAL) {
            return;
        }
        // otherwise
        super.invalidate();
    }

    @Override
    public synchronized Roles getRoles() {
        if (!isSignedIn()) {
            return null;
        }

        final Roles roles = new Roles();
        getAuthenticationSession().getRoles().stream()
        .forEach(roles::add);
        return roles;
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
        return commonContext.getAuthenticationManager();
    }

    // /////////////////////////////////////////////////
    // *Provider impl.
    // /////////////////////////////////////////////////

    private void log(
            final SessionLoggingService.Type type,
            final String username,
            final SessionLoggingService.CausedBy causedBy) {


        val isisSessionFactory = getIsisSessionFactory();
        val sessionLoggingService = getSessionLoggingService();

        final Runnable loggingTask = ()->{
            // use hashcode as session identifier, to avoid re-binding http sessions if using Session#getId()
            int sessionHashCode = System.identityHashCode(AuthenticatedWebSessionForIsis.this);
            sessionLoggingService.log(type, username, new Date(Clock.getEpochMillis()), causedBy, Integer.toString(sessionHashCode));
        };

        if(isisSessionFactory!=null) {
            isisSessionFactory.doInSession(loggingTask);
        } else {
            loggingTask.run();
        }

    }

    protected SessionLoggingService getSessionLoggingService() {
        try {
            final SessionLoggingService service = 
                    commonContext.getServiceRegistry().lookupService(SessionLoggingService.class)
                    .orElseGet(SessionLoggingService.Stderr::new);
            return service;
        } catch (Exception e) {
            // fallback to System.err
            return new SessionLoggingService.Stderr();
        }

    }
    
    protected IsisSessionFactory getIsisSessionFactory() {
        return commonContext.lookupServiceElseFail(IsisSessionFactory.class);
    }

    @Override
    public void replaceSession() {
        // do nothing here because this will lead to problems with Shiro
        // see https://issues.apache.org/jira/browse/ISIS-1018
    }



}
