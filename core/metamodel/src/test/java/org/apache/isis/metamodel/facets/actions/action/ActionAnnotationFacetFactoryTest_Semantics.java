package org.apache.isis.metamodel.facets.actions.action;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.junit.Assert;
import org.junit.Test;

public class ActionAnnotationFacetFactoryTest_Semantics extends ActionAnnotationFacetFactoryTest {

    @Test
    public void whenSafe() {

        class Customer {
            @Action(semantics = SemanticsOf.SAFE)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processSemantics(processMethodContext);

        // then
        final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
        Assert.assertNotNull(facet);
        assertThat(facet.value(), is(SemanticsOf.SAFE));
    }

    @Test
    public void whenNotSpecified() {

        class Customer {
            @Action()
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processSemantics(processMethodContext);

        // then
        final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
        Assert.assertNotNull(facet);
        assertThat(facet.value(), is(SemanticsOf.NON_IDEMPOTENT));
    }

    @Test
    public void whenNoAnnotation() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processSemantics(processMethodContext);

        // then
        final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
        Assert.assertNotNull(facet);
        assertThat(facet.value(), is(SemanticsOf.NON_IDEMPOTENT));
    }

}