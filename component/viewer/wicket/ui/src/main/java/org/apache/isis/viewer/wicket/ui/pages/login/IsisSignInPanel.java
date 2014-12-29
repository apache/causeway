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

package org.apache.isis.viewer.wicket.ui.pages.login;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

import javax.inject.Inject;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authentication.panel.SignInPanel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.isis.applib.services.userreg.UserRegistrationService;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;

/**
 * An extension of Wicket's default SignInPanel that provides
 * custom markup, based on Bootstrap, and uses
 * {@link de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel}
 * to Bootstrap styled error messages
 */
public class IsisSignInPanel extends SignInPanel {

    private final boolean clearOriginalDestination;
    private final boolean forgotPassword;

    /**
     * Constructor
     *
     * @param id
     *            the component id
     * @param rememberMe
     *            True if form should include a remember-me checkbox
     * @param continueToOriginalDestination
     *            A flag indicating whether to continue to the originally requested destination
     */
    public IsisSignInPanel(
            final String id,
            final boolean rememberMe,
            final boolean forgotPassword,
            final boolean continueToOriginalDestination) {
        super(id, rememberMe);
        this.forgotPassword = forgotPassword;
        this.clearOriginalDestination = !continueToOriginalDestination;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        addSignUpLink("signUpLink");

        addNotificationPanel("feedback");
    }

    protected void addNotificationPanel(String id) {
        addOrReplace(new NotificationPanel(id));
    }

    protected void addSignUpLink(String id) {
        // TODO ISIS-987 is this the correct way to check for the service availability here ? Open a session, etc.
        IsisContext.openSession(new InitialisationSession());
        Class<? extends Page> signUpPageClass = pageClassRegistry.getPageClass(PageType.SIGN_UP);
        BookmarkablePageLink<Void> signUpLink = new BookmarkablePageLink<>(id, signUpPageClass);
        final UserRegistrationService userRegistrationService = IsisContext.getPersistenceSession().getServicesInjector().lookupService(UserRegistrationService.class);
        signUpLink.setVisibilityAllowed(true);//userRegistrationService != null);
        IsisContext.closeSession();

        getSignInForm().addOrReplace(signUpLink);
    }

    private MarkupContainer getSignInForm() {
        return (MarkupContainer) get("signInForm");
    }

    @Override
    protected void onSignInSucceeded() {

        if(clearOriginalDestination) {
            clearOriginalDestination();
        }
        super.onSignInSucceeded();
    }

    @Override
    protected void onSignInRemembered() {
        if(clearOriginalDestination) {
            clearOriginalDestination();
        }
        super.onSignInRemembered();
    }

    @Inject
    private PageClassRegistry pageClassRegistry;
}
