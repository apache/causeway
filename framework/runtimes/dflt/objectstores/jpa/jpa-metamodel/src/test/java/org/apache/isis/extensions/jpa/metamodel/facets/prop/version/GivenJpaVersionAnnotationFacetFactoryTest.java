package org.apache.isis.extensions.jpa.metamodel.facets.prop.version;

import java.lang.reflect.Method;
import java.util.List;

import junit.framework.Assert;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.progmodel.facets.members.disable.DisabledFacet;


public class GivenJpaVersionAnnotationFacetFactoryTest extends
        AbstractFacetFactoryTest {

    private JpaVersionAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new JpaVersionAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testFeatureTypes() {
        final List<FeatureType> featureTypes = facetFactory
                .getFeatureTypes();
        Assert
                .assertFalse(contains(featureTypes,
                FeatureType.OBJECT));
        assertTrue(contains(featureTypes,
                FeatureType.PROPERTY));
        assertFalse(contains(featureTypes,
                FeatureType.COLLECTION));
        Assert
                .assertFalse(contains(featureTypes,
                FeatureType.ACTION));
        assertFalse(contains(featureTypes,
                FeatureType.ACTION_PARAMETER));
    }

    public void testTransientAnnotationPickedUpOnProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithVersionAnnotation.class;
        final Method method = cls.getMethod("getVersionColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(JpaVersionFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof JpaVersionFacetAnnotation);
    }

    public void testDisabledFacetDerivedForProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithVersionAnnotation.class;
        final Method method = cls.getMethod("getVersionColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final DisabledFacet facet = facetedMethod.getFacet(DisabledFacet.class);
        assertNotNull(facet);
        Assert
                .assertTrue(facet instanceof DisabledFacetDerivedFromJpaVersionAnnotation);
    }

    public void testOptionalFacetDerivedForProperty() throws Exception {

        final Class<?> cls = SimpleObjectWithVersionAnnotation.class;
        final Method method = cls.getMethod("getVersionColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        final MandatoryFacet facet = facetedMethod.getFacet(MandatoryFacet.class);
        assertNotNull(facet);
        Assert
                .assertTrue(facet instanceof OptionalFacetDerivedFromJpaVersionAnnotation);
    }


    public void testIfNoVersionAnnotationThenNoVersionFacet() throws Exception {

        final Class<?> cls = SimpleObjectWithNoVersionAnnotation.class;
        final Method method = cls.getMethod("getVersionColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(JpaVersionFacet.class));
    }

    public void testIfNoTransientAnnotationThenNoDisabledFacet()
            throws Exception {

        final Class<?> cls = SimpleObjectWithNoVersionAnnotation.class;
        final Method method = cls.getMethod("getVersionColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        assertNull(facetedMethod.getFacet(DisabledFacet.class));
    }


    public void testNoMethodsRemoved() throws Exception {

        final Class<?> cls = SimpleObjectWithVersionAnnotation.class;
        final Method method = cls.getMethod("getVersionColumn");
        facetFactory.process(new FacetFactory.ProcessMethodContext(cls, method, methodRemover, facetedMethod));

        assertNoMethodsRemoved();
    }


}

// Copyright (c) Naked Objects Group Ltd.
