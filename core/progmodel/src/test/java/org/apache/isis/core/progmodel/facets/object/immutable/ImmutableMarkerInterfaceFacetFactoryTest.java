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


package org.apache.isis.core.progmodel.facets.object.immutable;

import org.apache.isis.applib.marker.AlwaysImmutable;
import org.apache.isis.applib.marker.ImmutableOncePersisted;
import org.apache.isis.applib.marker.ImmutableUntilPersisted;
import org.apache.isis.applib.marker.NeverImmutable;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.object.immutable.ImmutableFacetMarkerInterface;
import org.apache.isis.core.progmodel.facets.object.immutable.ImmutableMarkerInterfacesFacetFactory;


public class ImmutableMarkerInterfaceFacetFactoryTest extends AbstractFacetFactoryTest {

    private ImmutableMarkerInterfacesFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new ImmutableMarkerInterfacesFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    @Override
    public void testFeatureTypes() {
        final ObjectFeatureType[] featureTypes = facetFactory.getFeatureTypes();
        assertTrue(contains(featureTypes, ObjectFeatureType.OBJECT));
        assertFalse(contains(featureTypes, ObjectFeatureType.PROPERTY));
        assertFalse(contains(featureTypes, ObjectFeatureType.COLLECTION));
        assertFalse(contains(featureTypes, ObjectFeatureType.ACTION));
        assertFalse(contains(featureTypes, ObjectFeatureType.ACTION_PARAMETER));
    }

    public void testAlwaysImmutable() {
        class Customer implements AlwaysImmutable {}

        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ImmutableFacetMarkerInterface);
        final ImmutableFacetMarkerInterface immutableFacetMarkerInterface = (ImmutableFacetMarkerInterface) facet;
        assertEquals(org.apache.isis.core.metamodel.facets.When.ALWAYS, immutableFacetMarkerInterface.value());

        assertNoMethodsRemoved();
    }

    public void testImmutableOncePersisted() {
        class Customer implements ImmutableOncePersisted {}

        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ImmutableFacetMarkerInterface);
        final ImmutableFacetMarkerInterface immutableFacetMarkerInterface = (ImmutableFacetMarkerInterface) facet;
        assertEquals(org.apache.isis.core.metamodel.facets.When.ONCE_PERSISTED, immutableFacetMarkerInterface.value());

        assertNoMethodsRemoved();
    }

    public void testImmutableUntilPersisted() {
        class Customer implements ImmutableUntilPersisted {}

        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ImmutableFacetMarkerInterface);
        final ImmutableFacetMarkerInterface immutableFacetMarkerInterface = (ImmutableFacetMarkerInterface) facet;
        assertEquals(org.apache.isis.core.metamodel.facets.When.UNTIL_PERSISTED, immutableFacetMarkerInterface.value());

        assertNoMethodsRemoved();
    }

    public void testNeverImmutable() {
        class Customer implements NeverImmutable {}

        facetFactory.process(Customer.class, methodRemover, facetHolder);

        final Facet facet = facetHolder.getFacet(ImmutableFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ImmutableFacetMarkerInterface);
        final ImmutableFacetMarkerInterface immutableFacetMarkerInterface = (ImmutableFacetMarkerInterface) facet;
        assertEquals(org.apache.isis.core.metamodel.facets.When.NEVER, immutableFacetMarkerInterface.value());

        assertNoMethodsRemoved();
    }

}

