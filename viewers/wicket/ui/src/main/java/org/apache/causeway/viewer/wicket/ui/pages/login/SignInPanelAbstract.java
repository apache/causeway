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
package org.apache.causeway.viewer.wicket.ui.pages.login;

import java.time.ZoneId;
import java.util.Optional;

import jakarta.servlet.http.Cookie;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.cookies.CookieUtils;

import org.apache.causeway.commons.internal.base._StableValue;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.viewer.commons.model.session.LoginFormModel;
import org.apache.causeway.viewer.commons.services.session.CookieHandler;
import org.apache.causeway.viewer.commons.services.session.LoginService;
import org.apache.causeway.viewer.commons.services.session.LoginSuccessCallback;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.Getter;

/**
 * Cloned from {@link org.apache.wicket.authroles.authentication.panel.SignInPanel},
 * with an additional 'timezone' form field.
 * @see org.apache.wicket.authroles.authentication.panel.SignInPanel
 */
public abstract class SignInPanelAbstract
extends Panel
implements HasMetaModelContext {

    private static final long serialVersionUID = 1L;

    private static final String SIGN_IN_FORM = "signInForm";
    private static final String TIME_ZONE_SELECT = "timezone-select";

    private final _StableValue<LoginService> loginServiceRef = new _StableValue<>();
    protected final LoginService loginService() {
    	return loginServiceRef.orElseSet(()->lookupServiceElseFail(LoginService.class));
    }

    private final SignInFormWithTimeZone signInForm;

    /**
     * @param id
     *            See Component constructor
     * @param includeRememberMe
     *            True if form should include a remember-me checkbox
     * @see org.apache.wicket.Component#Component(String)
     */
    protected SignInPanelAbstract(final String id) {
        super(id);

        // Create feedback panel and add to page
        add(new FeedbackPanel("feedback"));

        // Add sign-in form to page, passing feedback panel as
        // validation error handler
        add(this.signInForm = new SignInFormWithTimeZone(SIGN_IN_FORM, this));
    }

    protected LoginFormModel loginFormModel() {
    	return signInForm.loginFormModel;
    }


    /**
     * @return signin form
     */
    protected SignInFormWithTimeZone getSignInFormWithTimeZone() {
        return (SignInFormWithTimeZone)get(SIGN_IN_FORM);
    }

    /**
     * Try to sign-in with remembered credentials.
     *
     * @see #setRememberMe(boolean)
     */
    @Override
    protected void onConfigure() {
        // logged in already?
        if (isSignedIn() == false) {
        	loginService().initModel(loginFormModel(), new CookieHandlerWkt());
        }
        super.onConfigure();
    }

    public void onSubmit(final LoginFormModel loginFormModel) {
    	var success = loginService().login(loginFormModel, new CookieHandlerWkt(), (rq, ic)->{
    		if(AuthenticatedWebSession.get() instanceof LoginSuccessCallback loginSuccessCallback) {
    			loginSuccessCallback.onLoginSuccess(rq, ic);
    		}
    	});
    	if(success) {
	        if(getWicketViewerSettings().clearOriginalDestination()) {
	            clearOriginalDestination();
	        }
            // If login has been called because the user was not yet logged in, than continue to the
            // original destination, otherwise to the Home page
            continueToOriginalDestination();
            setResponsePage(getApplication().getHomePage());
    	} else {
            // Try the component based localizer first. If not found try the
            // application localizer. Else use the default
            error(getLocalizer().getString("signInFailed", this, "Sign in failed"));
    	}
    }

    /**
     * @return true, if signed in
     */
    private boolean isSignedIn() {
        return AuthenticatedWebSession.get().isSignedIn();
    }

    @Getter(lazy = true) private static final ResourceReference jsForTimezoneSelectDefault =
            new JavaScriptResourceReference(SignInPanelAbstract.class,
                    "js/client-side-timezone-select.js");

    /**
     * Sign in form.
     */
    final static class SignInFormWithTimeZone extends StatelessForm<LoginFormModel> {
        private static final long serialVersionUID = 1L;

        private final SignInPanelAbstract parentPanel;
        private final LoginFormModel loginFormModel = new LoginFormModel();

        SignInFormWithTimeZone(final String id, final SignInPanelAbstract parentPanel) {
            super(id);

            this.parentPanel = parentPanel;

            var loginService = parentPanel.loginService();
            setModel(new CompoundPropertyModel<>(loginFormModel));

            // Attach textfields for username and password
            add(new TextField<>("username").setRequired(true));
            add(new PasswordTextField("password"));

            add(Wkt.dropDownChoice("timezone",
                    new PropertyModel<ZoneId>(loginFormModel, "timezone"),
                    loginService.timeZoneChoiceProvider().getAvailableZoneIds())
                .setRequired(true)
                .setMarkupId(TIME_ZONE_SELECT));

            // container for remember me checkbox
            var rememberMeContainer = new WebMarkupContainer("rememberMeContainer");
            add(rememberMeContainer);

            // Add rememberMe checkbox
            rememberMeContainer.add(new CheckBox("rememberMe"));
            rememberMeContainer.add(new Label("rememberMeDescription", loginService.rememberMeDescription()));

            // Show remember me checkbox?
            rememberMeContainer.setVisible(!loginService.signinConf().rememberMe().suppress());
        }

        /**
         * @see org.apache.wicket.markup.html.form.Form#onSubmit()
         */
        @Override
        public final void onSubmit() {
        	parentPanel.onSubmit(loginFormModel);
        }

    }

    // -- COOKIE HANDLER

    private record CookieHandlerWkt() implements CookieHandler {
		@Override public void put(final String cookieName, final String value) {
			new CookieUtils().save(cookieName, value);
		}
		@Override public Optional<String> lookup(final String cookieName) {
			return Optional.ofNullable(new CookieUtils().getCookie(cookieName))
					.map(Cookie::getValue);
		}
    }


}
