package org.apache.isis.extensions.jpa.metamodel.facets.prop.manytoone;

import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.FetchType;

import junit.framework.Assert;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.extensions.jpa.metamodel.facets.JpaFetchTypeFacet;

public class GivenJpaManyToOneAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private JpaManyToOneAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new JpaManyToOneAnnotationFacetFactory();
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

        final Class<?> cls = SimpleObjectWithManyToOneAnnotation.class;
        final Method method = cls.getMethod("getParent");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaManyToOneFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof JpaManyToOneFacetAnnotation);
    }

    public void testMandatoryFacetDerivedForProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithManyToOneAnnotation.class;
        final Method method = cls.getMethod("getParent");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
    }

    public void testOptionalAttributeExplicitlySpecifiedAsFalse() throws Exception {

        final Class<?> cls = SimpleObjectWithManyToOneOptionalFalse.class;
        final Method method = cls.getMethod("getParent");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof MandatoryFacetDerivedFromJpaManyToOneAnnotation);
        assertFalse(facet.isInvertedSemantics());
    }

    public void testOptionalAttributeExplicitlySpecifiedAsTrue() throws Exception {

        final Class<?> cls = SimpleObjectWithManyToOneOptionalTrue.class;
        final Method method = cls.getMethod("getParent");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof OptionalFacetDerivedFromJpaManyToOneAnnotation);
        assertTrue(facet.isInvertedSemantics());
    }

    public void testOptionalAttributeNotSpecified() throws Exception {

        final Class<?> cls = SimpleObjectWithManyToOneAnnotation.class;
        final Method method = cls.getMethod("getParent");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof OptionalFacetDerivedFromJpaManyToOneAnnotation);
        assertTrue(facet.isInvertedSemantics());
    }

    public void testJpaFetchTypeFacetDerivedForProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithManyToOneAnnotation.class;
        final Method method = cls.getMethod("getParent");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final JpaFetchTypeFacet facet = facetedMethod.getFacet(JpaFetchTypeFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof JpaFetchTypeFacetDerivedFromJpaManyToOneAnnotation);
    }

    public void testFetchTypeAttributeExplicitlySpecifiedAsLazy() throws Exception {

        final Class<?> cls = SimpleObjectWithManyToOneFetchLazy.class;
        final Method method = cls.getMethod("getParent");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final JpaFetchTypeFacet facet = facetedMethod.getFacet(JpaFetchTypeFacet.class);
        assertNotNull(facet);
        assertEquals(FetchType.LAZY, facet.getFetchType());
    }

    public void testFetchTypeAttributeExplicitlySpecifiedAsEager() throws Exception {

        final Class<?> cls = SimpleObjectWithManyToOneFetchEager.class;
        final Method method = cls.getMethod("getParent");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final JpaFetchTypeFacet facet = facetedMethod.getFacet(JpaFetchTypeFacet.class);
        assertNotNull(facet);
        assertEquals(FetchType.EAGER, facet.getFetchType());
    }

    public void testFetchTypeAttributeNotSpecified() throws Exception {

        final Class<?> cls = SimpleObjectWithManyToOneAnnotation.class;
        final Method method = cls.getMethod("getParent");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final JpaFetchTypeFacet facet = facetedMethod.getFacet(JpaFetchTypeFacet.class);
        assertNotNull(facet);
        assertEquals(FetchType.EAGER, facet.getFetchType());
    }

    public void testIfNoManyToOneColumnAnnotationThenNoManyToOneColumnFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoManyToOneAnnotation.class;
        final Method method = cls.getMethod("getParent");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaManyToOneFacet.class);
        assertNull(facet);
    }

    public void testIfNoManyToOneAnnotationThenNoMandatoryFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoManyToOneAnnotation.class;
        final Method method = cls.getMethod("getParent");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNull(facet);
    }

    public void testIfNoManyToOneAnnotationThenNoJpaFetchTypeFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoManyToOneAnnotation.class;
        final Method method = cls.getMethod("getParent");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaFetchTypeFacet.class);
        assertNull(facet);
    }

    public void testNoMethodsRemoved() throws Exception {

        final Class<?> cls = SimpleObjectWithManyToOneAnnotation.class;
        final Method method = cls.getMethod("getParent");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        assertNoMethodsRemoved();
    }

}

// Copyright (c) Naked Objects Group Ltd.
