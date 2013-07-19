package org.apache.isis.core.progmodel.facets.object.cssclass;

import org.apache.isis.applib.annotation.CssClass;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacetAbstract;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class CssClassAnnotationForTypeFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    @Test
    public void testCssClassAnnotationPickedUpOnClass() {

        final CssClassAnnotationForTypeFacetFactory facetFactory = new CssClassAnnotationForTypeFacetFactory();
        facetFactory.setSpecificationLookup(mockSpecificationLoaderSpi);

        @CssClass("testClass")
        class Customer {
        }

        expectNoMethodsRemoved();

        facetFactory.process(new FacetFactory.ProcessClassContext(Customer.class, null, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CssClassFacet.class);
        assertThat(facet, is(not(nullValue())));
        assertThat(facet instanceof CssClassFacetAbstract, is(true));
        final CssClassFacetAbstract cssClassFacetAbstract = (CssClassFacetAbstract) facet;
        assertThat(cssClassFacetAbstract.value(), equalTo("testClass"));
    }
}
