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
import org.jspecify.annotations.NonNull;

import org.springframework.util.StringUtils;

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.value.semantics.TimeZoneChoiceProvider;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Wicket.RememberMe;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.Getter;
import lombok.Setter;

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

    /** True if the panel should display a remember-me checkbox */
    private boolean includeRememberMe = true;

    /**
     * True if the username and timezone should be remembered via form persistence (cookies).
     * This does not include any secrets.
     */
    @Getter @Setter
    private boolean rememberMe = true;

    /** Password. */
    @Getter @Setter
    private String password;

    /** User name. */
    @Getter @Setter
    private String username;

    /** Timezone id. */
    @Getter @Setter
    private ZoneId timezone;

    /**
     * @param id
     *            See Component constructor
     * @param includeRememberMe
     *            True if form should include a remember-me checkbox
     * @see org.apache.wicket.Component#Component(String)
     */
    protected SignInPanelAbstract(final String id, final boolean includeRememberMe) {
        super(id);

        this.includeRememberMe = includeRememberMe;

        // Create feedback panel and add to page
        add(new FeedbackPanel("feedback"));

        // Add sign-in form to page, passing feedback panel as
        // validation error handler
        add(new SignInFormWithTimeZone(SIGN_IN_FORM));
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
            // get username and zoneID from cookies (if available)
        	username = usernameCookie().recoverUsername()
            		.orElse(null);
        	timezone = timezoneCookie().recoverTimezone()
            		.orElse(null);
        }

        super.onConfigure();
    }

    /**
     * Sign in user if possible.
     *
     * @param username
     *            The username
     * @param password
     *            The password
     * @return True if signin was successful
     */
    private boolean signIn(final String username, final String password) {
        return AuthenticatedWebSession.get().signIn(username, password);
    }

    /**
     * @return true, if signed in
     */
    private boolean isSignedIn() {
        return AuthenticatedWebSession.get().isSignedIn();
    }

    /**
     * Called when sign in failed
     */
    protected void onSignInFailed() {
        clearUserTimeZoneFromSession();
        // Try the component based localizer first. If not found try the
        // application localizer. Else use the default
        error(getLocalizer().getString("signInFailed", this, "Sign in failed"));
    }

    /**
     * Called when sign in was successful
     */
    protected void onSignInSucceeded() {
        Optional.ofNullable(getTimezone())
            .ifPresent(this::storeUserTimeZoneToSession);
        // If login has been called because the user was not yet logged in, than continue to the
        // original destination, otherwise to the Home page
        continueToOriginalDestination();
        setResponsePage(getApplication().getHomePage());
    }

    @Getter(lazy = true) private static final ResourceReference jsForTimezoneSelectDefault =
            new JavaScriptResourceReference(SignInPanelAbstract.class,
                    "js/client-side-timezone-select.js");

    /**
     * Sign in form.
     */
    public final class SignInFormWithTimeZone extends StatelessForm<SignInPanelAbstract> {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param id
         *            id of the form component
         */
        public SignInFormWithTimeZone(final String id) {
            super(id);

            var propertyModel = new CompoundPropertyModel<>(SignInPanelAbstract.this);

            setModel(propertyModel);

            // Attach textfields for username and password
            add(new TextField<>("username").setRequired(true));
            add(new PasswordTextField("password"));

            add(Wkt.dropDownChoice("timezone",
                    new PropertyModel<ZoneId>(SignInPanelAbstract.this, "timezone"),
                    getTimeZoneChoiceProvider().getAvailableZoneIds())
                .setRequired(true)
                .setMarkupId(TIME_ZONE_SELECT));

            // container for remember me checkbox
            var rememberMeContainer = new WebMarkupContainer("rememberMeContainer");
            add(rememberMeContainer);

            // Add rememberMe checkbox
            rememberMeContainer.add(new CheckBox("rememberMe"));
            rememberMeContainer.add(new Label("rememberMeDescription",
            		getTranslationService().translate(
            				TranslationContext.named("Login"),
            				getConfiguration().viewer().wicket().rememberMe().description())));

            // Show remember me checkbox?
            rememberMeContainer.setVisible(includeRememberMe);
        }

        /**
         * @see org.apache.wicket.markup.html.form.Form#onSubmit()
         */
        @Override
        public final void onSubmit() {
            if (signIn(username, password)) {
                if (isRememberMe()
                		&& StringUtils.hasLength(username)) {
                	// remember latest username
                	usernameCookie()
                		.rememberUsername(username);
                }
                if (isRememberMe()
                		&& timezone!=null) {
                	// remember time-zone selection

                	timezoneCookie()
                		.rememberTimezone(timezone);
                }
                onSignInSucceeded();
            } else {
                onSignInFailed();
            }
        }

        private TimeZoneChoiceProvider getTimeZoneChoiceProvider() {
            return getMetaModelContext().getServiceRegistry().lookupService(TimeZoneChoiceProvider.class)
                    .orElseGet(TimeZoneChoiceProvider::fallback);
        }

    }

    /**
     * Stores user's {@link ZoneId} to their session.
     */
    protected abstract void storeUserTimeZoneToSession(@NonNull ZoneId zoneId);

    /**
     * Clears user's {@link ZoneId} from their session.
     */
    protected abstract void clearUserTimeZoneFromSession();

    // -- USER NAME COOKIES

    private record UsernameCookie(String cookieName) {
    	UsernameCookie(final RememberMe conf) {
    		this(conf.cookieKey() + "_usr");
    	}
    	void rememberUsername(final String username) {
    		new CookieUtils().save(cookieName, username);
    	}
    	Optional<String> recoverUsername() {
    		return Optional.ofNullable(new CookieUtils().getCookie(cookieName))
					.map(Cookie::getValue)
					.map(_Strings::emptyToNull);
    	}
    }

    private UsernameCookie usernameCookie() {
    	return new UsernameCookie(getConfiguration().viewer().wicket().rememberMe());
    }

    // -- TIME ZONE COOKIES

    private record TimezoneCookie(String cookieName) {
    	TimezoneCookie(final RememberMe conf) {
    		this(conf.cookieKey() + "_tz");
    	}
    	void rememberTimezone(final ZoneId zoneId) {
    		new CookieUtils().save(cookieName, zoneId.getId());
    	}
    	Optional<ZoneId> recoverTimezone() {
    		try {
    			return Optional.ofNullable(new CookieUtils().getCookie(cookieName))
    					.map(Cookie::getValue)
    					.map(_Strings::emptyToNull)
    					.map(ZoneId::of);
    		} catch (Exception e) {
    			e.printStackTrace();
    			return Optional.empty();
    		}
    	}
    }

    private TimezoneCookie timezoneCookie() {
    	return new TimezoneCookie(getConfiguration().viewer().wicket().rememberMe());
    }
}
