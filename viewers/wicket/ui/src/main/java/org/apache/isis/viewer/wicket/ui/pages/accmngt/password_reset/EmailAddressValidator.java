package org.apache.isis.viewer.wicket.ui.pages.accmngt.password_reset;

import org.apache.wicket.validation.validator.PatternValidator;

/**
 * Copy of {@link org.apache.wicket.validation.validator.EmailAddressValidator}.
 */
public class EmailAddressValidator extends PatternValidator {
    private static final long serialVersionUID = 1L;
    private static final EmailAddressValidator INSTANCE = new EmailAddressValidator();

    public static EmailAddressValidator getInstance() {
        return INSTANCE;
    }

    protected EmailAddressValidator() {
        super(
        "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z]{2,}){1}$)", 2);
    }
}
