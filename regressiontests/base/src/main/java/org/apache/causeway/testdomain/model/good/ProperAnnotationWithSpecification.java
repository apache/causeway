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
package org.apache.causeway.testdomain.model.good;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.spec.AbstractSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

@Property(
        maxLength = ProperAnnotationWithSpecification.MAX_LEN,
        mustSatisfy = ProperAnnotationWithSpecification.ProperSpecification.class)
@PropertyLayout(named = "Proper Name")
@Parameter(
        maxLength = ProperAnnotationWithSpecification.MAX_LEN,
        mustSatisfy = ProperAnnotationWithSpecification.ProperSpecification.class)
@ParameterLayout(named = "Proper Name")
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ProperAnnotationWithSpecification {

    int MAX_LEN = 40;

    // used for testing whether injection works
    class ProperSpecification extends AbstractSpecification<String> {

        @Inject private SpecificationLoader specLoader;

        @Override
        public String satisfiesSafely(final String obj) {
            return specLoader!=null
                    ? "injection worked"
                    : "injection failed";
        }
    }

}
