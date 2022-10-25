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
package org.apache.causeway.viewer.wicket.ui.pages.login;

import java.time.ZoneId;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.services.user.UserCurrentSessionTimeZoneHolder;
import org.apache.causeway.applib.services.userreg.EmailNotificationService;
import org.apache.causeway.applib.services.userreg.UserRegistrationService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.viewer.wicket.model.causeway.HasAmendableInteractionContext;
import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistry;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import lombok.NonNull;
import lombok.val;

/**
 * An extension of Wicket's default SignInPanel that provides
 * custom markup, based on Bootstrap, and uses
 * {@link de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel}
 * for Bootstrap styled error messages
 */
public class CausewaySignInPanel extends SignInPanelAbstract {

    private static final long serialVersionUID = 1L;

    @Inject transient InteractionService interactionService;
    @Inject transient ServiceInjector serviceInjector;
    @Inject transient ServiceRegistry serviceRegistry;
    @Inject transient private PageClassRegistry pageClassRegistry;
    @Inject transient private UserCurrentSessionTimeZoneHolder userCurrentSessionTimeZoneHolder;

    transient Can<UserRegistrationService> anyUserRegistrationService;
    transient Can<EmailNotificationService> anyEmailNotificationService;

    private final boolean signUpLink;
    private final boolean passwordResetLink;
    private final boolean isClearOriginalDestination;

    /**
     * Constructor
     *  @param id
     *            the component id
     * @param rememberMe
     *            True if form should include a remember-me checkbox
     * @param signUpLink
     * @param passwordResetLink
     *            True if form should include the password reset link
     * @param continueToOriginalDestination
     */
    public CausewaySignInPanel(
            final String id,
            final boolean rememberMe,
            final boolean signUpLink,
            final boolean passwordResetLink,
            final boolean continueToOriginalDestination) {
        super(id, rememberMe);
        this.signUpLink = signUpLink;
        this.passwordResetLink = passwordResetLink;
        this.isClearOriginalDestination = !continueToOriginalDestination;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        {//inject fields
            anyUserRegistrationService = serviceRegistry.select(UserRegistrationService.class);
            anyEmailNotificationService = serviceRegistry.select(EmailNotificationService.class);
        }

        addOrReplace(new NotificationPanel("feedback"));

        final Component passwordResetLink = addPasswordResetLink();
        final Component signUpLink = addSignUpLink();

        setVisibilityAllowedBasedOnAvailableServices(signUpLink, passwordResetLink);
    }

    @Override
    protected void onSignInSucceeded() {
        if(isClearOriginalDestination) {
            clearOriginalDestination();
        }
        super.onSignInSucceeded();
    }

    @Override
    protected void onSignInRemembered() {
        if(isClearOriginalDestination) {
            clearOriginalDestination();
        }
        super.onSignInRemembered();
    }

    @Override
    protected void storeUserTimeZoneToSession(final @NonNull ZoneId zoneId) {
        userCurrentSessionTimeZoneHolder.setUserTimeZone(zoneId);
        // fail early if not wired up correctly: there must be a session available for storage
        _Assert.assertEquals(zoneId, userCurrentSessionTimeZoneHolder.getUserTimeZone().orElse(null),
                ()->"no session available to store time-zone data");

        // amend authentication/context with time-zone,
        // also propagate user's Locale from current InteractionContext to Wicket's session
        val session = AuthenticatedWebSession.get();
        ((HasAmendableInteractionContext)session).amendInteractionContext(interactionContext->{
            Optional.ofNullable(interactionContext.getUser().getLanguageLocale())
                .ifPresent(session::setLocale);
            return interactionContext.withTimeZone(zoneId);
        });
    }

    @Override
    protected void clearUserTimeZoneFromSession() {
        userCurrentSessionTimeZoneHolder.clearUserTimeZone();
    }

    // -- HELPER

    private BookmarkablePageLink<Void> addPasswordResetLink() {
        return addLink("passwdResetLink", PageType.PASSWORD_RESET, this.passwordResetLink);
    }

    private BookmarkablePageLink<Void> addSignUpLink() {
        return addLink("signUpLink", PageType.SIGN_UP, this.signUpLink);
    }

    private BookmarkablePageLink<Void> addLink(
            final String id,
            final PageType pageType,
            final boolean visibilityAllowed) {

        final BookmarkablePageLink<Void> link;
        if(pageClassRegistry != null) {
            final Class<? extends Page> signUpPageClass = pageClassRegistry.getPageClass(pageType);
            link = new BookmarkablePageLink<>(id, signUpPageClass);
            if(!visibilityAllowed) {
                link.setVisibilityAllowed(false);
            }
        } else {
            // can happen if failed to bootstrap due to metamodel validation errors
            link = new BookmarkablePageLink<>(id, null);
            link.setVisibilityAllowed(false);
        }

        getSignInFormWithTimeZone().addOrReplace(link);
        return link;
    }

    private void setVisibilityAllowedBasedOnAvailableServices(final Component... components) {
        val hasUserRegistrationService = anyUserRegistrationService.isNotEmpty();
        val hasConfiguredEmailNotificationService = anyEmailNotificationService.stream()
                .anyMatch(EmailNotificationService::isConfigured);

        val visibilityAllowed =
                hasUserRegistrationService
                && hasConfiguredEmailNotificationService;

        for (val component: components) {
            if(component.isVisibilityAllowed()) {
                component.setVisibilityAllowed(visibilityAllowed);
            }
        }
    }

}
