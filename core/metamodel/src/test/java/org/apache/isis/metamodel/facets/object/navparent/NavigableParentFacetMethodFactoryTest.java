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

package org.apache.isis.metamodel.facets.object.navparent;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.metamodel.facets.object.navparent.NavigableParentFacet;
import org.apache.isis.metamodel.facets.object.navparent.method.NavigableParentFacetMethod;
import org.apache.isis.metamodel.facets.object.navparent.method.NavigableParentFacetMethodFactory;

public class NavigableParentFacetMethodFactoryTest extends AbstractFacetFactoryTest {

    private NavigableParentFacetMethodFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new NavigableParentFacetMethodFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testNavigableParentMethodPickedUpOnClassAndMethodRemoved() {
        class Customer {
            @SuppressWarnings("unused")
            public Object parent() {
                return null;
            }
        }
        final Method navigableParentMethod = findMethod(Customer.class, "parent");

        facetFactory.process(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(NavigableParentFacet.class);
        assertThat(facet, is(notNullValue()));
        assertThat(facet, is(instanceOf(NavigableParentFacetMethod.class)));

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(navigableParentMethod));
    }

}
