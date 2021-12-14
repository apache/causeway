package org.apache.isis.viewer.wicket.ui.pages.login;

import java.time.ZoneId;
import java.util.List;
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

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

/**
 * Cloned from {@link org.apache.wicket.authroles.authentication.panel.SignInPanel},
 * with an additional 'timezone' form field.
 * @see org.apache.wicket.authroles.authentication.panel.SignInPanel
 */
public class SignInPanelAbstract
extends Panel
implements HasCommonContext {

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

    private transient IsisAppCommonContext commonContext;

    @Override
    public IsisAppCommonContext getCommonContext() {
        return commonContext = CommonContextUtils.computeIfAbsent(commonContext);
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
            IAuthenticationStrategy authenticationStrategy = getApplication().getSecuritySettings()
                .getAuthenticationStrategy();
            // get username, password and zoneID from persistence store
            String[] data = authenticationStrategy.load();

            if ((data != null) && (data.length > 1)) {
                // try to sign in the user
                if (signIn(data[0], data[1])) {
                    username = data[0];
                    password = data[1];

                    if(data.length > 2
                            && _Strings.isNotEmpty(data[2])) {
                        try {
                            timezone = ZoneId.of(data[2]);
                        } catch (Exception e) {
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
        // Try the component based localizer first. If not found try the
        // application localizer. Else use the default
        error(getLocalizer().getString("signInFailed", this, "Sign in failed"));
    }

    /**
     * Called when sign in was successful
     */
    protected void onSignInSucceeded() {
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
        // logon successful. Continue to the original destination
        continueToOriginalDestination();

        // Ups, no original destination. Go to the home page
        throw new RestartResponseException(getApplication().getHomePage());
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
            IAuthenticationStrategy strategy = getApplication().getSecuritySettings()
                .getAuthenticationStrategy();

            if (signIn(getUsername(), getPassword())) {
                if (rememberMe == true) {
                    strategy.save(
                            username,
                            password,
                            timezone!=null
                                ? timezone.getId()
                                : "");
                } else {
                    strategy.remove();
                }

                onSignInSucceeded();
            } else {
                onSignInFailed();
                strategy.remove();
            }
        }

    }
}
