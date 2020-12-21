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
package org.apache.isis.legacy.jdo.datanucleus.metamodel.facets.prop.primarykey;

import java.lang.reflect.Method;

import javax.jdo.annotations.PrimaryKey;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.legacy.jdo.datanucleus.metamodel.facets.prop.primarykey.DisabledFacetDerivedFromJdoPrimaryKeyAnnotation;
import org.apache.isis.legacy.jdo.datanucleus.metamodel.facets.prop.primarykey.JdoPrimaryKeyAnnotationFacetFactory;
import org.apache.isis.legacy.jdo.datanucleus.metamodel.facets.prop.primarykey.JdoPrimaryKeyFacet;
import org.apache.isis.legacy.jdo.datanucleus.metamodel.facets.prop.primarykey.OptionalFacetDerivedFromJdoPrimaryKeyAnnotation;
import org.apache.isis.legacy.jdo.datanucleus.testing.AbstractFacetFactoryTest;

import lombok.val;

public class GivenJdoPrimaryKeyAnnotationFacetFactoryTest 
extends AbstractFacetFactoryTest {

    private JdoPrimaryKeyAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new JdoPrimaryKeyAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testFeatureTypes() {
        val featureTypes = facetFactory.getFeatureTypes();
        assertFalse(contains(featureTypes, FeatureType.OBJECT));
        assertTrue(contains(featureTypes, FeatureType.PROPERTY));
        assertFalse(contains(featureTypes, FeatureType.COLLECTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION));
        assertFalse(contains(featureTypes, FeatureType.ACTION_PARAMETER_SCALAR));
    }

    public void testIdAnnotationPickedUpOnProperty() throws Exception {
        final Class<?> cls = SimpleObjectWithPrimaryKey.class;
        final Method method = cls.getMethod("getId");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JdoPrimaryKeyFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof JdoPrimaryKeyFacet);
    }

    public void testOptionalDerivedFromId() throws Exception {
        final Class<?> cls = SimpleObjectWithPrimaryKey.class;
        final Method method = cls.getMethod("getId");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof OptionalFacetDerivedFromJdoPrimaryKeyAnnotation);
    }

    public void testDisabledDerivedFromId() throws Exception {
        final Class<?> cls = SimpleObjectWithPrimaryKey.class;
        final Method method = cls.getMethod("getId");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(DisabledFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof DisabledFacetDerivedFromJdoPrimaryKeyAnnotation);
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
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JdoPrimaryKeyFacet.class);
        assertNull(facet);
    }

    public void testNoMethodsRemoved() throws Exception {
        class Customer {
            private Long id;

            @SuppressWarnings("unused")
            @PrimaryKey
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
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, null, method, methodRemover, facetedMethod));

        assertNoMethodsRemoved();
    }
}
