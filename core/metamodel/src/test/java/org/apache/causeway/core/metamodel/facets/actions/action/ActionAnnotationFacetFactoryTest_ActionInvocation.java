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

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEvent;
import org.apache.causeway.core.metamodel.facets.members.disabled.method.DisableForContextFacet;
import org.apache.causeway.core.metamodel.facets.members.disabled.method.DisableForContextFacetViaMethod;
import org.apache.causeway.core.metamodel.facets.members.disabled.method.DisableForContextFacetViaMethodFactory;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.causeway.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethod;
import org.apache.causeway.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethodFactory;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.val;

@SuppressWarnings("unused")
class ActionAnnotationFacetFactoryTest_ActionInvocation
extends FacetFactoryTestAbstract {

    private ObjectSpecification voidSpec;
    private ObjectSpecification stringSpec;
    private ActionAnnotationFacetFactory facetFactory;

    private void processInvocation(
            final ActionAnnotationFacetFactory facetFactory, final ProcessMethodContext processMethodContext) {

        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processInvocation(processMethodContext, actionIfAny);
    }

    @BeforeEach
    public void setUp() {

        this.facetFactory =  new ActionAnnotationFacetFactory(getMetaModelContext());

        val specLoader = getSpecificationLoader();
        voidSpec = specLoader.loadSpecification(void.class);
        stringSpec = specLoader.loadSpecification(java.lang.String.class);
    }

    public void testActionInvocationFacetIsInstalledAndMethodRemoved() {

        class Customer {
            public void someAction() {}
        }

        final Method actionMethod = findMethodExactOrFail(Customer.class, "someAction");

        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter) -> {
            //when
            processInvocation(facetFactory, processMethodContext);

            //then
            final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof ActionInvocationFacetForDomainEvent);
            final ActionInvocationFacetForDomainEvent actionInvocationFacetViaMethod = (ActionInvocationFacetForDomainEvent) facet;
            assertEquals(actionMethod, actionInvocationFacetViaMethod.getMethods().getFirstElseFail());
            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(actionMethod));
        });
    }

    public void testActionReturnTypeWhenVoid() {

        class Customer {
            public void someAction() {}
        }

        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter) -> {
            //when
            processInvocation(facetFactory, processMethodContext);
            //then
            final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
            final ActionInvocationFacetForDomainEvent actionInvocationFacetViaMethod = (ActionInvocationFacetForDomainEvent) facet;
            assertEquals(voidSpec, actionInvocationFacetViaMethod.getReturnType());
        });
    }

    public void testActionReturnTypeWhenNotVoid() {

        class Customer {
            public String someAction() { return null; }
        }

        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter) -> {
            //when
            processInvocation(facetFactory, processMethodContext);
            //then
            final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
            final ActionInvocationFacetForDomainEvent actionInvocationFacetViaMethod = (ActionInvocationFacetForDomainEvent) facet;
            assertEquals(stringSpec, actionInvocationFacetViaMethod.getReturnType());
        });
    }

    public void testActionOnType() {

        class LocalCustomer {
            public String someAction() {
                return null;
            }
        }

        val customerSpec = getSpecificationLoader().loadSpecification(LocalCustomer.class);

        actionScenario(LocalCustomer.class, "someAction", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter) -> {
            //when
            processInvocation(facetFactory, processMethodContext);
            // then
            final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
            final ActionInvocationFacetForDomainEvent actionInvocationFacetViaMethod =
                    (ActionInvocationFacetForDomainEvent) facet;
            assertEquals(
                    customerSpec,
                    actionInvocationFacetViaMethod.getDeclaringType());
        });
    }

    public void testActionsPickedUpFromSuperclass() {

        class Customer {
            public void someAction(final int x, final long y) {
            }
        }

        class CustomerEx extends Customer {
        }

        final Method actionMethod = findMethodExactOrFail(CustomerEx.class, "someAction", new Class[] { int.class, long.class });

        final FacetedMethod facetHolderWithParms = FacetedMethod
                .createForAction(getMetaModelContext(), CustomerEx.class, _MethodFacades.regular(actionMethod));

        processInvocation(facetFactory, ProcessMethodContext
                .forTesting(CustomerEx.class, null, actionMethod, methodRemover, facetHolderWithParms));

        final Facet facet0 = facetHolderWithParms.getFacet(ActionInvocationFacet.class);
        assertNotNull(facet0);
    }

    public void testActionsPickedUpFromSuperclassButHelpersFromSubClass() {

        val facetFactoryForChoices = new ActionParameterChoicesFacetViaMethodFactory(getMetaModelContext());
        val facetFactoryForDisable = new DisableForContextFacetViaMethodFactory(getMetaModelContext());

        class Customer {
            public void someAction(final int x, final long y) { }
            public int[] choices0SomeAction() { return new int[0]; }
        }

        class CustomerEx extends Customer {
            @Override
            public int[] choices0SomeAction() {
                return new int[0];
            }
            public long[] choices1SomeAction() {
                return new long[0];
            }
            public String disableSomeAction() {
                return null;
            }
        }

        final Method actionMethod = findMethodExactOrFail(CustomerEx.class, "someAction", new Class[] { int.class, long.class });
        final Method choices0Method = findMethodExactOrFail(CustomerEx.class, "choices0SomeAction", new Class[] {});
        final Method choices1Method = findMethodExactOrFail(CustomerEx.class, "choices1SomeAction", new Class[] {});
        final Method disableMethod = findMethodExactOrFail(CustomerEx.class, "disableSomeAction", new Class[] {});

        final FacetedMethod facetHolderWithParms = FacetedMethod.createForAction(getMetaModelContext(), CustomerEx.class,
                _MethodFacades.regular(actionMethod));

        final ProcessMethodContext processMethodContext = ProcessMethodContext
                .forTesting(CustomerEx.class, FeatureType.ACTION, actionMethod, methodRemover, facetHolderWithParms);
        processInvocation(facetFactory, processMethodContext);

        facetFactoryForChoices.process(processMethodContext);
        facetFactoryForDisable.process(processMethodContext);

        final Facet facet0 = facetHolderWithParms.getFacet(ActionInvocationFacet.class);
        assertNotNull(facet0);

        final Facet facet1 = facetHolderWithParms.getParameters().getElseFail(0).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet1);
        assertTrue(facet1 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod0 = (ActionParameterChoicesFacetViaMethod) facet1;
        assertEquals(choices0Method, actionChoicesFacetViaMethod0.getMethods().getFirstElseFail());

        final Facet facet2 = facetHolderWithParms.getParameters().getElseFail(1).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet2);
        assertTrue(facet2 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod1 = (ActionParameterChoicesFacetViaMethod) facet2;
        assertEquals(choices1Method, actionChoicesFacetViaMethod1.getMethods().getFirstElseFail());

        final Facet facet3 = facetHolderWithParms.getFacet(DisableForContextFacet.class);
        assertNotNull(facet3);
        assertTrue(facet3 instanceof DisableForContextFacetViaMethod);
        final DisableForContextFacetViaMethod disableFacetViaMethod = (DisableForContextFacetViaMethod) facet3;
        assertEquals(disableMethod, disableFacetViaMethod.getMethods().getFirstElseFail());
    }
}
