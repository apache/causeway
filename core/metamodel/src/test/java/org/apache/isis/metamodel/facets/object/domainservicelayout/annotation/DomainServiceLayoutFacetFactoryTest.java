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

package org.apache.isis.metamodel.facets.object.domainservicelayout.annotation;

import static org.hamcrest.Matchers.is;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFacet;
import org.apache.isis.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFacetFactory;
import org.junit.Assert;

public class DomainServiceLayoutFacetFactoryTest extends AbstractFacetFactoryTest {

    private DomainServiceLayoutFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new DomainServiceLayoutFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testAnnotationPickedUpOnClass() {
        @DomainService
        @DomainServiceLayout(menuBar = DomainServiceLayout.MenuBar.SECONDARY)
        class Customers {
        }

        facetFactory.process(new ProcessClassContext(Customers.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(DomainServiceLayoutFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DomainServiceLayoutFacetAnnotation);
        DomainServiceLayoutFacetAnnotation domainServiceLayoutFacet = (DomainServiceLayoutFacetAnnotation) facet;
        //Assert.assertThat(domainServiceLayoutFacet.getMenuOrder(), is("123"));
        Assert.assertThat(domainServiceLayoutFacet.getMenuBar(), is(DomainServiceLayout.MenuBar.SECONDARY));

        assertNoMethodsRemoved();
    }


    public void testDomainServiceMenuOrderAnnotationPickedUpOnClass() {
        @DomainService//(menuOrder = "123")
        class Customers {
        }

        facetFactory.process(new ProcessClassContext(Customers.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(DomainServiceLayoutFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DomainServiceLayoutFacetAnnotation);
        DomainServiceLayoutFacetAnnotation domainServiceLayoutFacet = (DomainServiceLayoutFacetAnnotation) facet;
        //Assert.assertThat(domainServiceLayoutFacet.getMenuOrder(), is("123"));

        assertNoMethodsRemoved();
    }

    public void testDomainServiceAndDomainServiceLayoutAnnotationWhenCompatiblePickedUpOnClass() {
        @DomainService//(menuOrder = "123")
        @DomainServiceLayout(menuBar = DomainServiceLayout.MenuBar.SECONDARY)
        class Customers {
        }

        facetFactory.process(new ProcessClassContext(Customers.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(DomainServiceLayoutFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DomainServiceLayoutFacetAnnotation);
        DomainServiceLayoutFacetAnnotation domainServiceLayoutFacet = (DomainServiceLayoutFacetAnnotation) facet;
        //Assert.assertThat(domainServiceLayoutFacet.getMenuOrder(), is("123"));
        Assert.assertThat(domainServiceLayoutFacet.getMenuBar(), is(DomainServiceLayout.MenuBar.SECONDARY));

        assertNoMethodsRemoved();
    }

    public void testDomainServiceAndDomainServiceLayoutAnnotation_takes_the_minimum() {
        @DomainService//(menuOrder = "1")
        @DomainServiceLayout(menuBar = DomainServiceLayout.MenuBar.SECONDARY)
        class Customers {
        }

        facetFactory.process(new ProcessClassContext(Customers.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(DomainServiceLayoutFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DomainServiceLayoutFacetAnnotation);
        DomainServiceLayoutFacetAnnotation domainServiceLayoutFacet = (DomainServiceLayoutFacetAnnotation) facet;
        //Assert.assertThat(domainServiceLayoutFacet.getMenuOrder(), is("1"));
        Assert.assertThat(domainServiceLayoutFacet.getMenuBar(), is(DomainServiceLayout.MenuBar.SECONDARY));

        assertNoMethodsRemoved();
    }


}
