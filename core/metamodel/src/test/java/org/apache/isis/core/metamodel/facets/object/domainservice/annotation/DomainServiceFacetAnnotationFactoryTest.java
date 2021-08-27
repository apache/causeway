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
package org.apache.isis.core.metamodel.facets.object.domainservice.annotation;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;

public class DomainServiceFacetAnnotationFactoryTest
extends AbstractFacetFactoryTest {

    private DomainServiceFacetAnnotationFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new DomainServiceFacetAnnotationFactory(metaModelContext);
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testAggregatedAnnotationPickedUpOnClass() {

        @DomainService
        class Customers {
        }

        facetFactory.process(ProcessClassContext
                .forTesting(Customers.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(DomainServiceFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DomainServiceFacetAnnotation);
        DomainServiceFacetAnnotation domainServiceFacet = (DomainServiceFacetAnnotation) facet;
        assertNotNull(domainServiceFacet);

        assertNoMethodsRemoved();
    }

}
