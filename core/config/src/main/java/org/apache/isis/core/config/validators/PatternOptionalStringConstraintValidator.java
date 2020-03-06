/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
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
