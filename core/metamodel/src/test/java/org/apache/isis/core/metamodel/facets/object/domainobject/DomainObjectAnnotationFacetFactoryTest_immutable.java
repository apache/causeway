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

package org.apache.isis.core.metamodel.facets.object.domainobject;

import org.apache.isis.applib.annotation.Immutable;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.immutableannot.ImmutableFacetForImmutableAnnotation;

public class DomainObjectAnnotationFacetFactoryTest_immutable extends AbstractFacetFactoryTest {

    private DomainObjectAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new DomainObjectAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testImmutableAnnotationPickedUpOnClassAndDefaultsToAlways() {
        @Immutable
        class Customer {
        }

        facetFactory.processEditing(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ImmutableFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ImmutableFacetForImmutableAnnotation);
        final ImmutableFacetForImmutableAnnotation immutableFacetAnnotation = (ImmutableFacetForImmutableAnnotation) facet;
        assertEquals(When.ALWAYS, immutableFacetAnnotation.when());

        assertNoMethodsRemoved();
    }

    public void testImmutableAnnotationAlwaysPickedUpOnClass() {
        @Immutable(When.ALWAYS)
        class Customer {
        }

        facetFactory.processEditing(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ImmutableFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ImmutableFacetForImmutableAnnotation);
        final ImmutableFacetForImmutableAnnotation immutableFacetAnnotation = (ImmutableFacetForImmutableAnnotation) facet;
        assertEquals(When.ALWAYS, immutableFacetAnnotation.when());

        assertNoMethodsRemoved();
    }

    public void testImmutableAnnotationNeverPickedUpOnClass() {
        @Immutable(When.NEVER)
        class Customer {
        }

        facetFactory.processEditing(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ImmutableFacet.class);
        final ImmutableFacetForImmutableAnnotation immutableFacetAnnotation = (ImmutableFacetForImmutableAnnotation) facet;
        assertEquals(When.NEVER, immutableFacetAnnotation.when());

        assertNoMethodsRemoved();
    }

    public void testImmutableAnnotationOncePersistedPickedUpOnClass() {
        @Immutable(When.ONCE_PERSISTED)
        class Customer {
        }

        facetFactory.processEditing(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ImmutableFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ImmutableFacetForImmutableAnnotation);
        final ImmutableFacetForImmutableAnnotation immutableFacetAnnotation = (ImmutableFacetForImmutableAnnotation) facet;
        assertEquals(When.ONCE_PERSISTED, immutableFacetAnnotation.when());

        assertNoMethodsRemoved();
    }

    public void testImmutableAnnotationUntilPersistedPickedUpOnClass() {
        @Immutable(When.UNTIL_PERSISTED)
        class Customer {
        }

        facetFactory.processEditing(new ProcessClassContext(Customer.class, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ImmutableFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ImmutableFacetForImmutableAnnotation);
        final ImmutableFacetForImmutableAnnotation immutableFacetAnnotation = (ImmutableFacetForImmutableAnnotation) facet;
        assertEquals(When.UNTIL_PERSISTED, immutableFacetAnnotation.when());

        assertNoMethodsRemoved();
    }

}
