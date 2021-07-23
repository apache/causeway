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

import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.springframework.core.annotation.AliasFor;

/**
 * Meta-annotation for an optional {@link String} property or parameter representing a
 * phone number.
 *
 * @since 2.0 {@index}
 */
@Property(
        maxLength = PhoneNumber.MAX_LENGTH,
        optionality = Optionality.OPTIONAL,
        regexPattern = PhoneNumber.REGEX_PATTERN,
        regexPatternReplacement = PhoneNumber.REGEX_PATTERN_REPLACEMENT
)
@PropertyLayout(
        named = PhoneNumber.NAMED
)
@Parameter(
        maxLength = PhoneNumber.MAX_LENGTH,
        optionality = Optionality.OPTIONAL,
        regexPattern = PhoneNumber.REGEX_PATTERN,
        regexPatternReplacement = PhoneNumber.REGEX_PATTERN_REPLACEMENT
)
@ParameterLayout(
        named = PhoneNumber.NAMED
)
//@javax.jdo.annotations.Column(length = PhoneNumber.MAX_LENGTH, allowsNull = "true")
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumber {

    int MAX_LENGTH = 20;
    @AliasFor( annotation =  Property.class, attribute = "maxLength")
    int propertyMaxLength() default MAX_LENGTH;
    @AliasFor( annotation =  Parameter.class, attribute = "maxLength")
    int parameterMaxLength() default MAX_LENGTH;

    @AliasFor( annotation = Property.class, attribute = "optionality")
    Optionality propertyOptionality() default Optionality.OPTIONAL;
    @AliasFor( annotation = Parameter.class, attribute = "optionality")
    Optionality parameterOptionality() default Optionality.OPTIONAL;

    String REGEX_PATTERN = "[+]?[0-9 -]*";
    @AliasFor( annotation =  Property.class, attribute = "regexPattern")
    String propertyRegexPattern() default REGEX_PATTERN;
    @AliasFor( annotation =  Parameter.class, attribute = "regexPattern")
    String parameterRegexPattern() default REGEX_PATTERN;

    String REGEX_PATTERN_REPLACEMENT = "Only numbers and two symbols being \"-\" and \"+\" are allowed ";
    @AliasFor( annotation =  Property.class, attribute = "regexPatternReplacement")
    String propertyRegexPatternReplacement() default REGEX_PATTERN_REPLACEMENT;
    @AliasFor( annotation =  Parameter.class, attribute = "regexPatternReplacement")
    String parameterRegexPatternReplacement() default REGEX_PATTERN_REPLACEMENT;

//    @AliasFor( annotation = javax.jdo.annotations.Column.class, attribute = "allowsNull")
//    String columnAllowsNull() default "true";
//    @AliasFor( annotation = javax.jdo.annotations.Column.class, attribute = "length")
//    int columnLength() default MAX_LENGTH;

    String NAMED = "Phone Number";
    @AliasFor( annotation =  PropertyLayout.class, attribute = "named")
    String propertyLayoutNamed() default NAMED;
    @AliasFor( annotation =  ParameterLayout.class, attribute = "named")
    String parameterLayoutNamed() default NAMED;

}
