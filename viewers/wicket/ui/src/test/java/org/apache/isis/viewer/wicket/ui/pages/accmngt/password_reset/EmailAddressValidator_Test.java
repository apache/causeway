package org.apache.isis.viewer.wicket.ui.pages.accmngt.password_reset;


import org.apache.wicket.validation.Validatable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class EmailAddressValidator_Test {

    @Test
    void valid() {
        final Validatable<String> validatable = new Validatable<>("foo@bar.com");
        EmailAddressValidator.getInstance().validate(validatable);

        Assertions.assertThat(validatable.getErrors()).isEmpty();
    }

    @Test
    void invalid() {
        final Validatable<String> validatable = new Validatable<>("foo@bar.co.");
        EmailAddressValidator.getInstance().validate(validatable);

        Assertions.assertThat(validatable.getErrors()).isNotEmpty();
    }

    @Test
    void invalid_with_plus_at_start() {
        final Validatable<String> validatable = new Validatable<>("+foo@bar.com");
        EmailAddressValidator.getInstance().validate(validatable);

        Assertions.assertThat(validatable.getErrors()).isNotEmpty();
    }

    @Test
    void valid_with_plus_at_end() {
        final Validatable<String> validatable = new Validatable<>("foo+@bar.com");
        EmailAddressValidator.getInstance().validate(validatable);

        Assertions.assertThat(validatable.getErrors()).isEmpty();
    }

    @Test
    void valid_with_plus() {
        final Validatable<String> validatable = new Validatable<>("foo+bop@bar.com");
        EmailAddressValidator.getInstance().validate(validatable);

        Assertions.assertThat(validatable.getErrors()).isEmpty();
    }

    @Test
    void valid_co_uk() {
        final Validatable<String> validatable = new Validatable<>("foo@bar.co.uk");
        EmailAddressValidator.getInstance().validate(validatable);

        Assertions.assertThat(validatable.getErrors()).isEmpty();
    }


}
