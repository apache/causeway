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

package org.apache.isis.core.metamodel.facets.naming.named;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Collection;

import org.junit.Test;

import org.apache.isis.applib.annotation.Named;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessParameterContext;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetAbstract;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.members.named.annotprop.NamedFacetOnMemberFactory;
import org.apache.isis.core.metamodel.facets.object.named.annotation.NamedFacetOnTypeAnnotationFactory;
import org.apache.isis.core.metamodel.facets.param.named.annotation.NamedFacetOnParameterAnnotationFactory;

public class NamedAnnotationFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    @Test
    public void testNamedAnnotationPickedUpOnClass() {

        final NamedFacetOnTypeAnnotationFactory facetFactory = new NamedFacetOnTypeAnnotationFactory();
        facetFactory.setSpecificationLookup(mockSpecificationLoaderSpi);

        @Named("some name")
        class Customer {
        }
        
        expectNoMethodsRemoved();
        
        facetFactory.process(new ProcessClassContext(Customer.class, null, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NamedFacet.class);
        assertThat(facet, is(not(nullValue())));
        assertThat(facet instanceof NamedFacetAbstract, is(true));
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertThat(namedFacetAbstract.value(), equalTo("some name"));
    }

    @Test
    public void testNamedAnnotationPickedUpOnProperty() {

        final NamedFacetOnMemberFactory facetFactory = new NamedFacetOnMemberFactory();
        facetFactory.setSpecificationLookup(mockSpecificationLoaderSpi);

        class Customer {
            @SuppressWarnings("unused")
            @Named("some name")
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getNumberOfOrders");

        expectNoMethodsRemoved();

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NamedFacet.class);
        assertThat(facet, is(not(nullValue())));
        assertThat(facet instanceof NamedFacetAbstract, is(true));
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertThat(namedFacetAbstract.value(), equalTo("some name"));
    }

    public void testNamedAnnotationPickedUpOnCollection() {
        final NamedFacetOnMemberFactory facetFactory = new NamedFacetOnMemberFactory();
        facetFactory.setSpecificationLookup(mockSpecificationLoaderSpi);

        class Customer {
            @SuppressWarnings("unused")
            @Named("some name")
            public Collection<?> getOrders() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getOrders");

        expectNoMethodsRemoved();

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NamedFacet.class);
        assertThat(facet, is(not(nullValue())));
        assertThat(facet instanceof NamedFacetAbstract, is(true));
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertThat(namedFacetAbstract.value(), equalTo("some name"));
    }

    public void testNamedAnnotationPickedUpOnAction() {
        final NamedFacetOnMemberFactory facetFactory = new NamedFacetOnMemberFactory();
        facetFactory.setSpecificationLookup(mockSpecificationLoaderSpi);

        class Customer {
            @SuppressWarnings("unused")
            @Named("some name")
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        expectNoMethodsRemoved();

        facetFactory.process(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NamedFacet.class);
        assertThat(facet, is(not(nullValue())));
        assertThat(facet instanceof NamedFacetAbstract, is(true));
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertThat(namedFacetAbstract.value(), equalTo("some name"));
    }

    public void testNamedAnnotationPickedUpOnActionParameter() {

        final NamedFacetOnParameterAnnotationFactory facetFactory = new NamedFacetOnParameterAnnotationFactory();
        facetFactory.setSpecificationLookup(mockSpecificationLoaderSpi);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(@Named("some name") final int x) {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class });

        expectNoMethodsRemoved();

        facetFactory.processParams(new ProcessParameterContext(actionMethod, 0, facetedMethodParameter));

        final Facet facet = facetedMethodParameter.getFacet(NamedFacet.class);
        assertThat(facet, is(not(nullValue())));
        assertThat(facet instanceof NamedFacetAbstract, is(true));
        final NamedFacetAbstract namedFacetAbstract = (NamedFacetAbstract) facet;
        assertThat(namedFacetAbstract.value(), equalTo("some name"));
    }

}
