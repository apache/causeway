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
package org.apache.isis.core.metamodel.facets.param.name;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.isis.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.all.named.ParamNamedFacet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;

import lombok.val;

/**
 * needs the javac -parameter flag set when compiling this test
 */
public class ParameterNameFacetTest
extends AbstractFacetFactoryJUnit4TestCase {

    ProgrammingModel programmingModel;
    Method actionMethod;

    @Before
    public void setUp() throws Exception {

        val metaModelContext = MetaModelContext_forTesting.builder()
                .build();

        super.setUpFacetedMethodAndParameter();

        programmingModel = metaModelContext.getProgrammingModel();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        programmingModel = null;
    }

    @Test
    public void verifyProgrammingModelNumberOfFactories() {
        assertEquals(63, programmingModel.streamFactories().count());
    }

    @Test //verify we have the javac -parameter flag set when compiling this class
    public void verifyTestEnvironmentIsSetupCorrectly() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final String anAwesomeName) { }
        }

        val someAction = _Reflect.streamAllMethods(Customer.class, false)
        .filter(method->method.getName().equals("someAction"))
        .findFirst()
        .get();

        assertEquals("anAwesomeName", someAction.getParameters()[0].getName());
    }

    @Test
    public void someActionParameterShouldHaveProperName() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final String anAwesomeName) { }
        }

        // given
        actionMethod = findMethod(Customer.class, "someAction", new Class[]{String.class} );

        // when
        val processParameterContext =
                new FacetFactory.ProcessParameterContext(
                        Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, actionMethod, null, facetedMethodParameter);

        programmingModel.streamFactories()
        .forEach(facetFactory->facetFactory.processParams(processParameterContext));

        // then
        val namedFacet = facetedMethodParameter.getFacet(ParamNamedFacet.class);

        assertEquals("An Awesome Name", namedFacet.text());

    }

    @Test
    public void explicitNameShouldTakePrecedenceOverReflective() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(
                    @ParameterLayout(
                            named = "Even Better Name"
                            )
                    final String anAwesomeName) { }
        }



        // given
        actionMethod = findMethod(Customer.class, "someAction", new Class[]{String.class} );

        // when
        val processParameterContext =
                new FacetFactory.ProcessParameterContext(
                        Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, actionMethod, null, facetedMethodParameter);
        programmingModel.streamFactories().forEach(facetFactory->facetFactory.processParams(processParameterContext));

        // then
        val namedFacet = facetedMethodParameter.getFacet(ParamNamedFacet.class);
        assertNotNull(namedFacet);
        assertEquals("Even Better Name", namedFacet.text());

    }

}
