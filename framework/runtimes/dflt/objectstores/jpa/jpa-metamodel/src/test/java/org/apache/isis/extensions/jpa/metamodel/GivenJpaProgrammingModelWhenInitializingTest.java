package org.apache.isis.extensions.jpa.metamodel;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.object.entity.JpaEntityAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.specloader.progmodelfacets.JpaProgrammingModelFacets;

public class GivenJpaProgrammingModelWhenInitializingTest {

    private JpaProgrammingModelFacets jpaProgrammingModelFacets;


    @Before
    public void setUp() throws Exception {
        jpaProgrammingModelFacets = new JpaProgrammingModelFacets();
    }


    @Test
    public void hasNoFacetFactoriesRegisteredIfNotInitialized()
            throws Exception {
        final List<FacetFactory> list = jpaProgrammingModelFacets.getList();
        assertThat(list.size(), is(0));
    }

    @Test
    public void onceInitializedShouldContainEntityAnnotationFacetFactory()
            throws Exception {
        jpaProgrammingModelFacets.init();
        final List<FacetFactory> list = jpaProgrammingModelFacets.getList();
        assertThat(
                list,
                IsisMatchers
                        .containsElementThat(is(instanceOf(JpaEntityAnnotationFacetFactory.class))));
    }

}
// Copyright (c) Naked Objects Group Ltd.
