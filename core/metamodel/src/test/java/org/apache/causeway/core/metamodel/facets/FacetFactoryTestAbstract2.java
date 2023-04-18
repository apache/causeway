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
package org.apache.causeway.core.metamodel.facets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;

@Deprecated //TODO[CAUSEWAY-3409] use FacetFactoryTestAbstract scenarios instead
public abstract class FacetFactoryTestAbstract2
extends FacetFactoryTestAbstract {

    public static class Customer {

        private String firstName;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(final String firstName) {
            this.firstName = firstName;
        }
    }

    protected FacetHolder facetHolder;
    protected FacetedMethod facetedMethod;
    protected FacetedMethodParameter facetedMethodParameter;

    @BeforeEach
    protected void setUpAll2() {
        facetHolder = FacetHolder.simple(
                getMetaModelContext(),
                Identifier.propertyIdentifier(LogicalType.fqcn(Customer.class), "firstName"));

        facetedMethod = FacetedMethod.createSetterForProperty(getMetaModelContext(), Customer.class, "firstName");
        facetedMethodParameter = new FacetedMethodParameter(
                getMetaModelContext(),
                FeatureType.ACTION_PARAMETER_SINGULAR,
                facetedMethod.getOwningType(),
                facetedMethod.getMethod(), 0);
    }

    @AfterEach
    protected void tearDownAll2() {
        facetedMethod = null;
    }

    protected void processMethod(
            final FacetFactory facetFactory,
            final Class<?> type,
            final String methodName,
            final Class<?>[] signature) {

        facetFactory.process(ProcessMethodContext
                .forTesting(type, null,
                        findMethod(type, methodName, signature),
                        methodRemover, facetedMethod));
    }

    protected void processParams(
            final FacetFactory facetFactory,
            final Class<?> type,
            final String methodName,
            final Class<?>[] signature) {

        facetFactory.processParams(FacetFactory.ProcessParameterContext
                .forTesting(type, IntrospectionPolicy.ANNOTATION_OPTIONAL,
                        findMethod(type, methodName, signature),
                        null, facetedMethodParameter));
    }
}
