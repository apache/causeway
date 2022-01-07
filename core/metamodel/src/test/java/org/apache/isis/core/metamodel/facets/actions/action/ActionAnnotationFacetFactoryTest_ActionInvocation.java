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
package org.apache.isis.core.metamodel.facets.actions.action;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventAbstract;
import org.apache.isis.core.metamodel.facets.members.disabled.method.DisableForContextFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.method.DisableForContextFacetViaMethod;
import org.apache.isis.core.metamodel.facets.members.disabled.method.DisableForContextFacetViaMethodFactory;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethod;
import org.apache.isis.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethodFactory;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.testspec.ObjectSpecificationStub;

import lombok.val;

@SuppressWarnings("unused")
public class ActionAnnotationFacetFactoryTest_ActionInvocation
extends AbstractFacetFactoryTest {

    private ObjectSpecification voidSpec;
    private ObjectSpecification stringSpec;
    private ObjectSpecification customerSpec;
    private ActionAnnotationFacetFactory facetFactory;

    private void processInvocation(
            final ActionAnnotationFacetFactory facetFactory, final ProcessMethodContext processMethodContext) {

        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processInvocation(processMethodContext, actionIfAny);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.facetFactory =  new ActionAnnotationFacetFactory(metaModelContext);

        voidSpec = new ObjectSpecificationStub(metaModelContext, void.class);
        stringSpec = new ObjectSpecificationStub(metaModelContext, java.lang.String.class);
        customerSpec = new ObjectSpecificationStub(metaModelContext, Customer.class);
    }

    public void testActionInvocationFacetIsInstalledAndMethodRemoved() {

        allowing_specificationLoader_loadSpecification_any_willReturn(voidSpec);

        class Customer {
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        processInvocation(facetFactory, ProcessMethodContext
                .forTesting(Customer.class, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionInvocationFacetForDomainEventAbstract);
        final ActionInvocationFacetForDomainEventAbstract actionInvocationFacetViaMethod = (ActionInvocationFacetForDomainEventAbstract) facet;
        assertEquals(actionMethod, actionInvocationFacetViaMethod.getMethods().getFirstOrFail());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(actionMethod));
    }

    public void testActionReturnTypeWhenVoid() {

        allowing_specificationLoader_loadSpecification_any_willReturn(voidSpec);

        class Customer {
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        processInvocation(facetFactory, ProcessMethodContext
                .forTesting(Customer.class, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
        final ActionInvocationFacetForDomainEventAbstract actionInvocationFacetViaMethod = (ActionInvocationFacetForDomainEventAbstract) facet;
        assertEquals(voidSpec, actionInvocationFacetViaMethod.getReturnType());
    }

    public void testActionReturnTypeWhenNotVoid() {

        allowing_specificationLoader_loadSpecification_any_willReturn(stringSpec);

        class Customer {
            public String someAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        processInvocation(facetFactory, ProcessMethodContext
                .forTesting(Customer.class, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
        final ActionInvocationFacetForDomainEventAbstract actionInvocationFacetViaMethod = (ActionInvocationFacetForDomainEventAbstract) facet;
        assertEquals(stringSpec, actionInvocationFacetViaMethod.getReturnType());
    }

    public void testActionOnType() {

        allowing_specificationLoader_loadSpecification_any_willReturn(customerSpec);

        class Customer {
            public String someAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        processInvocation(facetFactory, ProcessMethodContext
                .forTesting(Customer.class, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionInvocationFacet.class);
        final ActionInvocationFacetForDomainEventAbstract actionInvocationFacetViaMethod = (ActionInvocationFacetForDomainEventAbstract) facet;
        assertEquals(customerSpec, actionInvocationFacetViaMethod.getDeclaringType());
    }

    public void testActionsPickedUpFromSuperclass() {

        allowing_specificationLoader_loadSpecification_any_willReturn(voidSpec);

        class Customer {
            public void someAction(final int x, final long y) {
            }
        }

        class CustomerEx extends Customer {
        }

        final Method actionMethod = findMethod(CustomerEx.class, "someAction", new Class[] { int.class, long.class });

        final FacetedMethod facetHolderWithParms = FacetedMethod
                .createForAction(metaModelContext, CustomerEx.class, actionMethod);

        processInvocation(facetFactory, ProcessMethodContext
                .forTesting(CustomerEx.class, null, actionMethod, methodRemover, facetHolderWithParms));

        final Facet facet0 = facetHolderWithParms.getFacet(ActionInvocationFacet.class);
        assertNotNull(facet0);
    }

    public void testActionsPickedUpFromSuperclassButHelpersFromSubClass() {

        allowing_specificationLoader_loadSpecification_any_willReturn(voidSpec);

        val facetFactoryForChoices = new ActionParameterChoicesFacetViaMethodFactory(metaModelContext);
        val facetFactoryForDisable = new DisableForContextFacetViaMethodFactory(metaModelContext);

        class Customer {

            public void someAction(final int x, final long y) {
            }


            public int[] choices0SomeAction() {
                return new int[0];
            }
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

        final Method actionMethod = findMethod(CustomerEx.class, "someAction", new Class[] { int.class, long.class });
        final Method choices0Method = findMethod(CustomerEx.class, "choices0SomeAction", new Class[] {});
        final Method choices1Method = findMethod(CustomerEx.class, "choices1SomeAction", new Class[] {});
        final Method disableMethod = findMethod(CustomerEx.class, "disableSomeAction", new Class[] {});

        final FacetedMethod facetHolderWithParms = FacetedMethod.createForAction(metaModelContext, CustomerEx.class, actionMethod);

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
        assertEquals(choices0Method, actionChoicesFacetViaMethod0.getMethods().getFirstOrFail());

        final Facet facet2 = facetHolderWithParms.getParameters().getElseFail(1).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet2);
        assertTrue(facet2 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod1 = (ActionParameterChoicesFacetViaMethod) facet2;
        assertEquals(choices1Method, actionChoicesFacetViaMethod1.getMethods().getFirstOrFail());

        final Facet facet3 = facetHolderWithParms.getFacet(DisableForContextFacet.class);
        assertNotNull(facet3);
        assertTrue(facet3 instanceof DisableForContextFacetViaMethod);
        final DisableForContextFacetViaMethod disableFacetViaMethod = (DisableForContextFacetViaMethod) facet3;
        assertEquals(disableMethod, disableFacetViaMethod.getMethods().getFirstOrFail());
    }
}
