package org.apache.isis.core.metamodel.facets.value.semantics;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.objectvalue.digits.MaxFractionalDigitsFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.digits.MaxTotalDigitsFacet;

public class ValueSemanticsAnnotationFacetFactoryTest
extends AbstractFacetFactoryTest {

    public void testAnnotationPickedUpOnProperty() {
        final ValueSemanticsAnnotationFacetFactory facetFactory =
                new ValueSemanticsAnnotationFacetFactory(metaModelContext);

        class Order {
            @javax.validation.constraints.Digits(integer=14, fraction=4)
            public BigDecimal getCost() {
                return null;
            }
        }
        final Method method = findMethod(Order.class, "getCost");

        facetFactory.process(ProcessMethodContext
                .forTesting(Order.class, null, method, methodRemover, facetedMethod));

        assertBigDecimalSemantics(facetedMethod, 18, 4);
    }

    public void testAnnotationPickedUpOnActionParameter() {
        final ValueSemanticsAnnotationFacetFactory facetFactory =
                new ValueSemanticsAnnotationFacetFactory(metaModelContext);

        class Order {
            @SuppressWarnings("unused")
            public void updateCost(
                    @javax.validation.constraints.Digits(integer=14, fraction=4)
                    final BigDecimal cost) {
            }
        }
        final Method method = findMethod(Order.class, "updateCost", new Class[] { BigDecimal.class });

        facetFactory.processParams(new FacetFactory
                .ProcessParameterContext(Customer.class, IntrospectionPolicy.ANNOTATION_OPTIONAL, method, null, facetedMethodParameter));

        assertBigDecimalSemantics(facetedMethodParameter, 18, 4);

    }

    // -- HELPER

    private void assertBigDecimalSemantics(
            final FacetHolder facetedMethod, final int maxTotalDigits, final int maxFractionalDigits) {
        if(maxTotalDigits>=0) {
            final MaxTotalDigitsFacet facet = facetedMethod.getFacet(MaxTotalDigitsFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof MaxTotalDigitsFacetOnPropertyFromJavaxValidationDigitsAnnotation
                    ||facet instanceof MaxTotalDigitsFacetOnParameterFromJavaxValidationDigitsAnnotation);
            assertThat(facet.maxTotalDigits(), is(maxTotalDigits));
        }

        if(maxFractionalDigits>=0) {
            final MaxFractionalDigitsFacet facet = facetedMethod.getFacet(MaxFractionalDigitsFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof MaxFractionalDigitsFacetOnPropertyFromJavaxValidationDigitsAnnotation
                    ||facet instanceof MaxFractionalDigitsFacetOnParameterFromJavaxValidationDigitsAnnotation);
            assertThat(facet.getMaxFractionalDigits(), is(maxFractionalDigits));
        }
    }

}
