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
package org.apache.isis.metamodel.facets.object.domainservice.annotation;

import org.junit.Assert;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.metamodel.facets.object.domainservice.annotation.DomainServiceFacetAnnotation;
import org.apache.isis.metamodel.facets.object.domainservice.annotation.DomainServiceFacetAnnotationFactory;

import static org.apache.isis.core.commons.matchers.IsisMatchers.classEqualTo;

public class DomainServiceFacetAnnotationFactoryTest extends AbstractFacetFactoryTest {

    private DomainServiceFacetAnnotationFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new DomainServiceFacetAnnotationFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testAggregatedAnnotationPickedUpOnClass() {
        class Customer {
        }
        @DomainService(menuOrder = "123", repositoryFor = Customer.class)
        class Customers {
        }

        facetFactory.process(new ProcessClassContext(Customers.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(DomainServiceFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DomainServiceFacetAnnotation);
        DomainServiceFacetAnnotation domainServiceFacet = (DomainServiceFacetAnnotation) facet;
        Assert.assertThat(domainServiceFacet.getRepositoryFor(), classEqualTo(Customer.class));

        assertNoMethodsRemoved();
    }

}
