package org.apache.isis.extensions.jpa.metamodel.facets.object.embeddable;

import java.util.List;

import junit.framework.Assert;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.object.aggregated.AggregatedFacet;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryTest;


public class GivenJpaEmbeddableAnnotationFacetFactoryTest extends
        AbstractFacetFactoryTest {

    private JpaEmbeddableAnnotationFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new JpaEmbeddableAnnotationFacetFactory();
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
                .assertTrue(contains(featureTypes,
                FeatureType.OBJECT));
        assertFalse(contains(featureTypes,
                FeatureType.PROPERTY));
        assertFalse(contains(featureTypes,
                FeatureType.COLLECTION));
        Assert
                .assertFalse(contains(featureTypes,
                FeatureType.ACTION));
        assertFalse(contains(featureTypes,
                FeatureType.ACTION_PARAMETER));
    }


    public void testAggregatedFacetPickedUpOnType() throws Exception {
        facetFactory.process(new FacetFactory.ProcessClassContext(SimpleObjectWithEmbeddable.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(AggregatedFacet.class);
        assertNotNull(facet);
        Assert
                .assertTrue(facet instanceof AggregatedFacetDerivedFromJpaEmbeddableAnnotation);
    }

    public void testJpaEmbeddableFacetPickedUpOnType() throws Exception {
        facetFactory.process(new FacetFactory.ProcessClassContext(SimpleObjectWithEmbeddable.class, methodRemover, facetHolder));

        final Facet facet = facetHolder.getFacet(JpaEmbeddableFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof JpaEmbeddableFacetAnnotation);
    }


    public void testNoMethodsRemovedForType() throws Exception {
        facetFactory.process(new FacetFactory.ProcessClassContext(SimpleObjectWithEmbeddable.class, methodRemover, facetHolder));
        assertNoMethodsRemoved();
    }


}

// Copyright (c) Naked Objects Group Ltd.
