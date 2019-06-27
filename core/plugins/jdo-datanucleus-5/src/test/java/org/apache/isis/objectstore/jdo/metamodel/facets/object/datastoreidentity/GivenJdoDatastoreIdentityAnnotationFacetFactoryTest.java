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
package org.apache.isis.objectstore.jdo.metamodel.facets.object.datastoreidentity;

import java.util.List;

import javax.jdo.annotations.DatastoreIdentity;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.datanucleus.enhancement.Persistable;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.FacetFactory;

import junit.framework.Assert;


public class GivenJdoDatastoreIdentityAnnotationFacetFactoryTest extends
        AbstractFacetFactoryTest {

    private JdoDatastoreIdentityAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new JdoDatastoreIdentityAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testFeatureTypes() {
        final List<FeatureType> featureTypes = facetFactory
                .getFeatureTypes();
        Assert.assertTrue(contains(featureTypes, FeatureType.OBJECT));
        assertFalse(contains(featureTypes, FeatureType.PROPERTY));
        assertFalse(contains(featureTypes, FeatureType.COLLECTION));
        Assert.assertFalse(contains(featureTypes, FeatureType.ACTION));
        assertFalse(contains(featureTypes,
                FeatureType.ACTION_PARAMETER_SCALAR));
    }

    public void testDatastoreIdentityAnnotationPickedUpOnClass() {
        @DatastoreIdentity()
        abstract class Customer implements Persistable {
        }

        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(JdoDatastoreIdentityFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof JdoDatastoreIdentityFacetAnnotation);
    }

    public void testIfNoDatastoreIdentityAnnotationThenNoFacet() {

        abstract class Customer implements Persistable {
        }

        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(JdoDatastoreIdentityFacet.class);
        assertNull(facet);
    }

    public void testDatastoreIdentityAnnotationWithNoExplicitStrategyDefaultsToUnspecified() {
        @DatastoreIdentity()
        abstract class Customer implements Persistable {
        }
        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        final JdoDatastoreIdentityFacet entityFacet = facetHolder
                .getFacet(JdoDatastoreIdentityFacet.class);
        assertEquals(IdGeneratorStrategy.UNSPECIFIED, entityFacet.getStrategy());
    }

    public void testEntityAnnotationWithExplicitStrategyAttributeProvided() {
        @DatastoreIdentity(strategy=IdGeneratorStrategy.IDENTITY)
        abstract class Customer implements Persistable {
        }

        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        final JdoDatastoreIdentityFacet entityFacet = facetHolder
                .getFacet(JdoDatastoreIdentityFacet.class);
        assertEquals(IdGeneratorStrategy.IDENTITY, entityFacet.getStrategy());
    }

    public void testNoMethodsRemoved() {
        @PersistenceCapable
        abstract class Customer implements Persistable {
        }

        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        assertNoMethodsRemoved();
    }
}
