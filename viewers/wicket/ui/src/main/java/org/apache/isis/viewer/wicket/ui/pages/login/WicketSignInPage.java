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

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.panel.SignInPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.pages.PageNavigationService;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.AccountManagementPageAbstract;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.SuccessFeedbackCookieManager;

/**
 * Boilerplate, pick up our HTML and CSS.
 */
public class WicketSignInPage extends AccountManagementPageAbstract {

    private static final long serialVersionUID = 1L;

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

        SuccessFeedbackCookieManager.drainSuccessFeedback(this::success);
    }


    protected SignInPanel addSignInPanel() {


        final boolean rememberMeSuppress = getConfiguration().getViewer().getWicket().getRememberMe().isSuppress();
        final boolean suppressRememberMe = rememberMeSuppress;

        final boolean suppressSignUpLink = getConfiguration().getViewer().getWicket().isSuppressSignUp();
        final boolean suppressPasswordResetLink = getConfiguration().getViewer().getWicket().isSuppressPasswordReset();
        final boolean clearOriginalDestination = getConfiguration().getViewer().getWicket().isClearOriginalDestination();
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
