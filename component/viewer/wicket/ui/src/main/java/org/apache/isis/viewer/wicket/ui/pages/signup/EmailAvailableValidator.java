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

    private static final EmailAvailableValidator INSTANCE = new EmailAvailableValidator();

    public static EmailAvailableValidator getInstance() {
        return INSTANCE;
    }

    private EmailAvailableValidator() {}

    @Override
    public void validate(IValidatable<String> validatable) {
        IsisContext.openSession(new InitialisationSession());
        final UserRegistrationService userRegistrationService = IsisContext.getPersistenceSession().getServicesInjector().lookupService(UserRegistrationService.class);
        String email = validatable.getValue();
        if (userRegistrationService.emailExists(email)) {
            validatable.error(new ValidationError().addKey("emailIsNotAvailable"));
        }
        IsisContext.closeSession();
    }
}
