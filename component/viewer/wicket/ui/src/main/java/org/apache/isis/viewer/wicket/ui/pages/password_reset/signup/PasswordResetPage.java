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

package org.apache.isis.viewer.wicket.ui.pages.password_reset.signup;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.isis.applib.services.userreg.UserRegistrationService;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.pages.AccountManagementPageAbstract;

/**
 * Boilerplate, pick up our HTML and CSS.
 */
public class PasswordResetPage extends AccountManagementPageAbstract {
    
    private static final long serialVersionUID = 1L;

    public PasswordResetPage(final PageParameters parameters) {
        this(parameters, getAndClearExceptionModelIfAny());
    }

    public PasswordResetPage(final PageParameters parameters, ExceptionModel exceptionModel) {
        super(parameters, exceptionModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        StringValue uuidValue = getPageParameters().get(0);
        if (uuidValue.isEmpty()) {
            addPasswordResetPanel("passwordResetPanel");
        } else {
            String uuid = uuidValue.toString();
            AccountConfirmationMap accountConfirmationMap = getApplication().getMetaData(AccountConfirmationMap.KEY);
            String email = accountConfirmationMap.get(uuid);

            // TODO ISIS-987 Show a Panel with password/confirmPassword fields and update the passwd of the user with the given email
//            IsisContext.openSession(new InitialisationSession());
//            final UserRegistrationService userRegistrationService = IsisContext.getPersistenceSession().getServicesInjector().lookupService(UserRegistrationService.class);
//            userRegistrationService.updatePasswordByEmail(email, password);
//            IsisContext.closeSession();
        }
    }

    protected PasswordResetPanel addPasswordResetPanel(String id) {
        final PasswordResetPanel passwordResetPanel = new PasswordResetPanel(id);
        addOrReplace(passwordResetPanel);
        return passwordResetPanel;
    }
}
