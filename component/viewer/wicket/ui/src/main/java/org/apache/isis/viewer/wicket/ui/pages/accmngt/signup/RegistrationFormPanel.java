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

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.inject.Inject;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.isis.applib.services.email.EmailNotificationService;
import org.apache.isis.applib.services.email.events.EmailRegistrationEvent;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.AccountConfirmationMap;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.EmailAvailableValidator;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.register.RegisterPage;

/**
 * A panel with a form for creation of new users
 */
public class RegistrationFormPanel extends Panel {

    /**
     * Constructor
     *
     * @param id
     *            the component id
     */
    public RegistrationFormPanel(final String id) {
        super(id);

        addOrReplace(new NotificationPanel("feedback"));

        StatelessForm<Void> form = new StatelessForm<>("signUpForm");
        addOrReplace(form);

        final RequiredTextField<String> emailField = new RequiredTextField<>("email", Model.of(""));
        emailField.setLabel(new ResourceModel("emailLabel"));
        emailField.add(EmailAddressValidator.getInstance());
        emailField.add(EmailAvailableValidator.DOESNT_EXIST);

        FormGroup formGroup = new FormGroup("formGroup", emailField);
        form.add(formGroup);

        formGroup.add(emailField);

        Button signUpButton = new Button("signUp") {
            @Override
            public void onSubmit() {
                super.onSubmit();

                String email = emailField.getModelObject();
                String confirmationUrl = createUrl(email);

                /**
                 * We have to init() the service here because the Isis runtime is not available to us
                 * (guice will have instantiated a new instance of the service).
                 *
                 * We do it this way just so that the programming model for the EmailService is similar to regular Isis-managed services.
                 */
                emailService.init();

                boolean emailSent = emailService.send(new EmailRegistrationEvent(email, confirmationUrl));
                if (emailSent) {
                    Map<String, String> map = new HashMap<>();
                    map.put("email", email);
                    String emailSentMessage = getString("emailSentMessage", Model.ofMap(map));
                    success(emailSentMessage);
                }
            }
        };

        form.add(signUpButton);
    }

    private String createUrl(String email) {
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replace("-", "");

        // TODO mgrigorov: either improve the API or use a DB table for this
        AccountConfirmationMap accountConfirmationMap = getApplication().getMetaData(AccountConfirmationMap.KEY);
        accountConfirmationMap.put(uuid, email);

        PageParameters parameters = new PageParameters();
        parameters.set(0, uuid);
        CharSequence relativeUrl = urlFor(RegisterPage.class, parameters);
        UrlRenderer urlRenderer = getRequestCycle().getUrlRenderer();
        String fullUrl = urlRenderer.renderFullUrl(Url.parse(relativeUrl));

        return fullUrl;
    }

    @Inject
    private EmailNotificationService emailService;
}
