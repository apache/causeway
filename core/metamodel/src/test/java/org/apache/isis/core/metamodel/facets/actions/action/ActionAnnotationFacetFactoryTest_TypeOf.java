package org.apache.isis.core.metamodel.facets.actions.action;

import static org.apache.isis.core.commons.matchers.IsisMatchers.classEqualTo;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromArray;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromGenerics;
import org.apache.isis.core.metamodel.facets.actions.action.typeof.TypeOfFacetForActionAnnotation;
import org.junit.Assert;
import org.junit.Test;

public class ActionAnnotationFacetFactoryTest_TypeOf extends ActionAnnotationFacetFactoryTest {


    @Test
    public void whenDeprecatedTypeOfAnnotationOnActionNotReturningCollection() {

        class Customer {
            @SuppressWarnings("unused")
            public Customer someAction() {
                return null;
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processTypeOf(processMethodContext);

        // then
        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        Assert.assertNull(facet);
    }

    @Test
    public void whenActionAnnotationOnActionReturningCollection() {

        class Order {
        }
        class Customer {
            @SuppressWarnings("rawtypes")
            @Action(typeOf = Order.class)
            public Collection someAction() {
                return null;
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processTypeOf(processMethodContext);

        // then
        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        Assert.assertNotNull(facet);
        Assert.assertTrue(facet instanceof TypeOfFacetForActionAnnotation);
        assertThat(facet.value(), classEqualTo(Order.class));
    }

    @Test
    public void whenActionAnnotationOnActionNotReturningCollection() {

        class Order {
        }
        class Customer {
            @Action(typeOf = Order.class)
            public Customer someAction() {
                return null;
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processTypeOf(processMethodContext);

        // then
        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        Assert.assertNull(facet);
    }

    @Test
    public void whenInferFromType() {

        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Order[] someAction() {
                return null;
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processTypeOf(processMethodContext);

        // then
        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        Assert.assertNotNull(facet);
        Assert.assertTrue(facet instanceof TypeOfFacetInferredFromArray);
        assertThat(facet.value(), classEqualTo(Order.class));
    }

    @Test
    public void whenInferFromGenerics() {

        class Order {
        }
        class Customer {
            @SuppressWarnings("unused")
            public Collection<Order> someAction() {
                return null;
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processTypeOf(processMethodContext);

        // then
        final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
        Assert.assertNotNull(facet);
        Assert.assertTrue(facet instanceof TypeOfFacetInferredFromGenerics);
        assertThat(facet.value(), classEqualTo(Order.class));
    }

}