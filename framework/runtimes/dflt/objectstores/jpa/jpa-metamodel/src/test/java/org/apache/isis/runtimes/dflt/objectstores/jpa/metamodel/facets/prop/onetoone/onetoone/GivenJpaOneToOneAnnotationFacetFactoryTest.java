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
package org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.onetoone.onetoone;

import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.FetchType;

import junit.framework.Assert;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.JpaFetchTypeFacet;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.onetoone.JpaFetchTypeFacetDerivedFromJpaOneToOneAnnotation;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.onetoone.JpaOneToOneAnnotationFacetFactory;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.onetoone.JpaOneToOneFacet;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.onetoone.JpaOneToOneFacetAnnotation;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.onetoone.MandatoryFacetDerivedFromJpaOneToOneAnnotation;
import org.apache.isis.runtimes.dflt.objectstores.jpa.metamodel.facets.prop.onetoone.OptionalFacetDerivedFromJpaOneToOneAnnotation;

public class GivenJpaOneToOneAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private JpaOneToOneAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new JpaOneToOneAnnotationFacetFactory();
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

        final Class<?> cls = SimpleObjectWithOneToOneAnnotation.class;
        final Method method = cls.getMethod("getOther");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaOneToOneFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof JpaOneToOneFacetAnnotation);
    }

    public void testMandatoryFacetDerivedForProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithOneToOneAnnotation.class;
        final Method method = cls.getMethod("getOther");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
    }

    public void testOptionalAttributeExplicitlySpecifiedAsFalse() throws Exception {

        final Class<?> cls = SimpleObjectWithOneToOneOptionalFalse.class;
        final Method method = cls.getMethod("getOther");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof MandatoryFacetDerivedFromJpaOneToOneAnnotation);
        assertFalse(facet.isInvertedSemantics());
    }

    public void testOptionalAttributeExplicitlySpecifiedAsTrue() throws Exception {

        final Class<?> cls = SimpleObjectWithOneToOneOptionalTrue.class;
        final Method method = cls.getMethod("getOther");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof OptionalFacetDerivedFromJpaOneToOneAnnotation);
        assertTrue(facet.isInvertedSemantics());
    }

    public void testOptionalAttributeNotSpecified() throws Exception {

        final Class<?> cls = SimpleObjectWithOneToOneAnnotation.class;
        final Method method = cls.getMethod("getOther");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof OptionalFacetDerivedFromJpaOneToOneAnnotation);
        assertTrue(facet.isInvertedSemantics());
    }

    public void testJpaFetchTypeFacetDerivedForProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithOneToOneAnnotation.class;
        final Method method = cls.getMethod("getOther");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final JpaFetchTypeFacet facet = facetedMethod.getFacet(JpaFetchTypeFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof JpaFetchTypeFacetDerivedFromJpaOneToOneAnnotation);
    }

    public void testFetchTypeAttributeExplicitlySpecifiedAsLazy() throws Exception {

        final Class<?> cls = SimpleObjectWithOneToOneFetchLazy.class;
        final Method method = cls.getMethod("getOther");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final JpaFetchTypeFacet facet = facetedMethod.getFacet(JpaFetchTypeFacet.class);
        assertNotNull(facet);
        assertEquals(FetchType.LAZY, facet.getFetchType());
    }

    public void testFetchTypeAttributeExplicitlySpecifiedAsEager() throws Exception {

        final Class<?> cls = SimpleObjectWithOneToOneFetchEager.class;
        final Method method = cls.getMethod("getOther");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final JpaFetchTypeFacet facet = facetedMethod.getFacet(JpaFetchTypeFacet.class);
        assertNotNull(facet);
        assertEquals(FetchType.EAGER, facet.getFetchType());
    }

    public void testFetchTypeAttributeNotSpecified() throws Exception {

        final Class<?> cls = SimpleObjectWithOneToOneAnnotation.class;
        final Method method = cls.getMethod("getOther");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final JpaFetchTypeFacet facet = facetedMethod.getFacet(JpaFetchTypeFacet.class);
        assertNotNull(facet);
        assertEquals(FetchType.EAGER, facet.getFetchType());
    }

    public void testIfNoOneToOneColumnAnnotationThenNoOneToOneColumnFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoOneToOneAnnotation.class;
        final Method method = cls.getMethod("getOther");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaOneToOneFacet.class);
        assertNull(facet);
    }

    public void testIfNoOneToOneAnnotationThenNoMandatoryFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoOneToOneAnnotation.class;
        final Method method = cls.getMethod("getOther");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNull(facet);
    }

    public void testIfNoOneToOneAnnotationThenNoJpaFetchTypeFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoOneToOneAnnotation.class;
        final Method method = cls.getMethod("getOther");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaFetchTypeFacet.class);
        assertNull(facet);
    }

    public void testNoMethodsRemoved() throws Exception {

        final Class<?> cls = SimpleObjectWithOneToOneAnnotation.class;
        final Method method = cls.getMethod("getOther");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        assertNoMethodsRemoved();
    }
}
