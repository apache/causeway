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
package org.apache.isis.subdomains.base.applib.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

import org.apache.isis.applib.annotations.Optionality;
import org.apache.isis.applib.annotations.Parameter;
import org.apache.isis.applib.annotations.ParameterLayout;
import org.apache.isis.applib.annotations.Property;
import org.apache.isis.applib.annotations.PropertyLayout;

/**
 * Meta-annotation for an optional {@link String} property or parameter representing an
 * email address.
 *
 * @since 2.0 {@index}
 */
@Property(
        maxLength = Email.MAX_LENGTH,
        optionality = Optionality.OPTIONAL,
        regexPattern = Email.REGEX_PATTERN,
        regexPatternReplacement = Email.REGEX_PATTERN_REPLACEMENT
)
@PropertyLayout(
)
@Parameter(
        maxLength = Email.MAX_LENGTH,
        optionality = Optionality.OPTIONAL,
        regexPattern = Email.REGEX_PATTERN,
        regexPatternReplacement = Email.REGEX_PATTERN_REPLACEMENT
)
@ParameterLayout(
)
//@javax.jdo.annotations.Column(length = Email.MAX_LENGTH, allowsNull = "true")
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Email {

    /**
     * See <a href="http://stackoverflow.com/questions/386294/what-is-the-maximum-length-of-a-valid-email-address">maximum length of an email address</a>.
     */
    int MAX_LENGTH = 254;
    @AliasFor( annotation =  Property.class, attribute = "maxLength")
    int propertyMaxLength() default MAX_LENGTH;
    @AliasFor( annotation =  Parameter.class, attribute = "maxLength")
    int parameterMaxLength() default MAX_LENGTH;

    @AliasFor( annotation = Property.class, attribute = "optionality")
    Optionality propertyOptionality() default Optionality.OPTIONAL;
    @AliasFor( annotation = Parameter.class, attribute = "optionality")
    Optionality parameterOptionality() default Optionality.OPTIONAL;

    /**
     * as per http://emailregex.com/
     */
    String REGEX_PATTERN = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
    @AliasFor( annotation =  Property.class, attribute = "regexPattern")
    String propertyRegexPattern() default REGEX_PATTERN;
    @AliasFor( annotation =  Parameter.class, attribute = "regexPattern")
    String parameterRegexPattern() default REGEX_PATTERN;

    String REGEX_PATTERN_REPLACEMENT = "Email address is badly formed";
    @AliasFor( annotation =  Property.class, attribute = "regexPatternReplacement")
    String propertyRegexPatternReplacement() default REGEX_PATTERN_REPLACEMENT;
    @AliasFor( annotation =  Parameter.class, attribute = "regexPatternReplacement")
    String parameterRegexPatternReplacement() default REGEX_PATTERN_REPLACEMENT;

//    @AliasFor( annotation = javax.jdo.annotations.Column.class, attribute = "allowsNull")
//    String columnAllowsNull() default "true";
//    @AliasFor( annotation = javax.jdo.annotations.Column.class, attribute = "length")
//    int columnLength() default MAX_LENGTH;

}
