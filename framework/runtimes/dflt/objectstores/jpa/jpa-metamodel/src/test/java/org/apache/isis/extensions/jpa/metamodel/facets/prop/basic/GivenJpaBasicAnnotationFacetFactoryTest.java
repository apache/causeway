package org.apache.isis.extensions.jpa.metamodel.facets.prop.basic;

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

public class GivenJpaBasicAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private JpaBasicAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new JpaBasicAnnotationFacetFactory();
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

    public void testBasicAnnotationPickedUpOnProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithBasicOptionalFalse.class;
        final Method method = cls.getMethod("getSomeColumn");
        
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaBasicFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof JpaBasicFacetAnnotation);
    }

    public void testFetchTypeAnnotationDerivedForProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithBasicFetchTypeEager.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final JpaFetchTypeFacet facet = facetedMethod.getFacet(JpaFetchTypeFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof JpaFetchTypeFacetDerivedFromJpaBasicAnnotation);
    }

    public void testFetchTypeAttributeExplicitlySpecified() throws Exception {

        final Class<?> cls = SimpleObjectWithBasicFetchTypeLazy.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final JpaFetchTypeFacet facet = facetedMethod.getFacet(JpaFetchTypeFacet.class);
        assertEquals(FetchType.LAZY, facet.getFetchType());
    }

    public void testFetchTypeAttributeNotExplicitlySpecified() throws Exception {

        final Class<?> cls = SimpleObjectWithBasicAnnotation.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final JpaFetchTypeFacet facet = facetedMethod.getFacet(JpaFetchTypeFacet.class);
        assertEquals(FetchType.EAGER, facet.getFetchType());
    }

    public void testMandatoryFacetDerivedForProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithBasicAnnotation.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
    }

    public void testOptionalAttributeExplicitlySpecifiedAsFalseForBasicAnnotationProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithBasicOptionalFalse.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof MandatoryFacetDerivedFromJpaBasicAnnotation);
        assertFalse(facet.isInvertedSemantics());
    }

    public void testOptionalAttributeExplicitlySpecifiedAsTrueForBasicAnnotationProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithBasicOptionalTrue.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof OptionalFacetDerivedFromJpaBasicAnnotation);
        assertTrue(facet.isInvertedSemantics());
    }

    public void testOptionalAttributeNotSpecifiedForBasicAnnotationProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithBasicAnnotation.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof OptionalFacetDerivedFromJpaBasicAnnotation);
    }

    public void testIfNoBasicAnnotationThenNoBasicFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoBasicAnnotation.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaBasicFacet.class);
        assertNull(facet);
    }

    public void testIfNoBasicAnnotationThenNoFetchTypeFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoBasicAnnotation.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaFetchTypeFacet.class);
        assertNull(facet);
    }

    public void testIfNoBasicAnnotationThenNoMandatoryFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoBasicAnnotation.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNull(facet);
    }

    public void testNoMethodsRemoved() throws Exception {

        final Class<?> cls = SimpleObjectWithBasicAnnotation.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        assertNoMethodsRemoved();
    }

}

// Copyright (c) Naked Objects Group Ltd.
