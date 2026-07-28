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
import java.util.regex.Matcher;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Pattern;

import org.springframework.stereotype.Component;

@Component
public class PatternOptionalStringConstraintValidator
        implements ConstraintValidator<Pattern, Optional<String>> {

    private java.util.regex.Pattern regex;
    private String regexp;
    private int flags;

    @Override
    public void initialize(final Pattern annotation) {
        this.regexp = annotation.regexp();
        this.flags = mapFlags(annotation.flags());
        this.regex = java.util.regex.Pattern.compile(this.regexp, this.flags);
    }

    @Override
    public boolean isValid(final Optional<String> value, final ConstraintValidatorContext context) {
        if (value == null || value.isEmpty())
			return true;

        String s = value.get();

        // Match semantics for Bean Validation @Pattern is "find a match", not "full match"
        // If you want full match, use matcher.matches() and/or add ^...$ in regexp.
        Matcher m = regex.matcher(s);
        return m.find();
    }

    private static int mapFlags(final jakarta.validation.constraints.Pattern.Flag[] flags) {
        int out = 0;
        for (jakarta.validation.constraints.Pattern.Flag f : flags) {
            out |= f.getValue();
        }
        return out;
    }
}