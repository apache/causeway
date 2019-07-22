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

import javax.inject.Inject;

import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.pages.PageNavigationService;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.AccountManagementPageAbstract;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.panel.SignInPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.util.string.Strings;

/**
 * Boilerplate, pick up our HTML and CSS.
 */
public class WicketSignInPage extends AccountManagementPageAbstract {

    private static final long serialVersionUID = 1L;

    public static final String ISIS_VIEWER_WICKET_REMEMBER_ME_SUPPRESS = "isis.viewer.wicket.rememberMe.suppress";
    public static final String ISIS_VIEWER_WICKET_SUPPRESS_SIGN_UP = "isis.viewer.wicket.suppressSignUp";
    public static final String ISIS_VIEWER_WICKET_SUPPRESS_PASSWORD_RESET = "isis.viewer.wicket.suppressPasswordReset";
    public static final String ISIS_VIEWER_WICKET_CLEAR_ORIGINAL_DESTINATION = "isis.viewer.wicket.clearOriginalDestination";

    public WicketSignInPage(final PageParameters parameters) {
        this(parameters, getAndClearExceptionModelIfAny());
    }

    public WicketSignInPage(final PageParameters parameters, ExceptionModel exceptionModel) {
        super(parameters, exceptionModel);

        if (AuthenticatedWebSession.exists() && AuthenticatedWebSession.get().isSignedIn()) {
            pageNavigationService.restartAt(PageType.HOME);
        }
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        addSignInPanel();

        checkForSuccessFeedback();
    }

    /**
     * Checks for a cookie with name {@value #FEEDBACK_COOKIE_NAME} that is
     * used as a temporary container for stateless session scoped success feedback
     * messages.
     */
    private void checkForSuccessFeedback() {
        CookieUtils cookieUtils = new CookieUtils();
        String successFeedback = cookieUtils.load(FEEDBACK_COOKIE_NAME);
        if (!Strings.isEmpty(successFeedback)) {
            success(successFeedback);
            cookieUtils.remove(FEEDBACK_COOKIE_NAME);
        }
    }

    protected SignInPanel addSignInPanel() {

        final boolean rememberMeSuppress = getConfiguration().getBoolean(ISIS_VIEWER_WICKET_REMEMBER_ME_SUPPRESS, false);
        final boolean suppressRememberMe = rememberMeSuppress;

        final boolean suppressSignUpLink = getConfiguration().getBoolean(ISIS_VIEWER_WICKET_SUPPRESS_SIGN_UP, false);
        final boolean suppressPasswordResetLink = getConfiguration().getBoolean(ISIS_VIEWER_WICKET_SUPPRESS_PASSWORD_RESET, false);
        final boolean clearOriginalDestination = getConfiguration().getBoolean(ISIS_VIEWER_WICKET_CLEAR_ORIGINAL_DESTINATION, false);
        final boolean rememberMe = !suppressRememberMe;
        final boolean signUpLink = !suppressSignUpLink;
        final boolean passwordReset = !suppressPasswordResetLink;
        final boolean continueToOriginalDestination = !clearOriginalDestination;
        SignInPanel signInPanel = createSignInPanel("signInPanel", rememberMe, signUpLink, passwordReset, continueToOriginalDestination);
        add(signInPanel);
        return signInPanel;
    }

    protected SignInPanel createSignInPanel(
            final String id,
            final boolean rememberMe,
            final boolean signUpLink,
            final boolean passwordResetLink,
            final boolean continueToOriginalDestination) {
        final SignInPanel signInPanel = new IsisSignInPanel(id, rememberMe, signUpLink, passwordResetLink, continueToOriginalDestination);
        return signInPanel;
    }

    @Inject private PageNavigationService pageNavigationService;
}
