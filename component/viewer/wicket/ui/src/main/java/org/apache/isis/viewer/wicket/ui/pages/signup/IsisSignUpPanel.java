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

package org.apache.isis.viewer.wicket.ui.pages.signup;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

import javax.inject.Inject;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.isis.applib.services.email.EmailSendingService;
import org.apache.isis.applib.services.email.events.UserCreationEvent;

/**
 * A panel with a form for creation of new users
 */
public class IsisSignUpPanel extends Panel {

    /**
     * Constructor
     *
     * @param id
     *            the component id
     */
    public IsisSignUpPanel(final String id) {
        super(id);

        addOrReplace(new NotificationPanel("feedback"));

        Form<Void> form = new Form<>("signUpForm");
        addOrReplace(form);

        final TextField<String> emailField = new TextField<>("email", Model.of(""));
        emailField.add(EmailAddressValidator.getInstance());
        final PasswordTextField passwordField = new PasswordTextField("password", Model.of(""));
        Button signUpButton = new Button("signUp") {
            @Override
            public void onSubmit() {
                super.onSubmit();

                String email = emailField.getModelObject();
                String password = passwordField.getModelObject();

                emailService.send(new UserCreationEvent(email, password));
            }
        };
        form.add(emailField, passwordField, signUpButton);
    }

    @Inject
    private EmailSendingService emailService;
}
