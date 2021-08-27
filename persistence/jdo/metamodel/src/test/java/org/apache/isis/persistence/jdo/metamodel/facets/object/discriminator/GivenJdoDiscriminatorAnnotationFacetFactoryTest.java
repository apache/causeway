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
package org.apache.isis.persistence.jdo.metamodel.facets.object.discriminator;

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.PersistenceCapable;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.ObjectTypeFacetFactory;
import org.apache.isis.core.metamodel.facets.object.logicaltype.LogicalTypeFacet;
import org.apache.isis.persistence.jdo.metamodel.testing.AbstractFacetFactoryTest;
import org.apache.isis.persistence.jdo.provider.metamodel.facets.object.discriminator.JdoDiscriminatorFacet;

import lombok.val;

public class GivenJdoDiscriminatorAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private JdoDiscriminatorAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new JdoDiscriminatorAnnotationFacetFactory(metaModelContext, jdoFacetContext, null);
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

    public void testDiscriminatorValueAnnotationPickedUpOnClass() {
        @Discriminator("CUS")
        abstract class Customer {
        }

        facetFactory.process(ProcessClassContext
                .forTesting(Customer.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(JdoDiscriminatorFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof JdoDiscriminatorFacetDefault);
    }

    public void testObjectTypeAnnotationPickedUpOnClass() {
        @Discriminator("CUS")
        abstract class Customer {
        }

        facetFactory.process(new ObjectTypeFacetFactory.ProcessObjectTypeContext(Customer.class, facetHolder));

        final Facet facet = facetHolder.getFacet(LogicalTypeFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof LogicalTypeFacetInferredFromJdoDiscriminatorValueAnnotation);
    }

    public void testIfNoEntityAnnotationThenNoFacet() {

        abstract class Customer {
        }

        facetFactory.process(ProcessClassContext
                .forTesting(Customer.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(LogicalTypeFacet.class);
        assertNull(facet);
    }

    public void testAnnotationValue() {
        @Discriminator("CUS")
        abstract class Customer {
        }

        facetFactory.process(new ObjectTypeFacetFactory.ProcessObjectTypeContext(Customer.class, facetHolder));

        final LogicalTypeFacet discriminatorValueFacet = facetHolder.getFacet(LogicalTypeFacet.class);
        assertEquals("CUS", discriminatorValueFacet.value());
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
