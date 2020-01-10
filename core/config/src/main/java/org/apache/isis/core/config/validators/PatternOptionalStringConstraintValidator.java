package org.apache.isis.core.config.validators;

import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.RegEx;
import javax.validation.Configuration;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidatorFactory;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ConfigurationState;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.internal.constraintvalidators.bv.PatternValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class PatternOptionalStringConstraintValidator
        implements ConstraintValidator<javax.validation.constraints.Pattern, Optional<String>> {


    private final PatternValidator patternValidator = new PatternValidator();

    public PatternOptionalStringConstraintValidator(){
    }

    @Override
    public void initialize(javax.validation.constraints.Pattern constraintAnnotation) {
        patternValidator.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(
            final Optional<String> value,
            final ConstraintValidatorContext context) {
        if(!value.isPresent()) {
            return true;
        }
        return patternValidator.isValid(value.get(), context);
    }
}
