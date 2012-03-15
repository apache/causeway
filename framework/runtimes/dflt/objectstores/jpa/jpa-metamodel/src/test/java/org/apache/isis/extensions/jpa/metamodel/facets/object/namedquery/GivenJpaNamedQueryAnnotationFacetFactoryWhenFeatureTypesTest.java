package org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryJUnit4TestCase;


public class GivenJpaNamedQueryAnnotationFacetFactoryWhenFeatureTypesTest
        extends AbstractFacetFactoryJUnit4TestCase {

    private JpaNamedQueryAnnotationFacetFactory facetFactory;

    @Before
    public void setUp() throws Exception {
        facetFactory = new JpaNamedQueryAnnotationFacetFactory();
    }

    @After
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    @Test
    public void testFeatureTypes() {
        final List<FeatureType> featureTypes = facetFactory
                .getFeatureTypes();
        assertTrue(contains(featureTypes,
                FeatureType.OBJECT));
        assertFalse(contains(featureTypes,
                FeatureType.PROPERTY));
        assertFalse(contains(featureTypes,
                FeatureType.COLLECTION));
        assertFalse(contains(featureTypes,
                FeatureType.ACTION));
        assertFalse(contains(featureTypes,
                FeatureType.ACTION_PARAMETER));
    }

}

// Copyright (c) Naked Objects Group Ltd.
