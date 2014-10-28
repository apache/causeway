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
package org.apache.isis.objectstore.jdo.metamodel.facets.object.auditable;

import junit.framework.Assert;

import java.util.List;
import javax.jdo.annotations.PersistenceCapable;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.object.audit.AuditableFacet;
import org.apache.isis.core.metamodel.facets.object.audit.markerifc.AuditableFacetMarkerInterface;
import org.apache.isis.objectstore.jdo.applib.Auditable;


public class GivenAuditableFacetMarkerInterfaceFactoryTest extends
        AbstractFacetFactoryTest {

    private AuditableMarkerInterfaceInJdoApplibFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new AuditableMarkerInterfaceInJdoApplibFacetFactory();
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
                FeatureType.ACTION_PARAMETER));
    }

    public void testAuditableMarkerInterfacePickedUpOnClass() {
        class Customer implements Auditable {
        }

        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(AuditableFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof AuditableFacetMarkerInterface);
    }

    public void testIfNoAuditableMarkerInterfaceThenNoFacet() {

        class Customer {
        }

        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(AuditableFacet.class);
        assertNull(facet);
    }


    public void testNoMethodsRemoved() {
        @PersistenceCapable
        class Customer {
        }

        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, methodRemover, facetHolder));

        assertNoMethodsRemoved();
    }
}
