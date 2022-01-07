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
package org.apache.isis.persistence.jdo.applib.types;

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
 * Meta-annotation for an optional {@link java.math.BigDecimal} property or parameter
 * representing a monetary amount.
 *
 * @since 2.0 {@index}
 */
@Property(
        optionality = Optionality.OPTIONAL
)
@PropertyLayout(
)
@Parameter(
        optionality = Optionality.OPTIONAL
)
@ParameterLayout(
)
@javax.validation.constraints.Digits(
        integer = Money.INTEGER,
        fraction = Money.FRACTION
)
@javax.jdo.annotations.Column(allowsNull = "true", length = Money.INTEGER + Money.FRACTION, scale = Money.FRACTION)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Money {

    @AliasFor( annotation = Property.class, attribute = "optionality")
    Optionality propertyOptionality() default Optionality.OPTIONAL;
    @AliasFor( annotation = Parameter.class, attribute = "optionality")
    Optionality parameterOptionality() default Optionality.OPTIONAL;

    @AliasFor( annotation = javax.jdo.annotations.Column.class, attribute = "allowsNull")
    String columnAllowsNull() default "true";
    @AliasFor( annotation = javax.jdo.annotations.Column.class, attribute = "length")
    int columnLength() default Money.INTEGER + Money.FRACTION;
    @AliasFor( annotation = javax.jdo.annotations.Column.class, attribute = "scale")
    int columnScale() default Money.FRACTION;

    int INTEGER = 10;
    @AliasFor( annotation = javax.validation.constraints.Digits.class, attribute = "integer")
    int digitsInteger() default Money.INTEGER;

    int FRACTION = 2;
    @AliasFor( annotation = javax.validation.constraints.Digits.class, attribute = "fraction")
    int digitsFraction() default Money.FRACTION;

}
