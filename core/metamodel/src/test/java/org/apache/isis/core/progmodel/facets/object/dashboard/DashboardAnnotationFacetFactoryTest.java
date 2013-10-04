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

package org.apache.isis.core.progmodel.facets.object.dashboard;

import org.apache.isis.applib.annotation.Dashboard;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.dashboard.DashboardFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;

public class DashboardAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private DashboardAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new DashboardAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testDashboardAnnotationPickedUpOnClass() {
        @Dashboard
        class Customer {
        }

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DashboardFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DashboardFacetAnnotation);
        final DashboardFacetAnnotation notPersistableFacetAnnotation = (DashboardFacetAnnotation) facet;

        assertNoMethodsRemoved();
    }


}
