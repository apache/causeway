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
package org.apache.causeway.core.metamodel.facets.param.name;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.all.named.ParamNamedFacet;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.mmtestsupport.MetaModelContext_forTesting;

/**
 * needs the javac -parameter flag set when compiling this test
 */
class ParameterNameFacetTest
extends FacetFactoryTestAbstract {

    protected ProgrammingModel programmingModel;

    @BeforeEach
    public void setUp() throws Exception {
        programmingModel = ((MetaModelContext_forTesting)getMetaModelContext()).getProgrammingModel();
    }

    @AfterEach
    public void tearDown() throws Exception {
        programmingModel = null;
    }

    @Test
    public void verifyProgrammingModelNumberOfFactories() {
        assertEquals(61, programmingModel.streamFactories().count());
    }

    @Test //verify we have the javac -parameter flag set when compiling this class
    public void verifyTestEnvironmentIsSetupCorrectly() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final String anAwesomeName) { }
        }

        var someAction = _Reflect.streamAllMethods(Customer.class, false)
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
        parameterScenario(Customer.class, "someAction", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            programmingModel.streamFactories()
            .forEach(facetFactory->facetFactory.processParams(processParameterContext));
            // then
            var namedFacet = facetedMethodParameter.getFacet(ParamNamedFacet.class);
            assertEquals("An Awesome Name", namedFacet.text());
        });
    }

    @Test
    public void explicitNameShouldTakePrecedenceOverReflective() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(
                    @ParameterLayout(named = "Even Better Name")
                    final String anAwesomeName) { }
        }

        // given
        parameterScenario(Customer.class, "someAction", 0, (processParameterContext, facetHolder, facetedMethod, facetedMethodParameter)->{
            // when
            programmingModel.streamFactories().forEach(facetFactory->facetFactory.processParams(processParameterContext));
            // then
            var namedFacet = facetedMethodParameter.getFacet(ParamNamedFacet.class);
            assertNotNull(namedFacet);
            assertEquals("Even Better Name", namedFacet.text());
        });

    }

}
