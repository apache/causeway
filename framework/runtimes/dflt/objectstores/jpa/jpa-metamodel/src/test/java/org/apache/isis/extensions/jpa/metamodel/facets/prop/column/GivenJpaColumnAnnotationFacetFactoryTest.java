package org.apache.isis.extensions.jpa.metamodel.facets.prop.column;

import java.lang.reflect.Method;
import java.util.List;

import junit.framework.Assert;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.maxlen.MaxLengthFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;

public class GivenJpaColumnAnnotationFacetFactoryTest extends AbstractFacetFactoryTest {

    private JpaColumnAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new JpaColumnAnnotationFacetFactory();
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

        final Class<SimpleObjectWithColumnAnnotation> cls = SimpleObjectWithColumnAnnotation.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaColumnFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof JpaColumnFacetAnnotation);
    }

    public void testColumnAnnotationNameAttributeSet() throws Exception {

        final Class<?> cls = SimpleObjectWithColumnName.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final JpaColumnFacet facet = facetedMethod.getFacet(JpaColumnFacet.class);
        assertEquals("someCol", facet.name());
    }

    public void testMaxLengthFacetDerivedForProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithColumnAnnotation.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MaxLengthFacet facet = facetedMethod.getFacet(MaxLengthFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof MaxLengthFacetDerivedFromJpaColumnAnnotation);
    }

    public void testMaxLengthAttributeExplicitlySpecified() throws Exception {

        final Class<?> cls = SimpleObjectWithColumnMaxLength30.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MaxLengthFacet facet = facetedMethod.getFacet(MaxLengthFacet.class);
        assertEquals(30, facet.value());
    }

    public void testMaxLengthAttributeNotExplicitlySpecified() throws Exception {

        final Class<?> cls = SimpleObjectWithColumnAnnotation.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MaxLengthFacet facet = facetedMethod.getFacet(MaxLengthFacet.class);
        assertEquals(255, facet.value());
    }

    public void testMandatoryFacetDerivedForProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithColumnAnnotation.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
    }

    public void testNullableAttributeExplicitlySpecifiedAsFalse() throws Exception {

        final Class<?> cls = SimpleObjectWithColumnNullableFalse.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof MandatoryFacetDerivedFromJpaColumnAnnotation);
        assertFalse(facet.isInvertedSemantics());
    }

    public void testNullableAttributeExplicitlySpecifiedAsTrue() throws Exception {

        final Class<?> cls = SimpleObjectWithColumnNullableTrue.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof OptionalFacetDerivedFromJpaColumnAnnotation);
        assertTrue(facet.isInvertedSemantics());
    }

    public void testNullableAttributeNotSpecifiedForColumnAnnotationProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithColumnAnnotation.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert.assertTrue(facet instanceof OptionalFacetDerivedFromJpaColumnAnnotation);
    }

    public void testIfNoColumnAnnotationThenNoColumnFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoColumnAnnotation.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaColumnFacet.class);
        assertNull(facet);
    }

    public void testIfNoColumnAnnotationThenNoMaxLengthFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoColumnAnnotation.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(MaxLengthFacet.class);
        assertNull(facet);
    }

    public void testIfNoColumnAnnotationThenNoMandatoryFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoColumnAnnotation.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNull(facet);
    }

    public void testNoMethodsRemoved() throws Exception {

        final Class<?> cls = SimpleObjectWithColumnAnnotation.class;
        final Method method = cls.getMethod("getSomeColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        assertNoMethodsRemoved();
    }

}

// Copyright (c) Naked Objects Group Ltd.
