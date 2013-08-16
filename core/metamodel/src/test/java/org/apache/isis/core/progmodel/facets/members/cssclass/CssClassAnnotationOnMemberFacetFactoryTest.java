package org.apache.isis.core.progmodel.facets.members.cssclass;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.apache.isis.applib.annotation.CssClass;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacetAbstract;
import org.apache.isis.core.progmodel.facets.AbstractFacetFactoryJUnit4TestCase;

public class CssClassAnnotationOnMemberFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    @Test
    public void testCssClassAnnotationPickedUpOnClass() {

        final CssClassAnnotationOnMemberFacetFactory facetFactory = new CssClassAnnotationOnMemberFacetFactory();
        facetFactory.setSpecificationLookup(mockSpecificationLoaderSpi);

        class Customer {

            @CssClass("user")
            public String getName() {
                return "Joe";
            }
        }

        expectNoMethodsRemoved();

        facetedMethod = FacetedMethod.createForProperty(Customer.class, "name");
        facetFactory.process(new FacetFactory.ProcessMethodContext(Customer.class, null, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(CssClassFacet.class);
        assertThat(facet, is(not(nullValue())));
        assertThat(facet instanceof CssClassFacetAbstract, is(true));
        final CssClassFacetAbstract cssClassFacetAbstract = (CssClassFacetAbstract) facet;
        assertThat(cssClassFacetAbstract.value(), equalTo("user"));
    }
}
