package org.apache.isis.extensions.jpa.metamodel;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.apache.isis.core.commons.matchers.IsisMatchers;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.object.entity.JpaEntityAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.facets.prop.id.JpaIdAnnotationFacetFactory;
import org.apache.isis.extensions.jpa.metamodel.specloader.progmodelfacets.JpaProgrammingModelFacets;

@RunWith(Parameterized.class)
public class GivenJpaProgrammingModelWhenInitializedThenFacetsContentsTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { JpaEntityAnnotationFacetFactory.class },
                { JpaIdAnnotationFacetFactory.class },
                });
    }


    private final Class<?> facetFactoryClass;

    public GivenJpaProgrammingModelWhenInitializedThenFacetsContentsTest(
            final Class<?> facetFactoryClass) {
        this.facetFactoryClass = facetFactoryClass;
    }


    @Test
    public void shouldContainSpecifiedFacetFactory() throws Exception {
        final JpaProgrammingModelFacets jpaProgrammingModelFacets = new JpaProgrammingModelFacets();
        jpaProgrammingModelFacets.init();
        final List<FacetFactory> list = jpaProgrammingModelFacets.getList();
        assertThat(list, IsisMatchers
                .containsElementThat(is(instanceOf(facetFactoryClass))));
    }


}
// Copyright (c) Naked Objects Group Ltd.
