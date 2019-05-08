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

package org.apache.isis.viewer.wicket.ui.pages.accmngt.signup;

import javax.inject.Inject;

import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.pages.PageNavigationService;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.AccountManagementPageAbstract;
import org.apache.isis.viewer.wicket.ui.pages.login.WicketSignInPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * A page for self-registering a new user after confirmation of her email account.
 */
public class RegistrationFormPage extends AccountManagementPageAbstract {

    private static final long serialVersionUID = 1L;

    @Inject private PageNavigationService pageNavigationService;

    public RegistrationFormPage(final PageParameters parameters) {
        this(parameters, getAndClearExceptionModelIfAny());
    }

    private RegistrationFormPage(final PageParameters parameters, ExceptionModel exceptionModel) {
        super(parameters, exceptionModel);

        boolean suppressSignUp = getConfiguration().getBoolean(WicketSignInPage.ISIS_VIEWER_WICKET_SUPPRESS_SIGN_UP, false);
        if(suppressSignUp) {
            pageNavigationService.interceptAndRestartAt(PageType.SIGN_IN);
        }

    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        addRegistrationFormPanel();
    }

    protected RegistrationFormPanel addRegistrationFormPanel() {
        final RegistrationFormPanel registrationFormPanel = new RegistrationFormPanel("registrationFormPanel");
        addOrReplace(registrationFormPanel);
        return registrationFormPanel;
    }
}
