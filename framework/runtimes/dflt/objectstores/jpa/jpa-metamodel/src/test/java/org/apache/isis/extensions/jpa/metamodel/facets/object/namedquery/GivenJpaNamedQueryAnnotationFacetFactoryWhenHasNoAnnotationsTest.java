package org.apache.isis.extensions.jpa.metamodel.facets.object.namedquery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryJUnit4TestCase;


public class GivenJpaNamedQueryAnnotationFacetFactoryWhenHasNoAnnotationsTest
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
    public void testIfNoNamedQueryOrNamedQueriesAnnotationThenNoFacet() {
        facetFactory.process(new FacetFactory.ProcessClassContext(SimpleObjectWithNoNamedQueries.class, methodRemover, facetHolder));
    }


}

// Copyright (c) Naked Objects Group Ltd.
