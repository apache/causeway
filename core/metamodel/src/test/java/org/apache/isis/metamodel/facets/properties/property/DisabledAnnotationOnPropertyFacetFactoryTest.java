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

package org.apache.isis.metamodel.facets.properties.property;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.metamodel.facets.members.disabled.DisabledFacetAbstract;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DisabledAnnotationOnPropertyFacetFactoryTest extends AbstractFacetFactoryTest {

    private PropertyAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new PropertyAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testDisabledAnnotationPickedUpOnProperty() {
        class Customer {
            @Property(editing = Editing.DISABLED)
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getNumberOfOrders");

        facetFactory.processEditing(new ProcessMethodContext(Customer.class, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisabledFacetAbstract);

        final DisabledFacet disabledFacet = (DisabledFacet) facet;
        assertThat(disabledFacet.disabledReason(null), is("Always disabled"));

        assertNoMethodsRemoved();
    }

    public void testDisabledAnnotationWithReason() {
        class Customer {
            @Property(editing = Editing.DISABLED, editingDisabledReason = "Oh no you don't!")
            public int getNumberOfOrders() {
                return 0;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "getNumberOfOrders");

        facetFactory.processEditing(new ProcessMethodContext(Customer.class, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisabledFacetAbstract);

        final DisabledFacet disabledFacet = (DisabledFacet) facet;
        assertThat(disabledFacet.disabledReason(null), is("Oh no you don't!"));

        assertNoMethodsRemoved();
    }

}
