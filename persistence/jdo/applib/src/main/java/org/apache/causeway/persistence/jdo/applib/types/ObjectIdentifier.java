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
package org.apache.causeway.persistence.jdo.applib.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.inject.Named;

import org.springframework.core.annotation.AliasFor;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;

/**
 * Meta-annotation for a mandatory {@link String} property or parameter representing a
 * the identity of a domain object (with respect to its logical type,
 * as per @{@link DomainObject#logicalTypeName()}
 * or @{@link Named#value()}) and consorts.
 *
 * @see LogicalTypeName
 *
 * @since 2.0 {@index}
 */
@Property(
        maxLength = ObjectIdentifier.MAX_LENGTH,
        optionality = Optionality.MANDATORY
)
@PropertyLayout(
)
@Parameter(
        maxLength = ObjectIdentifier.MAX_LENGTH,
        optionality = Optionality.MANDATORY
)
@ParameterLayout(
)
@javax.jdo.annotations.Column(length = ObjectIdentifier.MAX_LENGTH, allowsNull = "false")
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectIdentifier {

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
