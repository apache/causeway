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
package org.apache.isis.core.metamodel.facets.properties.property;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotations.Property;
import org.apache.isis.applib.annotations.Snapshot;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.propcoll.memserexcl.SnapshotExcludeFacet;
import org.apache.isis.core.metamodel.facets.properties.property.snapshot.SnapshotExcludeFacetForPropertyAnnotation;

import lombok.val;

public class PropertyAnnotationWithSnapshotOnPropertyFacetFactoryTest
extends AbstractFacetFactoryTest {

    private PropertyAnnotationFacetFactory facetFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        facetFactory = new PropertyAnnotationFacetFactory(metaModelContext);
    }

    private void processNotPersisted(
            final PropertyAnnotationFacetFactory facetFactory, final FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processSnapshot(processMethodContext, propertyIfAny);
    }

    public void testAnnotationPickedUpOnProperty() {

        class Customer {
            @Property(snapshot = Snapshot.EXCLUDED)
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        processNotPersisted(facetFactory, ProcessMethodContext
                .forTesting(Customer.class, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(SnapshotExcludeFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof SnapshotExcludeFacetForPropertyAnnotation);

        assertNoMethodsRemoved();
    }


}
