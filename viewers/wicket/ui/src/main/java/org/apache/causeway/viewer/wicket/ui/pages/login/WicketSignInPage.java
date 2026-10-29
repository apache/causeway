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

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.causeway.viewer.wicket.ui.pages.PageNavigationService;
import org.apache.causeway.viewer.wicket.ui.pages.accmngt.AccountManagementPageAbstract;
import org.apache.causeway.viewer.wicket.ui.pages.accmngt.SuccessFeedbackCookieManager;

import lombok.val;

/**
 * Boilerplate, pick up our HTML and CSS.
 */
public class WicketSignInPage extends AccountManagementPageAbstract {

    private static final long serialVersionUID = 1L;

    public WicketSignInPage(final PageParameters parameters) {
        this(parameters, getAndClearExceptionModelIfAny());
    }

    public WicketSignInPage(final PageParameters parameters, final ExceptionModel exceptionModel) {
        super(parameters, exceptionModel);

        if (AuthenticatedWebSession.exists() && AuthenticatedWebSession.get().isSignedIn()) {
            continueToOriginalDestinationIfAllowed(
                    this,
                    exceptionModel != null,
                    getWicketViewerSettings().isClearOriginalDestination());
            pageNavigationService.restartAt(PageType.HOME);
        }
    }

    /**
     * With external authentication (eg Spring Security), the first request for a protected page
     * (eg a deep link) is intercepted to this sign-in page, because the Wicket session is only
     * signed in once it exists. By the time this page is constructed the session is signed in,
     * so continue to that original destination, if any.
     * <p>
     * Not if reporting an exception, as the original destination may be the failing page
     * (and with external authentication the session remains signed in), causing a redirect loop.
     * <p>
     * Throws a restart exception if continuing; otherwise returns normally.
     */
    static void continueToOriginalDestinationIfAllowed(
            final Component component,
            final boolean reportingException,
            final boolean clearOriginalDestination) {
        if (reportingException
                || clearOriginalDestination) {
            return;
        }
        component.continueToOriginalDestination();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(createSignInPanel());

        SuccessFeedbackCookieManager.drainSuccessFeedback(this::success);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(
                SignInPanelAbstract.getJsForTimezoneSelectDefault()));
    }


    protected CausewaySignInPanel createSignInPanel() {

        final boolean rememberMeSuppress = getWicketViewerSettings().getRememberMe().isSuppress();
        final boolean suppressRememberMe = rememberMeSuppress;

        final boolean suppressSignUpLink = getWicketViewerSettings().isSuppressSignUp();
        final boolean suppressPasswordResetLink = getWicketViewerSettings().isSuppressPasswordReset();
        final boolean clearOriginalDestination = getWicketViewerSettings().isClearOriginalDestination();
        final boolean rememberMe = !suppressRememberMe;
        final boolean signUpLink = !suppressSignUpLink;
        final boolean passwordReset = !suppressPasswordResetLink;
        final boolean continueToOriginalDestination = !clearOriginalDestination;
        val signInPanel =
                new CausewaySignInPanel("signInPanel", rememberMe, signUpLink, passwordReset, continueToOriginalDestination);
        return signInPanel;
    }

    @Inject private PageNavigationService pageNavigationService;
}
