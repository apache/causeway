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

import org.apache.isis.applib.annotation.Snapshot;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.propcoll.memserexcl.SnapshotExcludeFacet;
import org.apache.isis.core.metamodel.facets.properties.property.notpersisted.SnapshotExcludeFacetForPropertyAnnotation;

import lombok.val;

public class NotPersistedAnnotationOnPropertyFacetFactoryTest extends AbstractFacetFactoryTest {

    private PropertyAnnotationFacetFactory facetFactory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        facetFactory = new PropertyAnnotationFacetFactory();
    }

    private void processNotPersisted(
            PropertyAnnotationFacetFactory facetFactory, FacetFactory.ProcessMethodContext processMethodContext) {
        val propertyIfAny = processMethodContext.synthesizeOnMethod(Property.class);
        facetFactory.processNotPersisted(processMethodContext, propertyIfAny);
    }

    public void testAnnotationPickedUpOnProperty() {

        class Customer {
            @SuppressWarnings("unused")
            @Property(snapshot = Snapshot.EXCLUDED)
            public String getFirstName() {
                return null;
            }
        }
        final Method method = findMethod(Customer.class, "getFirstName");

        processNotPersisted(facetFactory, new FacetFactory.ProcessMethodContext(Customer.class, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(SnapshotExcludeFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof SnapshotExcludeFacetForPropertyAnnotation);

        assertNoMethodsRemoved();
    }


}
