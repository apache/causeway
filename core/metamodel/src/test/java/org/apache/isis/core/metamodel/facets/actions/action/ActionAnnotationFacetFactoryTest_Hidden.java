package org.apache.isis.core.metamodel.facets.actions.action;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actions.action.hidden.HiddenFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.junit.Assert;
import org.junit.Test;

public class ActionAnnotationFacetFactoryTest_Hidden extends ActionAnnotationFacetFactoryTest {

    @Test
    public void withAnnotation() {

        class Customer {
            @Action(hidden = Where.REFERENCES_PARENT)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(
        		cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processHidden(processMethodContext);

        // then
        final HiddenFacet hiddenFacet = facetedMethod.getFacet(HiddenFacet.class);
        Assert.assertNotNull(hiddenFacet);
        assertThat(hiddenFacet.where(), is(Where.REFERENCES_PARENT));

        final Facet hiddenFacetImpl = facetedMethod.getFacet(HiddenFacetForActionAnnotation.class);
        Assert.assertNotNull(hiddenFacetImpl);
        Assert.assertTrue(hiddenFacet == hiddenFacetImpl);
    }

}