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
package org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.joincolumn;

import java.lang.reflect.Method;
import java.util.List;

import junit.framework.Assert;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.joincolumn.JpaJoinColumnAnnotationFacetFactory;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.joincolumn.JpaJoinColumnFacet;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.joincolumn.JpaJoinColumnFacetAnnotation;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.joincolumn.MandatoryFacetDerivedFromJpaJoinColumnAnnotation;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.joincolumn.OptionalFacetDerivedFromJpaJoinColumnAnnotation;

public class GivenJpaJoinColumnAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private JpaJoinColumnAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new JpaJoinColumnAnnotationFacetFactory();
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

    public void testColumnAnnotationPickedUpOnProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithJoinColumnAnnotation.class;
        final Method method = cls.getMethod("getJoinColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaJoinColumnFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof JpaJoinColumnFacetAnnotation);
    }

    public void testColumnAnnotationNameAttributeSet() throws Exception {

        final Class<?> cls = SimpleObjectWithJoinColumnName.class;
        final Method method = cls.getMethod("getJoinColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final JpaJoinColumnFacet facet = facetedMethod.getFacet(JpaJoinColumnFacet.class);
        assertEquals("joinCol", facet.name());
    }

    public void testMandatoryFacetDerivedForProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithJoinColumnAnnotation.class;
        final Method method = cls.getMethod("getJoinColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
    }

    public void testNullableAttributeExplicitlySpecifiedAsFalse() throws Exception {

        final Class<?> cls = SimpleObjectWithJoinColumnNullableFalse.class;
        final Method method = cls.getMethod("getJoinColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof MandatoryFacetDerivedFromJpaJoinColumnAnnotation);
        assertFalse(facet.isInvertedSemantics());
    }

    public void testNullableAttributeExplicitlySpecifiedAsTrue() throws Exception {

        final Class<?> cls = SimpleObjectWithJoinColumnNullableTrue.class;
        final Method method = cls.getMethod("getJoinColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof OptionalFacetDerivedFromJpaJoinColumnAnnotation);
        assertTrue(facet.isInvertedSemantics());
    }

    public void testNullableAttributeNotSpecified() throws Exception {

        final Class<?> cls = SimpleObjectWithJoinColumnAnnotation.class;
        final Method method = cls.getMethod("getJoinColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof OptionalFacetDerivedFromJpaJoinColumnAnnotation);
    }

    public void testIfNoJoinColumnAnnotationThenNoJoinColumnFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoJoinColumnAnnotation.class;
        final Method method = cls.getMethod("getJoinColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaJoinColumnFacet.class);
        assertNull(facet);
    }

    public void testIfNoJoinColumnAnnotationThenNoMandatoryFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoJoinColumnAnnotation.class;
        final Method method = cls.getMethod("getJoinColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNull(facet);
    }

    public void testNoMethodsRemoved() throws Exception {

        final Class<?> cls = SimpleObjectWithJoinColumnAnnotation.class;
        final Method method = cls.getMethod("getJoinColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        assertNoMethodsRemoved();
    }
}
