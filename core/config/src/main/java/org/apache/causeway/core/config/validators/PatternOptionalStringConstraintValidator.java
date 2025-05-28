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
package org.apache.causeway.core.config.validators;

import java.util.Optional;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Pattern;

import org.springframework.stereotype.Component;

import org.apache.causeway.commons.internal.context._Context;

import lombok.SneakyThrows;

@Component
public class PatternOptionalStringConstraintValidator
implements ConstraintValidator<jakarta.validation.constraints.Pattern, Optional<String>> {

    private final ConstraintValidator<Pattern, CharSequence> patternValidator;

    @SneakyThrows
    public PatternOptionalStringConstraintValidator(){
        var patternValidatorClass = _Context.loadClass("org.hibernate.validator.internal.constraintvalidators.bv.PatternValidator");
        this.patternValidator = (ConstraintValidator<Pattern, CharSequence>)
            patternValidatorClass
                .getConstructor()
                .newInstance();
    }

    @Override
    public void initialize(final jakarta.validation.constraints.Pattern constraintAnnotation) {
        patternValidator.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(
            final Optional<String> value,
            final ConstraintValidatorContext context) {
        if(!value.isPresent()) return true;

        return patternValidator.isValid(value.get(), context);
    }
}
