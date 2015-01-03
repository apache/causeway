package org.apache.isis.viewer.wicket.ui.pages.register;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.isis.applib.services.userreg.UserRegistrationService;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.TransactionalClosureAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.pages.signup.AccountConfirmationMap;
import org.apache.isis.viewer.wicket.ui.pages.signup.UsernameAvailableValidator;

/**
 * A panel with a form for self-registration of a user
 */
public class RegisterPanel extends Panel {

    private static final String ID_REGISTER_FORM = "registerForm";

    public RegisterPanel(String id, String uuid) {
        super(id);

        add(new RegisterForm(ID_REGISTER_FORM, uuid));
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

            addUsername();

            PasswordTextField password = addPassword();
            PasswordTextField confirmPassword = addConfirmPassword();
            add(new EqualPasswordInputValidator(password, confirmPassword));

            addEmail(registree);
        }

        protected TextField<String> addEmail(final Registree registree) {
            // use RO-ish model to prevent changing the email with manual form submits
            TextField<String> emailField = new TextField<>("email", new Model<String>() {
                @Override
                public String getObject() {
                    return registree.getEmail();
                }
            });
            addOrReplace(emailField);
            return emailField;
        }

        protected PasswordTextField addConfirmPassword() {
            PasswordTextField confirmPassword = new PasswordTextField("confirmPassword");
            confirmPassword.setLabel(new ResourceModel("confirmPasswordLabel"));
            FormGroup confirmPasswordFormGroup = new FormGroup("confirmPasswordFormGroup", confirmPassword);
            confirmPasswordFormGroup.add(confirmPassword);
            addOrReplace(confirmPasswordFormGroup);
            return confirmPassword;
        }

        protected PasswordTextField addPassword() {
            PasswordTextField password = new PasswordTextField("password");
            password.setLabel(new ResourceModel("passwordLabel"));
            FormGroup passwordFormGroup = new FormGroup("passwordFormGroup", password);
            passwordFormGroup.add(password);
            addOrReplace(passwordFormGroup);
            return password;
        }

        protected TextField<String> addUsername() {
            RequiredTextField<String> username = new RequiredTextField<>("username");
            username.add(UsernameAvailableValidator.INSTANCE);
            FormGroup usernameFormGroup = new FormGroup("usernameFormGroup", username);
            usernameFormGroup.add(username);
            addOrReplace(usernameFormGroup);
            return username;
        }

        @Override
        public final void onSubmit()
        {
            final Registree registree = getModelObject();

            IsisContext.doInSession(new Runnable() {
                @Override
                public void run() {
                    final UserRegistrationService userRegistrationService = IsisContext.getPersistenceSession().getServicesInjector().lookupService(UserRegistrationService.class);

                    IsisContext.getTransactionManager().executeWithinTransaction(new TransactionalClosureAbstract() {
                        @Override
                        public void execute() {
                            userRegistrationService.registerUser(registree.getUsername(), registree.getPassword(), registree.getEmail());
                            removeAccountConfirmation();
                        }
                    });
                }
            });

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
