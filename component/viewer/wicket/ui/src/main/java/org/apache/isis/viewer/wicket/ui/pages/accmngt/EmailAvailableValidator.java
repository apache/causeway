package org.apache.isis.viewer.wicket.ui.pages.accmngt;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.isis.applib.services.userreg.UserRegistrationService;
import org.apache.isis.core.runtime.system.context.IsisContext;

/**
 * Validates that an email is or is not already in use by another user
 */
public class EmailAvailableValidator implements IValidator<String> {

    public static final EmailAvailableValidator EXISTS = new EmailAvailableValidator(true, "noSuchUserByEmail");
    public static final EmailAvailableValidator DOESNT_EXIST = new EmailAvailableValidator(false, "emailIsNotAvailable");

    private final boolean emailExists;
    private final String resourceKey;

    private EmailAvailableValidator(boolean emailExists, String resourceKey) {
        this.emailExists = emailExists;
        this.resourceKey = resourceKey;
    }

    @Override
    public void validate(final IValidatable<String> validatable) {
        IsisContext.doInSession(new Runnable() {
            @Override
            public void run() {
                UserRegistrationService userRegistrationService = IsisContext.getPersistenceSession().getServicesInjector().lookupService(UserRegistrationService.class);
                String email = validatable.getValue();
                boolean emailExists1 = userRegistrationService.emailExists(email);
                if (emailExists1 != emailExists) {
                    validatable.error(new ValidationError().addKey(resourceKey));
                }
            }
        });

    }
}
