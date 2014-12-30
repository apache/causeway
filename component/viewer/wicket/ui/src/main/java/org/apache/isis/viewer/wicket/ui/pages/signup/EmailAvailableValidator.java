package org.apache.isis.viewer.wicket.ui.pages.signup;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.isis.applib.services.userreg.UserRegistrationService;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;

/**
 * Validates that an email is not already in use by another user
 */
public class EmailAvailableValidator implements IValidator<String> {

    public static final EmailAvailableValidator EXISTS = new EmailAvailableValidator(true, "emailIsNotAvailable");
    public static final EmailAvailableValidator DOESNT_EXIST = new EmailAvailableValidator(false, "noSuchUserByEmail");

    private final boolean emailExists;
    private final String resourceKey;

    private EmailAvailableValidator(boolean emailExists, String resourceKey) {
        this.emailExists = emailExists;
        this.resourceKey = resourceKey;
    }

    @Override
    public void validate(IValidatable<String> validatable) {
        IsisContext.openSession(new InitialisationSession());
        final UserRegistrationService userRegistrationService = IsisContext.getPersistenceSession().getServicesInjector().lookupService(UserRegistrationService.class);
        String email = validatable.getValue();
        if (userRegistrationService.emailExists(email) == emailExists) {
            validatable.error(new ValidationError().addKey(resourceKey));
        }
        IsisContext.closeSession();
    }
}
