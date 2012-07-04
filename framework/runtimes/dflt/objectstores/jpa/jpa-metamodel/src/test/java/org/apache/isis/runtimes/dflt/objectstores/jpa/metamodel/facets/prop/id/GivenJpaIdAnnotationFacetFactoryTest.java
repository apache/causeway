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
package org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.id;

import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.Id;

import junit.framework.Assert;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.members.disable.DisabledFacet;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.id.DisabledFacetDerivedFromJpaIdAnnotation;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.id.JpaIdAnnotationFacetFactory;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.id.JpaIdFacet;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.id.OptionalFacetDerivedFromJpaIdAnnotation;

public class GivenJpaIdAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private JpaIdAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new JpaIdAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testFeatureTypes() {
        final List<FeatureType> featureTypes = facetFactory.getFeatureTypes();
        Assert.assertFalse(contains(featureTypes, FeatureType.OBJECT));
        assertTrue(contains(featureTypes, FeatureType.PROPERTY));
        assertFalse(contains(featureTypes, FeatureType.COLLECTION));
        Assert.assertFalse(contains(featureTypes, FeatureType.ACTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION_PARAMETER));
    }

    public void testIdAnnotationPickedUpOnProperty() throws Exception {
        final Class<?> cls = SimpleObjectWithId.class;
        final Method method = cls.getMethod("getId");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaIdFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof JpaIdFacet);
    }

    public void testOptionalDerivedFromId() throws Exception {
        final Class<?> cls = SimpleObjectWithId.class;
        final Method method = cls.getMethod("getId");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof OptionalFacetDerivedFromJpaIdAnnotation);
    }

    public void testDisabledDerivedFromId() throws Exception {
        final Class<?> cls = SimpleObjectWithId.class;
        final Method method = cls.getMethod("getId");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof DisabledFacetDerivedFromJpaIdAnnotation);
    }

    public void testIfNoIdAnnotationThenNoFacet() throws Exception {

        class Customer {
            private Long id;

            // @Id missing
            @SuppressWarnings("unused")
            public Long getId() {
                return id;
            }

            @SuppressWarnings("unused")
            public void setId(final Long id) {
                this.id = id;
            }
        }

        final Class<?> cls = Customer.class;
        final Method method = cls.getMethod("getId");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaIdFacet.class);
        assertNull(facet);
    }

    public void testNoMethodsRemoved() throws Exception {
        class Customer {
            private Long id;

            @SuppressWarnings("unused")
            @Id
            public Long getId() {
                return id;
            }

            @SuppressWarnings("unused")
            public void setId(final Long id) {
                this.id = id;
            }
        }

        final Class<?> cls = Customer.class;
        final Method method = cls.getMethod("getId");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        assertNoMethodsRemoved();
    }
}
