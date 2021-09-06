package org.apache.isis.core.metamodel.facets.object.callback;

import org.junit.jupiter.api.Assertions;

import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackFacetFactory;
import org.apache.isis.core.metamodel.methods.MethodLiteralConstants;

import lombok.val;

abstract class CallbackFacetFactoryTestAbstract
extends AbstractFacetFactoryTest {

    protected CallbackFacetFactory facetFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new CallbackFacetFactory(metaModelContext);
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    protected void assertPicksUp(
            final int expectedCallbackCount,
            final FacetFactory facetFactory,
            final Class<?> type,
            final MethodLiteralConstants.CallbackMethod callbackMethod,
            final Class<? extends ImperativeFacet> facetType) {

        // when
        facetFactory.process(ProcessClassContext
                .forTesting(type, methodRemover, facetedMethod));

        val callbackMethods = callbackMethod.getMethodNames()
                .map(methodName->findMethod(type, methodName));

        Assertions.assertEquals(expectedCallbackCount, callbackMethods.size());

        val facet = facetedMethod.getFacet(facetType);
        assertNotNull(facet);
        assertTrue(facet instanceof ImperativeFacet);
        val imperativeFacet = facet;

        callbackMethods.forEach(method->{
            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(method));
            assertTrue(imperativeFacet.getMethods().contains(method));
        });

    }

}
