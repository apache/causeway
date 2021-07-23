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

import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.springframework.core.annotation.AliasFor;

/**
 * Meta-annotation for a mandatory {@link String} property or parameter representing a
 * name of some sort.
 *
 * @since 2.0 {@index}
 */
@Property(
        maxLength = Name.MAX_LENGTH,
        optionality = Optionality.MANDATORY
)
@PropertyLayout(
)
@Parameter(
        maxLength = Name.MAX_LENGTH,
        optionality = Optionality.MANDATORY
)
@ParameterLayout(
)
@javax.jdo.annotations.Column(length = Name.MAX_LENGTH, allowsNull = "false")
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Name {

    int MAX_LENGTH = 50;
    @AliasFor( annotation =  Property.class, attribute = "maxLength")
    int propertyMaxLength() default MAX_LENGTH;
    @AliasFor( annotation =  Parameter.class, attribute = "maxLength")
    int parameterMaxLength() default MAX_LENGTH;

    @AliasFor( annotation = Property.class, attribute = "optionality")
    Optionality propertyOptionality() default Optionality.MANDATORY;
    @AliasFor( annotation = Parameter.class, attribute = "optionality")
    Optionality parameterOptionality() default Optionality.MANDATORY;

    @AliasFor( annotation = javax.jdo.annotations.Column.class, attribute = "allowsNull")
    String columnAllowsNull() default "false";
    @AliasFor( annotation = javax.jdo.annotations.Column.class, attribute = "length")
    int columnLength() default MAX_LENGTH;

}