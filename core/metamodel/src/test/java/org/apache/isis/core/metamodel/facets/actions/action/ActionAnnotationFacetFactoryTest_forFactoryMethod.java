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

package org.apache.isis.core.metamodel.facets.actions.action;

import java.lang.reflect.Method;

import org.jmock.integration.junit4.JMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actions.action.factorymethod.FactoryMethodFacet;
import org.apache.isis.core.metamodel.facets.actions.action.factorymethod.FactoryMethodFacetForActionAnnotation;

import static org.hamcrest.CoreMatchers.containsString;

@RunWith(JMock.class)
public class ActionAnnotationFacetFactoryTest_forFactoryMethod extends AbstractFacetFactoryTest {

    ActionAnnotationFacetFactory facetFactory;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        facetFactory = new ActionAnnotationFacetFactory();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    @Test
    public void happyCase() {
        class Concert {

            @Action(factoryMethod=true)
            public Concert someAction() {
                return new Concert();
            }
        }

        final Method actionMethod = findMethod(Concert.class, "someAction");

        facetFactory.processFactoryMethod(new ProcessMethodContext(Concert.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(FactoryMethodFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof FactoryMethodFacetForActionAnnotation);
        assertTrue(((FactoryMethodFacetForActionAnnotation) facet).value().isAssignableFrom(Concert.class));

        assertNoMethodsRemoved();
    }

    @Test
    public void testNoAnnotation() {

        class Concert {
            public void someAction() {
            }
        }

        final Method actionMethod = findMethod(Concert.class, "someAction");

        facetFactory.processFactoryMethod(new ProcessMethodContext(Concert.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(FactoryMethodFacet.class);
        assertNull(facet);

        assertNoMethodsRemoved();
    }

    @Test
    public void testAnnotationWithoutParamReturningVoid() {
        class Concert {

            @Action
            public void someAction() {
            }
        }

        final Method actionMethod = findMethod(Concert.class, "someAction");

        facetFactory.processFactoryMethod(new ProcessMethodContext(Concert.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(FactoryMethodFacet.class);
        assertNull(facet);

        assertNoMethodsRemoved();
    }

    @Test
    public void testAnnotationWithoutParamNotReturningVoid() {
        class Concert {

            @Action
            public Concert someAction() {
                return new Concert();
            }
        }

        final Method actionMethod = findMethod(Concert.class, "someAction");

        facetFactory.processFactoryMethod(new ProcessMethodContext(Concert.class, null, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(FactoryMethodFacet.class);
        assertNull(facet);

        assertNoMethodsRemoved();
    }

    @Rule public ExpectedException exception = ExpectedException.none();

    @Test
    public void testAnnotationWithFactoryMethodReturnsVoid_throwsException() {

        class Concert {

            @Action(factoryMethod = true)
            public void someAction() {
            }
        }

        // given
        final Method actionMethod = findMethod(Concert.class, "someAction");

        // expect
        exception.expect(MetaModelException.class);
        exception.expectMessage(containsString("Concert"));
        exception.expectMessage(containsString("someAction"));

        // when
        facetFactory.processFactoryMethod(new ProcessMethodContext(Concert.class, null, null, actionMethod, methodRemover, facetedMethod));

        // then
        // Should never arrive here. Exception thrown before
        assertTrue(false);
    }

}
