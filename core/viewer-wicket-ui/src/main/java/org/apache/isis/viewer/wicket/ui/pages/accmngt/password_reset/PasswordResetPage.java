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

package org.apache.isis.viewer.wicket.ui.pages.accmngt.password_reset;

import java.util.concurrent.Callable;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;

import org.apache.isis.applib.services.userreg.UserRegistrationService;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.AccountConfirmationMap;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.AccountManagementPageAbstract;
import org.apache.isis.viewer.wicket.ui.pages.login.WicketSignInPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

/**
 * A page used for resetting the password of an user.
 */
public class PasswordResetPage extends AccountManagementPageAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_CONTENT_PANEL = "passwordResetPanel";

    public PasswordResetPage(final PageParameters parameters) {
        this(parameters, getAndClearExceptionModelIfAny());
    }

    private PasswordResetPage(final PageParameters parameters, ExceptionModel exceptionModel) {
        super(parameters, exceptionModel);

        boolean suppressPasswordResetLink = getConfiguration().getBoolean(WicketSignInPage.ISIS_VIEWER_WICKET_SUPPRESS_PASSWORD_RESET, false);
        if(suppressPasswordResetLink) {
            throw new RestartResponseAtInterceptPageException(WicketSignInPage.class);
        }
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        add(new NotificationPanel("feedback"));

        StringValue uuidValue = getPageParameters().get(0);
        if (uuidValue.isEmpty()) {
            addPasswordResetEmailPanel(ID_CONTENT_PANEL);
        } else {
            String uuid = uuidValue.toString();

            AccountConfirmationMap accountConfirmationMap = getApplication().getMetaData(AccountConfirmationMap.KEY);
            final String email = accountConfirmationMap.get(uuid);
            if (Strings.isEmpty(email)) {
                error(getString("passwordResetExpiredOrInvalidToken"));
                addOrReplace(addPasswordResetEmailPanel(ID_CONTENT_PANEL));
            } else {
                Boolean emailExists = getIsisSessionFactory().doInSession(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        UserRegistrationService userRegistrationService = getIsisSessionFactory()
                                .getCurrentSession().getPersistenceSession().getServicesInjector()
                                .lookupServiceElseFail(UserRegistrationService.class);
                        return userRegistrationService.emailExists(email);
                    }
                });
                if (!emailExists) {
                    error(getString("noUserForAnEmailValidToken"));
                    addOrReplace(addPasswordResetEmailPanel(ID_CONTENT_PANEL));
                } else {
                    addPasswordResetPanel(ID_CONTENT_PANEL, uuid);
                }
            }
        }
    }

    /**
     * Shows a panel with password reset form fields.
     *
     * @param id The component id
     * @param uuid A unique id used to identify the email of the user whose password will be reset
     * @return A panel with "password reset" form fields
     */
    protected PasswordResetPanel addPasswordResetPanel(String id, String uuid) {
        final PasswordResetPanel passwordResetPanel = new PasswordResetPanel(id, uuid);
        addOrReplace(passwordResetPanel);
        return passwordResetPanel;
    }

    /**
     * Shows a panel where where the user has to provide her email address.
     * An email with unique url will be sent to this email address. Once clicked
     * {@link #addPasswordResetPanel(String, String)} will be used to actually
     * change the password
     *
     * @param id The component id
     * @return A panel with "send email for password reset" functionality
     */
    protected PasswordResetEmailPanel addPasswordResetEmailPanel(String id) {
        final PasswordResetEmailPanel passwordResetEmailPanel = new PasswordResetEmailPanel(id);
        addOrReplace(passwordResetEmailPanel);
        return passwordResetEmailPanel;
    }
}
