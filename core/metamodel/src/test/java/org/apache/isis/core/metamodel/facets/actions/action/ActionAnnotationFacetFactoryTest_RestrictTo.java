package org.apache.isis.core.metamodel.facets.actions.action;

import static org.junit.Assert.assertNull;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actions.prototype.PrototypeFacet;
import org.junit.Assert;
import org.junit.Test;

public class ActionAnnotationFacetFactoryTest_RestrictTo extends ActionAnnotationFacetFactoryTest {

    @Test
    public void whenRestrictedToPrototyping() {

        class Customer {
            @Action(restrictTo = org.apache.isis.applib.annotation.RestrictTo.PROTOTYPING)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(
        		cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processRestrictTo(processMethodContext);

        // then
        final PrototypeFacet facet = facetedMethod.getFacet(PrototypeFacet.class);
        Assert.assertNotNull(facet);
    }

    @Test
    public void whenRestrictedToNoRestriction() {

        class Customer {
            @Action(restrictTo = org.apache.isis.applib.annotation.RestrictTo.NO_RESTRICTIONS)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(
        		cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processRestrictTo(processMethodContext);

        // then
        final PrototypeFacet facet = facetedMethod.getFacet(PrototypeFacet.class);
        assertNull(facet);
    }

    @Test
    public void whenNotPresent() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(
        		cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processRestrictTo(processMethodContext);

        // then
        final PrototypeFacet facet = facetedMethod.getFacet(PrototypeFacet.class);
        assertNull(facet);
    }

}