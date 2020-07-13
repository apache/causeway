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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.metamodel.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelInitFilterDefault;
import org.apache.isis.core.metamodel.progmodels.dflt.ProgrammingModelFacetsJava8;

import lombok.val;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Detached from its module because we need the javac -parameter flag set when compiling this test
 */
public class ParameterNameFacetTest extends AbstractFacetFactoryJUnit4TestCase {

    ProgrammingModelFacetsJava8 programmingModel;
    Method actionMethod;

    @Before
    public void setUp() throws Exception {

        val mockServiceInjector = Mockito.mock(ServiceInjector.class);

        programmingModel = new ProgrammingModelFacetsJava8(mockServiceInjector);

        val metaModelContext = MetaModelContext_forTesting.builder().build();

        ((ProgrammingModelAbstract)programmingModel)
        .init(new ProgrammingModelInitFilterDefault(), metaModelContext);

        super.setUpFacetedMethodAndParameter();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        programmingModel = null;
    }

//    @Test   fails with ISIS.2374
    public void someActionParameterShouldHaveProperName() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final String anAwesomeName) { }
        }

        // given
        actionMethod = findMethod(Customer.class, "someAction", new Class[]{String.class} );

        // when
        final FacetFactory.ProcessParameterContext processParameterContext =
                new FacetFactory.ProcessParameterContext(
                        Customer.class, actionMethod, 0, null, facetedMethodParameter);
        programmingModel.streamFactories()
        .forEach(facetFactory->facetFactory.processParams(processParameterContext));

        // then
        final NamedFacet namedFacet = facetedMethodParameter.getFacet(NamedFacet.class);
        assertThat(namedFacet.value(), is("An Awesome Name"));

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
        final FacetFactory.ProcessParameterContext processParameterContext =
                new FacetFactory.ProcessParameterContext(
                        Customer.class, actionMethod, 0, null, facetedMethodParameter);
        programmingModel.streamFactories().forEach(facetFactory->facetFactory.processParams(processParameterContext));

        // then
        final NamedFacet namedFacet = facetedMethodParameter.getFacet(NamedFacet.class);
        Assert.assertNotNull(namedFacet);
        assertThat(namedFacet.value(), is("Even Better Name"));

    }

}
