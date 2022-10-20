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
package org.apache.causeway.core.metamodel.facets.actions;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.actions.validate.ActionValidationFacet;
import org.apache.causeway.core.metamodel.facets.actions.validate.method.ActionValidationFacetViaMethod;
import org.apache.causeway.core.metamodel.facets.actions.validate.method.ActionValidationFacetViaMethodFactory;
import org.apache.causeway.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.causeway.core.metamodel.facets.param.autocomplete.method.ActionParameterAutoCompleteFacetViaMethod;
import org.apache.causeway.core.metamodel.facets.param.autocomplete.method.ActionParameterAutoCompleteFacetViaMethodFactory;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.causeway.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethod;
import org.apache.causeway.core.metamodel.facets.param.choices.methodnum.ActionParameterChoicesFacetViaMethodFactory;
import org.apache.causeway.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.causeway.core.metamodel.facets.param.defaults.methodnum.ActionParameterDefaultsFacetViaMethod;
import org.apache.causeway.core.metamodel.facets.param.defaults.methodnum.ActionParameterDefaultsFacetViaMethodFactory;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.val;

class ActionMethodsFacetFactoryTest extends AbstractFacetFactoryTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        val specLoader = metaModelContext.getSpecificationLoader();
        ObjectSpecification voidSpec = specLoader.loadSpecification(void.class);

        Mockito.when(mockInteractionService.currentInteractionContext()).thenReturn(Optional.of(iaContext));
    }

    public void testInstallsValidateMethodNoArgsFacetAndRemovesMethod() {
        val facetFactory = new ActionValidationFacetViaMethodFactory(metaModelContext);

        @SuppressWarnings("unused")
        class Customer {

            public void someAction() {
            }

            public String validateSomeAction() {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");
        final Method validateMethod = findMethod(Customer.class, "validateSomeAction");

        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, FeatureType.ACTION, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionValidationFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionValidationFacetViaMethod);
        final ActionValidationFacetViaMethod actionValidationFacetViaMethod = (ActionValidationFacetViaMethod) facet;
        assertEquals(validateMethod, actionValidationFacetViaMethod.getMethods().getFirstOrFail());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(validateMethod));
    }

    public void testInstallsValidateMethodSomeArgsFacetAndRemovesMethod() {
        val facetFactory = new ActionValidationFacetViaMethodFactory(metaModelContext);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final int y) {
            }

            @SuppressWarnings("unused")
            public String validateSomeAction(final int x, final int y) {
                return null;
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, int.class });
        final Method validateMethod = findMethod(Customer.class, "validateSomeAction", new Class[] { int.class, int.class });

        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, FeatureType.ACTION, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionValidationFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionValidationFacetViaMethod);
        final ActionValidationFacetViaMethod actionValidationFacetViaMethod = (ActionValidationFacetViaMethod) facet;
        assertEquals(validateMethod, actionValidationFacetViaMethod.getMethods().getFirstOrFail());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(validateMethod));
    }

    public void testInstallsParameterDefaultsMethodAndRemovesMethod() {
        val facetFactory = new ActionParameterDefaultsFacetViaMethodFactory(metaModelContext);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final long y) {
            }

            @SuppressWarnings("unused")
            public int default0SomeAction() {
                return 0;
            }

            @SuppressWarnings("unused")
            public long default1SomeAction() {
                return 0;
            }
        }

        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, long.class });
        final Method default0Method = findMethod(Customer.class, "default0SomeAction", new Class[] {});
        final Method default1Method = findMethod(Customer.class, "default1SomeAction", new Class[]{});

        final FacetedMethod facetHolderWithParms = FacetedMethod
                .createForAction(metaModelContext, Customer.class, actionMethod);

        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, FeatureType.ACTION, actionMethod, methodRemover, facetHolderWithParms));

        final Facet facet0 = facetHolderWithParms.getParameters().getElseFail(0).getFacet(ActionParameterDefaultsFacet.class);
        assertNotNull(facet0);
        assertTrue(facet0 instanceof ActionParameterDefaultsFacetViaMethod);
        final ActionParameterDefaultsFacetViaMethod actionDefaultFacetViaMethod0 = (ActionParameterDefaultsFacetViaMethod) facet0;
        assertEquals(default0Method, actionDefaultFacetViaMethod0.getMethods().getFirstOrFail());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(default0Method));

        final Facet facet1 = facetHolderWithParms.getParameters().getElseFail(1).getFacet(ActionParameterDefaultsFacet.class);
        assertNotNull(facet1);
        assertTrue(facet1 instanceof ActionParameterDefaultsFacetViaMethod);
        final ActionParameterDefaultsFacetViaMethod actionDefaultFacetViaMethod1 = (ActionParameterDefaultsFacetViaMethod) facet1;
        assertEquals(default1Method, actionDefaultFacetViaMethod1.getMethods().getFirstOrFail());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(default1Method));

    }

    public void testInstallsParameterChoicesMethodAndRemovesMethod() {
        val facetFactory = new ActionParameterChoicesFacetViaMethodFactory(metaModelContext);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final long y, final long z) {
            }

            @SuppressWarnings("unused")
            public Collection<Integer> choices0SomeAction() {
                return Collections.emptyList();
            }

            @SuppressWarnings("unused")
            public List<Long> choices1SomeAction() {
                return Collections.emptyList();
            }

            @SuppressWarnings("unused")
            public Set<Long> choices2SomeAction() {
                return Collections.emptySet();
            }
        }

        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, long.class, long.class });
        final Method choices0Method = findMethod(Customer.class, "choices0SomeAction", new Class[] {});
        final Method choices1Method = findMethod(Customer.class, "choices1SomeAction", new Class[] {});
        final Method choices2Method = findMethod(Customer.class, "choices2SomeAction", new Class[] {});

        final FacetedMethod facetHolderWithParms = FacetedMethod.createForAction(
                metaModelContext, Customer.class, actionMethod);

        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, FeatureType.ACTION, actionMethod, methodRemover, facetHolderWithParms));

        final Facet facet0 = facetHolderWithParms.getParameters().getElseFail(0).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet0);
        assertTrue(facet0 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod0 = (ActionParameterChoicesFacetViaMethod) facet0;
        assertEquals(choices0Method, actionChoicesFacetViaMethod0.getMethods().getFirstOrFail());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(choices0Method));

        final Facet facet1 = facetHolderWithParms.getParameters().getElseFail(1).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet1);
        assertTrue(facet1 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod1 = (ActionParameterChoicesFacetViaMethod) facet1;
        assertEquals(choices1Method, actionChoicesFacetViaMethod1.getMethods().getFirstOrFail());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(choices1Method));

        final Facet facet2 = facetHolderWithParms.getParameters().getElseFail(2).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet2);
        assertTrue(facet2 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod2 = (ActionParameterChoicesFacetViaMethod) facet2;
        assertEquals(choices2Method, actionChoicesFacetViaMethod2.getMethods().getFirstOrFail());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(choices2Method));


    }

    public void testInstallsParameterAutoCompleteMethodAndRemovesMethod() {
        val facetFactory = new ActionParameterAutoCompleteFacetViaMethodFactory(metaModelContext);

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final long y) {
            }

            @SuppressWarnings("unused")
            public List<Integer> autoComplete0SomeAction(final String searchArg) {
                return Collections.emptyList();
            }
        }

        final Method actionMethod = findMethod(Customer.class, "someAction", new Class[] { int.class, long.class });
        final Method autoComplete0Method = findMethod(Customer.class, "autoComplete0SomeAction", new Class[] {String.class});

        final FacetedMethod facetHolderWithParms = FacetedMethod
                .createForAction(metaModelContext, Customer.class, actionMethod);

        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, FeatureType.ACTION, actionMethod, methodRemover, facetHolderWithParms));

        final Facet facet0 = facetHolderWithParms.getParameters().getElseFail(0).getFacet(ActionParameterAutoCompleteFacet.class);
        assertNotNull(facet0);
        assertTrue(facet0 instanceof ActionParameterAutoCompleteFacetViaMethod);
        final ActionParameterAutoCompleteFacetViaMethod actionAutoCompleteFacetViaMethod0 = (ActionParameterAutoCompleteFacetViaMethod) facet0;
        assertEquals(autoComplete0Method, actionAutoCompleteFacetViaMethod0.getMethods().getFirstOrFail());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(autoComplete0Method));
    }


}
