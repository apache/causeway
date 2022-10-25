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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.authentication.IAuthenticationStrategy;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.cookies.CookieUtils;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.model.util.WktContext;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

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

    /** True if the user should be remembered via form persistence (cookies) */
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

    private transient MetaModelContext commonContext;

    @Override
    public MetaModelContext getMetaModelContext() {
        return commonContext = WktContext.computeIfAbsent(commonContext);
    }

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
            val authenticationStrategy = authenticationStrategy();
            // get username, password and zoneID from persistence store
            final String[] data = authenticationStrategy.load();

            if ((data != null) && (data.length > 1)) {
                // try to sign in the user
                if (signIn(data[0], data[1])) {
                    username = data[0];
                    password = data[1];

                    val tzMememnto = recoverTimezone();
                    if(_Strings.isNotEmpty(tzMememnto)) {
                        try {
                            timezone = ZoneId.of(tzMememnto);
                        } catch (Exception e) {
                            timezone = null;
                            e.printStackTrace();
                        }
                    }

                    onSignInRemembered();
                } else {
                    // the loaded credentials are wrong. erase them.
                    authenticationStrategy.remove();
                }
            }
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
            .ifPresent(zoneId->storeUserTimeZoneToSession(zoneId));
        // If login has been called because the user was not yet logged in, than continue to the
        // original destination, otherwise to the Home page
        continueToOriginalDestination();
        setResponsePage(getApplication().getHomePage());
    }

    /**
     * Called when sign-in was remembered.
     * <p>
     * By default tries to continue to the original destination or switches to the application's
     * home page.
     * <p>
     * Note: This method will be called during rendering of this panel, thus a
     * {@link RestartResponseException} has to be used to switch to a different page.
     *
     * @see #onConfigure()
     */
    protected void onSignInRemembered() {
        Optional.ofNullable(getTimezone())
            .ifPresent(zoneId->storeUserTimeZoneToSession(zoneId));

        // logon successful. Continue to the original destination
        continueToOriginalDestination();

        // Ups, no original destination. Go to the home page
        throw new RestartResponseException(getApplication().getHomePage());
    }


    @Getter(lazy = true) private static final ResourceReference jsForTimezoneSelectDefault =
            new JavaScriptResourceReference(SignInPanelAbstract.class,
                    "js/client-side-timezone-select.js");

    private IAuthenticationStrategy authenticationStrategy() {
        return getApplication().getSecuritySettings()
            .getAuthenticationStrategy();
    }

    // -- TIME ZONE COOKIES

    private void rememberTimezone(final String tzMemento) {
        new CookieUtils().save(timezoneCookieName(), tzMemento);
    }

    private String recoverTimezone() {
        val cookie = new CookieUtils().getCookie(timezoneCookieName());
        return cookie!=null
                ? cookie.getValue()
                : null;
    }

    private String timezoneCookieName() {
        val rememberMe = getConfiguration().getViewer().getWicket().getRememberMe();
        val cookieName = rememberMe.getCookieKey()+"_tz";
        return cookieName;
    }


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

            val propertyModel = new CompoundPropertyModel<>(SignInPanelAbstract.this);

            setModel(propertyModel);

            // Attach textfields for username and password
            add(new TextField<>("username").setRequired(true));
            add(new PasswordTextField("password"));

            add(new DropDownChoice<ZoneId>("timezone",
                    new PropertyModel<ZoneId>(SignInPanelAbstract.this, "timezone"),
                    new LoadableDetachableModel<List<ZoneId>>() {
                        private static final long serialVersionUID = 1L;
                        @Override
                        protected List<ZoneId> load() {
                            return ZoneId.getAvailableZoneIds().stream()
                                    .sorted()
                                    .map(ZoneId::of)
                                    .collect(Collectors.toList());
                        }
                    })
                    .setRequired(true)
                    .setMarkupId(TIME_ZONE_SELECT));

            // container for remember me checkbox
            WebMarkupContainer rememberMeContainer = new WebMarkupContainer("rememberMeContainer");
            add(rememberMeContainer);

            // Add rememberMe checkbox
            rememberMeContainer.add(new CheckBox("rememberMe"));

            // Show remember me checkbox?
            rememberMeContainer.setVisible(includeRememberMe);
        }

        /**
         * @see org.apache.wicket.markup.html.form.Form#onSubmit()
         */
        @Override
        public final void onSubmit() {
            val authenticationStrategy = authenticationStrategy();

            if (signIn(username, password)) {
                if (rememberMe) {
                    authenticationStrategy.save(
                            username,
                            password);

                    // remember time-zone selection
                    rememberTimezone(timezone!=null
                          ? timezone.getId()
                          : "");
                } else {
                    authenticationStrategy.remove();
                }

                onSignInSucceeded();
            } else {
                onSignInFailed();
                authenticationStrategy.remove();
            }
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

}
