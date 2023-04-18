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
package org.apache.causeway.core.metamodel.facets.param.layout.annotation;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.core.metamodel.facets.FacetFactory;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.all.named.ParamNamedFacet;
import org.apache.causeway.core.metamodel.facets.param.layout.NamedFacetForParameterLayoutAnnotation;
import org.apache.causeway.core.metamodel.facets.param.layout.ParameterLayoutFacetFactory;

import lombok.val;

class NamedFacetForParameterLayoutAnnotationFactoryTest
extends FacetFactoryTestAbstract {

    private static final String NAME = "an action";

    public void testParameterLayoutAnnotationNamed() {
        final ParameterLayoutFacetFactory facetFactory = new ParameterLayoutFacetFactory(getMetaModelContext());

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@ParameterLayout(named = NAME) final String foo) {
            }
        }
        final Method method = findMethod(Customer.class, "someAction", new Class[]{String.class});

        facetFactory.processParams(FacetFactory.ProcessParameterContext.forTesting(
                Customer.class,
                IntrospectionPolicy.ANNOTATION_OPTIONAL,
                method, null, facetedMethodParameter));

        val facet = facetedMethodParameter.getFacet(ParamNamedFacet.class);
        assertThat(facet, is(notNullValue()));
        assertThat(facet, is(instanceOf(NamedFacetForParameterLayoutAnnotation.class)));
        assertEquals(NAME, facet.text());
    }

}
