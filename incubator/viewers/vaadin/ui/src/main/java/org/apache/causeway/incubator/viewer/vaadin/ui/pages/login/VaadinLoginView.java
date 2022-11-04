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
package org.apache.causeway.incubator.viewer.vaadin.ui.pages.login;

import javax.inject.Inject;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;
import org.apache.causeway.core.security.authentication.AuthenticationRequestPassword;
import org.apache.causeway.incubator.viewer.vaadin.ui.auth.VaadinAuthenticationHandler;
import org.apache.causeway.incubator.viewer.vaadin.ui.pages.main.MainViewVaa;

import lombok.val;

/**
 * Yet a minimal working version of a login page.
 *
 */
@Route("login")
public class VaadinLoginView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private final transient VaadinAuthenticationHandler vaadinAuthenticationHandler;

    @Inject
    public VaadinLoginView(
            final CausewayConfiguration causewayConfiguration,
            final WebAppContextPath webAppContextPath,
            final VaadinAuthenticationHandler vaadinAuthenticationHandler) {

        this.vaadinAuthenticationHandler = vaadinAuthenticationHandler;

        addTitleAndLogo(causewayConfiguration, webAppContextPath);

        val usernameField = new TextField("Username");
        val passwordField = new PasswordField("Password");

        val loginButton = new Button("Login");
        loginButton.getElement().setAttribute("theme", "primary");
        loginButton.addClickListener((ComponentEventListener<ClickEvent<Button>>)
                buttonClickEvent -> doLogin(
                        usernameField.getValue(),
                        passwordField.getValue()));

        val loginAsSvenButton = new Button("Login (as Sven)");
        loginAsSvenButton.getElement().setAttribute("theme", "primary");
        loginAsSvenButton.addClickListener((ComponentEventListener<ClickEvent<Button>>)
                buttonClickEvent -> doLoginAsSven());

        val buttonsLayout = new HorizontalLayout(loginButton, loginAsSvenButton);
        val divLayout = new VerticalLayout(usernameField, passwordField, buttonsLayout);
        divLayout.setAlignSelf(Alignment.START, buttonsLayout);
        val loginDiv = new Div(divLayout);
        setAlignItems(Alignment.CENTER);
        usernameField.focus();
        add(loginDiv);

    }

    // -- HELPER

    private void doLogin(final String userName, final String secret) {
        val authenticationRequest = new AuthenticationRequestPassword(userName, secret);
        if(vaadinAuthenticationHandler.loginToSession(authenticationRequest)) {
            getUI().ifPresent(ui->ui.navigate(MainViewVaa.class));
        } else {
            // TODO indicate to the user: login failed
        }
    }

    /** @deprecated early development only */
    @Deprecated
    private void doLoginAsSven() {
        doLogin("sven", "pass");
    }

    private void addTitleAndLogo(final CausewayConfiguration causewayConfiguration, final WebAppContextPath webAppContextPath) {
        val applicationName = causewayConfiguration.getViewer().getCommon().getApplication().getName();
        val applicationLogo = causewayConfiguration.getViewer().getCommon().getApplication().getBrandLogoSignin();

        applicationLogo.ifPresent(logoUrl->{
            add(new Image(webAppContextPath.prependContextPathIfLocal(logoUrl), "logo"));
        });

        add(new H1(applicationName));

    }

}
