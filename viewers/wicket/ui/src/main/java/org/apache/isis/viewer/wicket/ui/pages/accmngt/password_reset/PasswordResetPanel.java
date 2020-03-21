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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

import org.apache.isis.applib.services.userreg.UserRegistrationService;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.AccountConfirmationMap;
import org.apache.isis.viewer.wicket.ui.panels.PanelBase;

import lombok.val;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.INotificationMessage;
import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationMessage;

/**
 * A panel with a form for creation of new users
 */
public class PasswordResetPanel extends PanelBase<Void> {

    private static final long serialVersionUID = -2072394926411738664L;

    /**
     * Constructor
     *
     * @param id The component id
     * @param uuid The unique id to find the user's email address
     */
    public PasswordResetPanel(final String id, final String uuid) {
        super(id);

        StatelessForm<Void> form = new StatelessForm<>("passwordResetForm");
        addOrReplace(form);

        final PasswordTextField passwordField = new PasswordTextField("password", Model.of(""));
        passwordField.setLabel(new ResourceModel("passwordLabel"));
        form.add(passwordField);

        final PasswordTextField confirmPasswordField = new PasswordTextField("confirmPassword", Model.of(""));
        confirmPasswordField.setLabel(new ResourceModel("confirmPasswordLabel"));
        form.add(confirmPasswordField);

        form.add(new EqualPasswordInputValidator(passwordField, confirmPasswordField));

        val commonContext = super.getCommonContext();
        
        Button signUpButton = new Button("passwordResetSubmit") {
            private static final long serialVersionUID = -6355836432811022200L;

            @Override
            public void onSubmit() {
                super.onSubmit();

                final String password = confirmPasswordField.getModelObject();

                final AccountConfirmationMap accountConfirmationMap = getApplication().getMetaData(AccountConfirmationMap.KEY);

                val userRegistrationService = 
                        commonContext.lookupServiceElseFail(UserRegistrationService.class);

                Boolean passwordUpdated = getIsisInteractionFactory().callAnonymous(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        String email = accountConfirmationMap.get(uuid);
                        return userRegistrationService.updatePasswordByEmail(email, password);
                    }
                });

                if (passwordUpdated) {
                    accountConfirmationMap.remove(uuid);
                    success(createPasswordChangeSuccessfulMessage());
                } else {
                    error(getString("passwordChangeUnsuccessful"));
                }
            }
        };

        form.add(signUpButton);
    }

    private INotificationMessage createPasswordChangeSuccessfulMessage() {
        Class<? extends Page> signInPage = getPageClassRegistry().getPageClass(PageType.SIGN_IN);
        CharSequence signInUrl = urlFor(signInPage, null);
        Map<String, CharSequence> map = new HashMap<>();
        map.put("signInUrl", signInUrl);
        String passwordChangeSuccessful = getString("passwordChangeSuccessful", Model.ofMap(map));
        NotificationMessage message = new NotificationMessage(Model.of(passwordChangeSuccessful));
        message.escapeModelStrings(false);
        return message;
    }


}


