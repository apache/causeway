package org.apache.isis.viewer.wicket.ui.pages.accmngt;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.isis.applib.services.userreg.UserRegistrationService;
import org.apache.isis.core.runtime.system.context.IsisContext;

/**
 * Validates that an username is or is not already in use by someone else
 */
public class UsernameAvailableValidator implements IValidator<String> {

    public static final UsernameAvailableValidator INSTANCE = new UsernameAvailableValidator();

    private UsernameAvailableValidator() {
    }

    @Override
    public void validate(final IValidatable<String> validatable) {
        IsisContext.doInSession(new Runnable() {
            @Override
            public void run() {
                UserRegistrationService userRegistrationService = IsisContext.getPersistenceSession().getServicesInjector().lookupService(UserRegistrationService.class);
                String username = validatable.getValue();
                boolean usernameExists = userRegistrationService.usernameExists(username);
                if (usernameExists) {
                    validatable.error(new ValidationError().addKey("usernameIsNotAvailable"));
                }
            }
        });

    }
}
