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
 * Meta-annotation for an optional {@link String} property or parameter representing a
 * description of some sort.
 *
 * @since 2.0 {@index}
 */
@Property(
        maxLength = Description.MAX_LENGTH,
        optionality = Optionality.OPTIONAL
)
@PropertyLayout(
        multiLine = Description.MULTI_LINE
)
@Parameter(
        maxLength = Description.MAX_LENGTH,
        optionality = Optionality.OPTIONAL
)
@ParameterLayout(
        multiLine = Description.MULTI_LINE
)
@javax.jdo.annotations.Column(length = Description.MAX_LENGTH, allowsNull = "true")
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {

    int MAX_LENGTH = 254;
    @AliasFor( annotation =  Property.class, attribute = "maxLength")
    int propertyMaxLength() default MAX_LENGTH;
    @AliasFor( annotation =  Parameter.class, attribute = "maxLength")
    int parameterMaxLength() default MAX_LENGTH;

    @AliasFor( annotation = Property.class, attribute = "optionality")
    Optionality propertyOptionality() default Optionality.OPTIONAL;
    @AliasFor( annotation = Parameter.class, attribute = "optionality")
    Optionality parameterOptionality() default Optionality.OPTIONAL;

    @AliasFor( annotation = javax.jdo.annotations.Column.class, attribute = "allowsNull")
    String columnAllowsNull() default "true";
    @AliasFor( annotation = javax.jdo.annotations.Column.class, attribute = "length")
    int columnLength() default MAX_LENGTH;

    int MULTI_LINE = 1;
    @AliasFor( annotation =  PropertyLayout.class, attribute = "multiLine")
    int propertyLayoutMultiLine() default MULTI_LINE;
    @AliasFor( annotation =  ParameterLayout.class, attribute = "multiLine")
    int parameterLayoutMultiLine() default MULTI_LINE;

}
