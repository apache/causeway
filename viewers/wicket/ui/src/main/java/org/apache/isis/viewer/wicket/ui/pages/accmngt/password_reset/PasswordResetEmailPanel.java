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

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.cookies.CookieUtils;
import org.apache.wicket.validation.validator.EmailAddressValidator;

import org.apache.isis.applib.services.userreg.events.PasswordResetEvent;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.AccountManagementPageAbstract;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.EmailAvailableValidator;
import org.apache.isis.viewer.wicket.ui.panels.PanelBase;

import lombok.val;

/**
 * A panel with a form for creation of new users
 */
public class PasswordResetEmailPanel extends PanelBase<Void> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     *
     * @param id
     *            the component id
     */
    public PasswordResetEmailPanel(final String id) {
        super(id);

        val form = new StatelessForm<Void>("signUpForm");
        addOrReplace(form);

        val emailField = new RequiredTextField<String>("email", Model.of(""));
        emailField.setLabel(new ResourceModel("emailLabel"));
        emailField.add(EmailAddressValidator.getInstance());
        emailField.add(EmailAvailableValidator.exists(getCommonContext()));

        val formGroup = new FormGroup("formGroup", emailField);
        form.add(formGroup);

        formGroup.add(emailField);

        val signUpButton = new Button("passwordResetSubmit") {

            private static final long serialVersionUID = 1L;
            private final RequiredTextField<String> _emailField = emailField;

            @Override
            public void onSubmit() {
                super.onSubmit();
                passwordResetSubmit(_emailField);
            }
        };

        form.add(signUpButton);
    }
    
    private void passwordResetSubmit(final RequiredTextField<String> emailField) {

        String email = emailField.getModelObject();

        String confirmationUrl = super.getEmailVerificationUrlService().createVerificationUrl(PageType.PASSWORD_RESET, email);

        val passwordResetEvent = new PasswordResetEvent(
                email, 
                confirmationUrl,
                getIsisConfiguration().getViewer().getWicket().getApplication().getName());

        boolean emailSent = super.getEmailNotificationService().send(passwordResetEvent);
        if (emailSent) {
            Map<String, String> map = new HashMap<>();
            map.put("email", email);
            IModel<Map<String, String>> model = Model.ofMap(map);
            String emailSentMessage = getString("emailSentMessage", model);

            CookieUtils cookieUtils = new CookieUtils();
            cookieUtils.save(AccountManagementPageAbstract.FEEDBACK_COOKIE_NAME, emailSentMessage);
            super.getPageNavigationService().navigateTo(PageType.SIGN_IN);
        }
    }



}
