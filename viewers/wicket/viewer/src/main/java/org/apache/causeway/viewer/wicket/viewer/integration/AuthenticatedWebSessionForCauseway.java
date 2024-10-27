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
package org.apache.causeway.viewer.wicket.viewer.integration;

import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.clock.VirtualClock;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.session.SessionSubscriber;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.services.user.UserMemento.AuthenticationSource;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.security.authentication.AuthenticationRequestPassword;
import org.apache.causeway.core.security.authentication.manager.AuthenticationManager;
import org.apache.causeway.viewer.wicket.model.causeway.HasAmendableInteractionContext;
import org.apache.causeway.viewer.wicket.model.models.BookmarkedPagesModel;
import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;
import org.apache.causeway.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModel;
import org.apache.causeway.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModelProvider;
import org.apache.causeway.viewer.wicket.ui.pages.BookmarkedPagesModelProvider;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * Viewer-specific implementation of {@link AuthenticatedWebSession}, which
 * delegates to the Causeway' configured {@link AuthenticationManager}, and which
 * also tracks thread usage (so that multiple concurrent requests are all
 * associated with the same session).
 */
@Log4j2
public class AuthenticatedWebSessionForCauseway
extends AuthenticatedWebSession
implements
    BreadcrumbModelProvider,
    BookmarkedPagesModelProvider,
    HasCommonContext,
    HasAmendableInteractionContext {

    private static final long serialVersionUID = 1L;

    public static AuthenticatedWebSessionForCauseway get() {
        return (AuthenticatedWebSessionForCauseway) Session.get();
    }

    /**
     * lazily populated in {@link #getBreadcrumbModel()}
     */
    private BreadcrumbModel breadcrumbModel;
    @Override
    public BreadcrumbModel getBreadcrumbModel() {
        return breadcrumbModel != null
                ? breadcrumbModel
                : (breadcrumbModel = new BreadcrumbModel());
    }

    /**
     * lazily populated in {@link #getBookmarkedPagesModel()}
     */
    private BookmarkedPagesModel bookmarkedPagesModel;
    @Override
    public BookmarkedPagesModel getBookmarkedPagesModel() {
        return bookmarkedPagesModel != null
                ? bookmarkedPagesModel
                : (bookmarkedPagesModel = new BookmarkedPagesModel());
    }

    /**
     * As populated in {@link #signIn(String, String)}.
     */
    private InteractionContext interactionContext;
    private void setInteractionContext(final @Nullable InteractionContext interactionContext) {
        _Assert.assertFalse(
                interactionContext !=null
                 && interactionContext.getUser().isImpersonating(), ()->
                "framework bug: cannot signin with an impersonated user");
        this.interactionContext = interactionContext;
    }

    /**
     * If there is an {@link InteractionContext} already (primed)
     * (as some authentication mechanisms setup in filters,
     * eg SpringSecurityFilter), then just use it.
     * <p>
     * However, for authorization, the authentication still must pass
     * {@link AuthenticationManager} checks,
     * as done in {@link #getInteractionContext()},
     * which on success also sets the signIn flag.
     * <p>
     * Called by {@link WebRequestCycleForCauseway}.
     */
    public void setPrimedInteractionContext(final @NonNull InteractionContext authentication) {
        this.interactionContext = authentication;
    }

    @Getter
    private UUID sessionGuid;
    private String cachedSessionId;

    /**
     * Optionally the current HttpSession's Id,
     * based on whether such a session is available.
     * @implNote side-effect free, that is,
     * must not create a session if there is none yet
     */
    public Optional<String> getCachedSessionId() {
        if (cachedSessionId == null
                && Session.exists()) {
            cachedSessionId = getId();
        }
        return Optional.ofNullable(cachedSessionId);
    }

    public AuthenticatedWebSessionForCauseway(final Request request) {
        super(request);
        sessionGuid = UUID.randomUUID();
    }

    @Override
    public synchronized boolean authenticate(final String username, final String password) {
        var authenticationRequest = new AuthenticationRequestPassword(username, password);
        authenticationRequest.addRole(UserMemento.AUTHORIZED_USER_ROLE);
        InteractionContext interactionContext = getAuthenticationManager().authenticate(authenticationRequest);
        setInteractionContext(interactionContext);
        if (interactionContext != null) {
            log(SessionSubscriber.Type.LOGIN, username, null);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized void invalidateNow() {

        //
        // similar code in Restful Objects viewer (UserResourceServerside#logout)
        //
        // this needs to be done here because Wicket will expire the HTTP session
        // while the Shiro authenticator uses the session to obtain the details of the
        // principals for it to logout
        //

        getAuthenticationManager().closeSession(
                Optional.ofNullable(interactionContext)
                .map(InteractionContext::getUser)
                .orElse(null));

        super.invalidateNow();

    }

    @Override
    public synchronized void onInvalidate() {

        String userName = null;
        var authentication = getInteractionContext();
        if (authentication != null) {
            userName = authentication.getUser().getName();
        }

        super.onInvalidate();

        var causedBy = RequestCycle.get() != null
                ? SessionSubscriber.CausedBy.USER
                : SessionSubscriber.CausedBy.SESSION_EXPIRATION;

        log(SessionSubscriber.Type.LOGOUT, userName, causedBy);
    }

    @Override
    public void amendInteractionContext(final UnaryOperator<InteractionContext> updater) {
        setInteractionContext(updater.apply(interactionContext));
    }

    /**
     * Returns an {@link InteractionContext} either as authenticated (and then cached on the session subsequently),
     * or taking into account {@link UserService impersonation}.
     * <p>
     * The session must still {@link AuthenticationManager#isSessionValid(InteractionContext) be valid}, though
     * note that this will always be true for externally authenticated users.
     */
     synchronized InteractionContext getInteractionContext() {

        if(interactionContext == null) {
            return null;
        }
        if (Optional.ofNullable(getMetaModelContext())
                .map(HasMetaModelContext::getAuthenticationManager)
                .filter(x -> x.isSessionValid(interactionContext))
                .isEmpty()) {
            return null;
        }
        signIn(true);

        return interactionContext;
    }

    @Override
    public AuthenticationManager getAuthenticationManager() {
        return Optional.ofNullable(getMetaModelContext())
                .map(HasMetaModelContext::getAuthenticationManager)
                .orElse(null);
    }

    /**
     * This is a no-op if the {@link #getInteractionContext() authentication session}'s
     * {@link UserMemento#getAuthenticationSource() source} is
     * {@link AuthenticationSource#EXTERNAL external}
     * (eg as managed by keycloak).
     */
    @Override
    public void invalidate() {
        if(interactionContext !=null
                && interactionContext.getUser().getAuthenticationSource().isExternal()) {
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
        return getInteractionService()
            .currentInteractionContext()
            .map(InteractionContext::getUser)
            .map(user->{
                var roles = new Roles();
                user.streamRoleNames()
                .forEach(roles::add);
                return roles;
            })
            .orElse(null);
    }

    @Override
    public synchronized void detach() {
        if(breadcrumbModel!=null) {
            breadcrumbModel.detach();
        }
        if(bookmarkedPagesModel!=null) {
            bookmarkedPagesModel.detach();
        }
        super.detach();
    }

    @Override
    public void replaceSession() {
        // do nothing here because this will lead to problems with Shiro
        // see https://issues.apache.org/jira/browse/CAUSEWAY-1018
    }

    private void log(
            final SessionSubscriber.Type type,
            final String username,
            final SessionSubscriber.CausedBy causedBy) {

        if(getMetaModelContext()==null) {
            log.warn("Failed to callback SessionLoggingServices due to unavailable MetaModelContext.\n"
                    + "\tEvent Data: type={}, username={}, causedBy={}", type, username, causedBy);
            return;
        }

        var interactionService = getInteractionService();
        var sessionLoggingServices = getSessionLoggingServices();

        final Runnable loggingTask = ()->{

            var now = virtualClock().nowAsJavaUtilDate();
            var httpSessionId = AuthenticatedWebSessionForCauseway.this.getCachedSessionId()
                    .orElse("(none)");

            sessionLoggingServices
            .forEach(sessionLoggingService ->
                sessionLoggingService.log(type, username, now, causedBy, getSessionGuid(), httpSessionId));
        };

        if(interactionService!=null) {
            interactionService.runAnonymous(loggingTask::run);
        } else {
            loggingTask.run();
        }
    }

    protected Can<SessionSubscriber> getSessionLoggingServices() {
        return getServiceRegistry().select(SessionSubscriber.class);
    }

    private VirtualClock virtualClock() {
        try {
            return getServiceRegistry()
                    .lookupService(ClockService.class)
                    .map(ClockService::getClock)
                    .orElseGet(this::nowFallback);
        } catch (Exception e) {
            return nowFallback();
        }
    }

    private VirtualClock nowFallback() {
        return VirtualClock.system();
    }

}
