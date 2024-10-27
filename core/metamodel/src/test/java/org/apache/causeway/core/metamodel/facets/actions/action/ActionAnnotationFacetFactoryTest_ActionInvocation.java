/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.core.metamodel.facets.actions.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForAction;
import org.apache.causeway.core.metamodel.facets.members.disabled.method.DisableForContextFacet;
import org.apache.causeway.core.metamodel.facets.members.disabled.method.DisableForContextFacetViaMethod;
import org.apache.causeway.core.metamodel.facets.members.disabled.method.DisableForContextFacetViaMethodFactory;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.causeway.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethod;
import org.apache.causeway.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethodFactory;

class ActionAnnotationFacetFactoryTest_ActionInvocation
extends FacetFactoryTestAbstract {

    private ActionAnnotationFacetFactory facetFactory;

    private void processInvocation(
            final ActionAnnotationFacetFactory facetFactory, final ProcessMethodContext processMethodContext) {
        var actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processDomainEvent(processMethodContext, actionIfAny);
    }

    @BeforeEach
    public void setUp() {
        this.facetFactory =  new ActionAnnotationFacetFactory(getMetaModelContext());
    }

    @Test
    void actionInvocationFacetIsInstalledAndMethodRemoved() {
        @SuppressWarnings("unused")
        class Customer {
            public void someAction() {}
        }

        final ResolvedMethod actionMethod = findMethodExactOrFail(Customer.class, "someAction");

        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod) -> {
            //when
            processInvocation(facetFactory, processMethodContext);

            //then
            final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof ActionInvocationFacetForAction);
            var actionInvocationFacetViaMethod = (ActionInvocationFacetForAction) facet;
            assertMethodEqualsFirstIn(actionMethod, actionInvocationFacetViaMethod);
            assertMethodWasRemoved(actionMethod);
        });
    }

    @Test
    void actionReturnTypeWhenVoid() {
        @SuppressWarnings("unused")
        class Customer {
            public void someAction() {}
        }

        var voidSpec = getSpecificationLoader().loadSpecification(void.class);

        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod) -> {
            //when
            processInvocation(facetFactory, processMethodContext);
            //then
            final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
            var actionInvocationFacetViaMethod = (ActionInvocationFacetForAction) facet;
            assertEquals(voidSpec, actionInvocationFacetViaMethod.getReturnType());
        });
    }

    @Test
    void actionReturnTypeWhenNotVoid() {
        @SuppressWarnings("unused")
        class Customer {
            public String someAction() { return null; }
        }

        var stringSpec = getSpecificationLoader().loadSpecification(java.lang.String.class);

        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod) -> {
            //when
            processInvocation(facetFactory, processMethodContext);
            //then
            final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
            var actionInvocationFacetViaMethod = (ActionInvocationFacetForAction) facet;
            assertEquals(stringSpec, actionInvocationFacetViaMethod.getReturnType());
        });
    }

    @Test
    void actionOnType() {
        @SuppressWarnings("unused")
        class LocalCustomer {
            public String someAction() { return null; }
        }

        var customerSpec = getSpecificationLoader().loadSpecification(LocalCustomer.class);

        actionScenario(LocalCustomer.class, "someAction", (processMethodContext, facetHolder, facetedMethod) -> {
            //when
            processInvocation(facetFactory, processMethodContext);
            //then
            final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
            var actionInvocationFacetViaMethod = (ActionInvocationFacetForAction) facet;
            assertEquals(
                    customerSpec,
                    actionInvocationFacetViaMethod.getDeclaringType());
        });
    }

    @Test
    void actionsPickedUpFromSuperclass() {
        @SuppressWarnings("unused")
        class Customer {
            public void someAction(final int x, final long y) {}
        }
        class CustomerEx extends Customer {
        }
        actionScenario(CustomerEx.class, "someAction", (processMethodContext, facetHolder, facetedMethod) -> {
            //when
            processInvocation(facetFactory, processMethodContext);
            //then
            final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
            assertNotNull(facet);
        });
    }

    @Test
    void actionsPickedUpFromSuperclassButHelpersFromSubClass() {
        var facetFactoryForChoices = new ActionParameterChoicesFacetViaMethodFactory(getMetaModelContext());
        var facetFactoryForDisable = new DisableForContextFacetViaMethodFactory(getMetaModelContext());

        @SuppressWarnings("unused")
        class Customer {
            public void someAction(final int x, final long y) { }
            public int[] choices0SomeAction() { return new int[0]; }
        }
        @SuppressWarnings("unused")
        class CustomerEx extends Customer {
            @Override
            public int[] choices0SomeAction() { return new int[0]; }
            public long[] choices1SomeAction() { return new long[0]; }
            public String disableSomeAction() { return null; }
        }

        final ResolvedMethod choices0Method = findMethodExactOrFail(CustomerEx.class, "choices0SomeAction", new Class[] {});
        final ResolvedMethod choices1Method = findMethodExactOrFail(CustomerEx.class, "choices1SomeAction", new Class[] {});
        final ResolvedMethod disableMethod = findMethodExactOrFail(CustomerEx.class, "disableSomeAction", new Class[] {});

        actionScenario(CustomerEx.class, "someAction", (processMethodContext, facetHolder, facetedMethod) -> {
            //when
            processInvocation(facetFactory, processMethodContext);
            facetFactoryForChoices.process(processMethodContext);
            facetFactoryForDisable.process(processMethodContext);
            //then
            final Facet facet0 = facetedMethod.getFacet(ActionInvocationFacet.class);
            assertNotNull(facet0);

            final Facet facet1 = facetedMethod.getParameters().getElseFail(0).getFacet(ActionParameterChoicesFacet.class);
            assertNotNull(facet1);
            assertTrue(facet1 instanceof ActionParameterChoicesFacetViaMethod);
            var actionChoicesFacetViaMethod0 = (ActionParameterChoicesFacetViaMethod) facet1;
            assertMethodEqualsFirstIn(choices0Method, actionChoicesFacetViaMethod0);

            final Facet facet2 = facetedMethod.getParameters().getElseFail(1).getFacet(ActionParameterChoicesFacet.class);
            assertNotNull(facet2);
            assertTrue(facet2 instanceof ActionParameterChoicesFacetViaMethod);
            var actionChoicesFacetViaMethod1 = (ActionParameterChoicesFacetViaMethod) facet2;
            assertMethodEqualsFirstIn(choices1Method, actionChoicesFacetViaMethod1);

            final Facet facet3 = facetedMethod.getFacet(DisableForContextFacet.class);
            assertNotNull(facet3);
            assertTrue(facet3 instanceof DisableForContextFacetViaMethod);
            var disableFacetViaMethod = (DisableForContextFacetViaMethod) facet3;
            assertMethodEqualsFirstIn(disableMethod, disableFacetViaMethod);
        });
    }
}
