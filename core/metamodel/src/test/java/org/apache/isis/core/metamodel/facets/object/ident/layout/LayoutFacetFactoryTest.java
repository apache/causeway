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

package org.apache.isis.core.metamodel.facets.object.ident.layout;

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.layout.LayoutFacet;
import org.apache.isis.core.metamodel.facets.object.layout.LayoutFacetMethod;
import org.apache.isis.core.metamodel.facets.object.layout.LayoutFacetFactory;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class LayoutFacetFactoryTest extends AbstractFacetFactoryTest {

    private LayoutFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new LayoutFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testLayoutMethodPickedUpOnClassAndMethodRemoved() {
        class Customer {
            @SuppressWarnings("unused")
            public String layout() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "layout");

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(LayoutFacet.class);
        assertThat(facet, is(notNullValue()));
        assertThat(facet, is(instanceOf(LayoutFacetMethod.class)));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(method));
    }

}
