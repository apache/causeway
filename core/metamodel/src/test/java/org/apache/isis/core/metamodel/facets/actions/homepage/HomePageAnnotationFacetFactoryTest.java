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

package org.apache.isis.core.metamodel.facets.actions.homepage;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.HomePage;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actions.homepage.annotation.HomePageFacetAnnotation;
import org.apache.isis.core.metamodel.facets.actions.homepage.annotation.HomePageFacetAnnotationFactory;

public class HomePageAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private HomePageFacetAnnotationFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new HomePageFacetAnnotationFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testHomePageAnnotationPickedUpOnAction() {
        class HomePageService {
            @HomePage
            public Object lookup() { return null; }
        }

        final Method actionMethod = findMethod(HomePageService.class, "lookup");

        facetFactory.process(new ProcessMethodContext(Customer.class, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(HomePageFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof HomePageFacetAnnotation);

        assertNoMethodsRemoved();
    }


}
