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
package org.apache.causeway.persistence.jdo.metamodel.facets.object.datastoreidentity;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.causeway.persistence.jdo.metamodel.testing.AbstractFacetFactoryTest;
import org.apache.causeway.persistence.jdo.provider.metamodel.facets.object.datastoreidentity.JdoDatastoreIdentityFacet;

import lombok.val;

class GivenJdoDatastoreIdentityAnnotationFacetFactoryTest
extends AbstractFacetFactoryTest {

    private JdoDatastoreIdentityAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        facetFactory = new JdoDatastoreIdentityAnnotationFacetFactory(metaModelContext, jdoFacetContext);
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testFeatureTypes() {
        val featureTypes = facetFactory.getFeatureTypes();
        assertTrue(contains(featureTypes, FeatureType.OBJECT));
        assertFalse(contains(featureTypes, FeatureType.PROPERTY));
        assertFalse(contains(featureTypes, FeatureType.COLLECTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION_PARAMETER_SINGULAR));
    }

    public void testDatastoreIdentityAnnotationPickedUpOnClass() {
        @DatastoreIdentity()
        abstract class Customer {
        }

        facetFactory.process(ProcessClassContext
                .forTesting(Customer.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(JdoDatastoreIdentityFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof JdoDatastoreIdentityFacetAnnotation);
    }

    public void testIfNoDatastoreIdentityAnnotationThenNoFacet() {

        abstract class Customer {
        }

        facetFactory.process(ProcessClassContext
                .forTesting(Customer.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(JdoDatastoreIdentityFacet.class);
        assertNull(facet);
    }

    public void testDatastoreIdentityAnnotationWithNoExplicitStrategyDefaultsToUnspecified() {
        @DatastoreIdentity()
        abstract class Customer {
        }
        facetFactory.process(ProcessClassContext
                .forTesting(Customer.class, methodRemover, facetHolder));

        final JdoDatastoreIdentityFacet entityFacet = facetHolder
                .getFacet(JdoDatastoreIdentityFacet.class);
        assertEquals(IdGeneratorStrategy.UNSPECIFIED, entityFacet.getStrategy());
    }

    public void testEntityAnnotationWithExplicitStrategyAttributeProvided() {
        @DatastoreIdentity(strategy=IdGeneratorStrategy.IDENTITY)
        abstract class Customer {
        }

        facetFactory.process(ProcessClassContext
                .forTesting(Customer.class, methodRemover, facetHolder));

        final JdoDatastoreIdentityFacet entityFacet = facetHolder
                .getFacet(JdoDatastoreIdentityFacet.class);
        assertEquals(IdGeneratorStrategy.IDENTITY, entityFacet.getStrategy());
    }

    public void testNoMethodsRemoved() {
        @PersistenceCapable
        abstract class Customer {
        }

        facetFactory.process(ProcessClassContext
                .forTesting(Customer.class, methodRemover, facetHolder));

        assertNoMethodsRemoved();
    }
}
