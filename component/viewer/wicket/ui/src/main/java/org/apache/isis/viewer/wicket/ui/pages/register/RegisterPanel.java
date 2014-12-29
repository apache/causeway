package org.apache.isis.viewer.wicket.ui.pages.register;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.isis.applib.services.userreg.UserRegistrationService;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosure;
import org.apache.isis.viewer.wicket.ui.pages.signup.AccountConfirmationMap;

/**
 * TODO ISIS-987 Add javadoc
 */
public class RegisterPanel extends Panel {

    private static final String REGISTER_FORM = "registerForm";

    public RegisterPanel(String id, String uuid) {
        super(id);

        add(new RegisterForm(REGISTER_FORM, uuid));

        add(new NotificationPanel("feedback"));
    }


    /**
     * Register user form.
     */
    private static class RegisterForm extends StatelessForm<Registree>
    {
        private static final long serialVersionUID = 1L;

        private final String uuid;

        /**
         * Constructor.
         *
         * @param id
         *            id of the form component
         */
        public RegisterForm(final String id, final String uuid)
        {
            super(id);

            this.uuid = uuid;

            String email = getEmail();

            final Registree registree = new Registree();
            registree.setEmail(email);

            setModel(new CompoundPropertyModel<>(registree));

            add(new RequiredTextField<String>("username"));

            PasswordTextField password = new PasswordTextField("password");
            add(password);
            PasswordTextField verifyPassword = new PasswordTextField("verifyPassword");
            add(verifyPassword);
            add(new EqualPasswordInputValidator(password, verifyPassword));

            // use RO model to prevent changing the email
            add(new TextField<>("email", new AbstractReadOnlyModel<String>() {
                @Override
                public String getObject() {
                    return registree.getEmail();
                }
            }));
        }

        @Override
        public final void onSubmit()
        {
            IsisContext.openSession(new InitialisationSession());

            final Registree registree = getModelObject();

            // TODO: need to add a link on the login page to register.
            // TODO: however, if there is no UserRegistrationService domain service available, then should suppress the link to get to the registration page.
            final UserRegistrationService userRegistrationService = IsisContext.getPersistenceSession().getServicesInjector().lookupService(UserRegistrationService.class);

            IsisContext.getTransactionManager().executeWithinTransaction(new TransactionalClosure() {
                @Override
                public void preExecute() {
                }

                @Override
                public void execute() {
                    userRegistrationService.registerUser(registree.getUsername(), registree.getPassword(), registree.getEmail());
                    removeAccountConfirmation();
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure() {
                }
            });

            IsisContext.closeSession();

            // TODO: more error handling here... eg what if the username is already in use.
            signIn(registree.getUsername(), registree.getPassword());
            setResponsePage(getApplication().getHomePage());
        }

        private boolean signIn(String username, String password)
        {
            return AuthenticatedWebSession.get().signIn(username, password);
        }

        private String getEmail() {
            AccountConfirmationMap accountConfirmationMap = getApplication().getMetaData(AccountConfirmationMap.KEY);
            return accountConfirmationMap.get(uuid);
        }

        private void removeAccountConfirmation() {
            AccountConfirmationMap accountConfirmationMap = getApplication().getMetaData(AccountConfirmationMap.KEY);
            accountConfirmationMap.remove(uuid);
        }
    }

}
