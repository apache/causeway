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
package org.apache.isis.persistence.jdo.metamodel.facets.object.persistencecapable;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.ObjectTypeFacetFactory;
import org.apache.isis.persistence.jdo.metamodel.testing.AbstractFacetFactoryTest;
import org.apache.isis.persistence.jdo.provider.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

import lombok.val;

public class GivenJdoPersistenceCapableAnnotationFacetFactoryTest
extends AbstractFacetFactoryTest {

    private JdoPersistenceCapableAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new JdoPersistenceCapableAnnotationFacetFactory(metaModelContext, jdoFacetContext);
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
        assertFalse(contains(featureTypes, FeatureType.ACTION_PARAMETER_SCALAR));
    }

    public void testPersistenceCapableAnnotationPickedUpOnClass() {
        @PersistenceCapable
        abstract class Customer {
        }

        facetFactory.process(new ObjectTypeFacetFactory.ProcessObjectTypeContext(Customer.class, facetHolder));
        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(JdoPersistenceCapableFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof JdoPersistenceCapableFacetAnnotation);
    }

    public void testIfNoPersistenceCapableAnnotationThenNoFacet() {

        abstract class Customer {
        }

        facetFactory.process(new ObjectTypeFacetFactory.ProcessObjectTypeContext(Customer.class, facetHolder));
        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(JdoPersistenceCapableFacet.class);
        assertNull(facet);
    }

    public void testEntityAnnotationWithNoExplicitNameDefaultsToClassName() {
        @PersistenceCapable()
        abstract class Customer {
        }

        facetFactory.process(new ObjectTypeFacetFactory.ProcessObjectTypeContext(Customer.class, facetHolder));
        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        final JdoPersistenceCapableFacet entityFacet = facetHolder
                .getFacet(JdoPersistenceCapableFacet.class);
        assertEquals("Customer", entityFacet.getTable());
    }

    public void testPersistenceCapableAnnotationWithNoExplicitIdentityTypeDefaultsToUnspecified() {
        @PersistenceCapable()
        abstract class Customer {
        }

        facetFactory.process(new ObjectTypeFacetFactory.ProcessObjectTypeContext(Customer.class, facetHolder));
        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        final JdoPersistenceCapableFacet entityFacet = facetHolder
                .getFacet(JdoPersistenceCapableFacet.class);
        assertEquals(IdentityType.UNSPECIFIED, entityFacet.getIdentityType());
    }

    public void testPersistenceCapableAnnotationWithExplicitNameAttributeProvided() {
        @PersistenceCapable(table = "CUS_CUSTOMER")
        abstract class Customer {
        }

        facetFactory.process(new ObjectTypeFacetFactory.ProcessObjectTypeContext(Customer.class, facetHolder));
        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        final JdoPersistenceCapableFacet entityFacet = facetHolder
                .getFacet(JdoPersistenceCapableFacet.class);
        assertEquals("CUS_CUSTOMER", entityFacet.getTable());
    }

    public void testPersistenceCapableAnnotationWithExplicitIdentityTypeAttributeProvided() {
        @PersistenceCapable(identityType=IdentityType.DATASTORE)
        abstract class Customer {
        }

        facetFactory.process(new ObjectTypeFacetFactory.ProcessObjectTypeContext(Customer.class, facetHolder));
        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        final JdoPersistenceCapableFacet entityFacet = facetHolder
                .getFacet(JdoPersistenceCapableFacet.class);
        assertEquals(IdentityType.DATASTORE, entityFacet.getIdentityType());
    }

    public void testNoMethodsRemoved() {
        @PersistenceCapable
        abstract class Customer {
        }

        facetFactory.process(new ObjectTypeFacetFactory.ProcessObjectTypeContext(Customer.class, facetHolder));
        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        assertNoMethodsRemoved();
    }
}
