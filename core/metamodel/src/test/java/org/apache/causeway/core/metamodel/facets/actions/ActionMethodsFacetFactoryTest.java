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
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.internal.reflection._MethodFacades;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
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

import lombok.val;

class ActionMethodsFacetFactoryTest
extends FacetFactoryTestAbstract {

    @Test
    void installsValidateMethodNoArgsFacetAndRemovesMethod() {
        val facetFactory = new ActionValidationFacetViaMethodFactory(getMetaModelContext());

        @SuppressWarnings("unused")
        class Customer {
            public void someAction() {}
            public String validateSomeAction() { return null;}
        }

        final Method validateMethod = findMethodExactOrFail(Customer.class, "validateSomeAction");

        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter) -> {
            //when
            facetFactory.process(processMethodContext);

            //then
            final Facet facet = facetedMethod.getFacet(ActionValidationFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof ActionValidationFacetViaMethod);
            val actionValidationFacetViaMethod = (ActionValidationFacetViaMethod) facet;
            assertMethodEquals(validateMethod, actionValidationFacetViaMethod.getMethods().getFirstElseFail().asMethodElseFail());

            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(validateMethod));
        });

    }

    @Test
    void installsValidateMethodSomeArgsFacetAndRemovesMethod() {
        val facetFactory = new ActionValidationFacetViaMethodFactory(getMetaModelContext());

        @SuppressWarnings("unused")
        class Customer {
            public void someAction(final int x, final int y) {}
            public String validateSomeAction(final int x, final int y) { return null;}
        }

        final Method validateMethod = findMethodExactOrFail(Customer.class, "validateSomeAction", new Class[] { int.class, int.class });

        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod, facetedMethodParameter) -> {
            //when
            facetFactory.process(processMethodContext);

            //then
            final Facet facet = facetedMethod.getFacet(ActionValidationFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof ActionValidationFacetViaMethod);
            val actionValidationFacetViaMethod = (ActionValidationFacetViaMethod) facet;
            assertMethodEquals(validateMethod, actionValidationFacetViaMethod.getMethods().getFirstElseFail().asMethodElseFail());

            assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(validateMethod));
        });
    }

    @Test
    void installsParameterDefaultsMethodAndRemovesMethod() {
        val facetFactory = new ActionParameterDefaultsFacetViaMethodFactory(getMetaModelContext());

        @SuppressWarnings("unused")
        class Customer {
            public void someAction(final int x, final long y) {}
            public int default0SomeAction() { return 0; }
            public long default1SomeAction() { return 0; }
        }

        final Method actionMethod = findMethodExactOrFail(Customer.class, "someAction", new Class[] { int.class, long.class });
        final Method default0Method = findMethodExactOrFail(Customer.class, "default0SomeAction", new Class[] {});
        final Method default1Method = findMethodExactOrFail(Customer.class, "default1SomeAction", new Class[]{});

        final FacetedMethod facetHolderWithParms = FacetedMethod
                .createForAction(getMetaModelContext(), Customer.class, _MethodFacades.regular(actionMethod));

        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, FeatureType.ACTION, actionMethod, methodRemover, facetHolderWithParms));

        final Facet facet0 = facetHolderWithParms.getParameters().getElseFail(0).getFacet(ActionParameterDefaultsFacet.class);
        assertNotNull(facet0);
        assertTrue(facet0 instanceof ActionParameterDefaultsFacetViaMethod);
        final ActionParameterDefaultsFacetViaMethod actionDefaultFacetViaMethod0 = (ActionParameterDefaultsFacetViaMethod) facet0;
        assertMethodEquals(default0Method, actionDefaultFacetViaMethod0.getMethods().getFirstElseFail().asMethodElseFail());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(default0Method));

        final Facet facet1 = facetHolderWithParms.getParameters().getElseFail(1).getFacet(ActionParameterDefaultsFacet.class);
        assertNotNull(facet1);
        assertTrue(facet1 instanceof ActionParameterDefaultsFacetViaMethod);
        final ActionParameterDefaultsFacetViaMethod actionDefaultFacetViaMethod1 = (ActionParameterDefaultsFacetViaMethod) facet1;
        assertMethodEquals(default1Method, actionDefaultFacetViaMethod1.getMethods().getFirstElseFail().asMethodElseFail());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(default1Method));

    }

    @Test
    void installsParameterChoicesMethodAndRemovesMethod() {
        val facetFactory = new ActionParameterChoicesFacetViaMethodFactory(getMetaModelContext());

        @SuppressWarnings("unused")
        class Customer {

            public void someAction(final int x, final long y, final long z) {}
            public Collection<Integer> choices0SomeAction() { return Collections.emptyList(); }
            public List<Long> choices1SomeAction() { return Collections.emptyList(); }
            public Set<Long> choices2SomeAction() { return Collections.emptySet(); }
        }

        final Method actionMethod = findMethodExactOrFail(Customer.class, "someAction", new Class[] { int.class, long.class, long.class });
        final Method choices0Method = findMethodExactOrFail(Customer.class, "choices0SomeAction", new Class[] {});
        final Method choices1Method = findMethodExactOrFail(Customer.class, "choices1SomeAction", new Class[] {});
        final Method choices2Method = findMethodExactOrFail(Customer.class, "choices2SomeAction", new Class[] {});

        final FacetedMethod facetHolderWithParms = FacetedMethod.createForAction(
                getMetaModelContext(), Customer.class, _MethodFacades.regular(actionMethod));

        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, FeatureType.ACTION, actionMethod, methodRemover, facetHolderWithParms));

        final Facet facet0 = facetHolderWithParms.getParameters().getElseFail(0).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet0);
        assertTrue(facet0 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod0 = (ActionParameterChoicesFacetViaMethod) facet0;
        assertMethodEquals(choices0Method, actionChoicesFacetViaMethod0.getMethods().getFirstElseFail().asMethodElseFail());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(choices0Method));

        final Facet facet1 = facetHolderWithParms.getParameters().getElseFail(1).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet1);
        assertTrue(facet1 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod1 = (ActionParameterChoicesFacetViaMethod) facet1;
        assertMethodEquals(choices1Method, actionChoicesFacetViaMethod1.getMethods().getFirstElseFail().asMethodElseFail());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(choices1Method));

        final Facet facet2 = facetHolderWithParms.getParameters().getElseFail(2).getFacet(ActionParameterChoicesFacet.class);
        assertNotNull(facet2);
        assertTrue(facet2 instanceof ActionParameterChoicesFacetViaMethod);
        final ActionParameterChoicesFacetViaMethod actionChoicesFacetViaMethod2 = (ActionParameterChoicesFacetViaMethod) facet2;
        assertMethodEquals(choices2Method, actionChoicesFacetViaMethod2.getMethods().getFirstElseFail().asMethodElseFail());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(choices2Method));


    }

    @Test
    void installsParameterAutoCompleteMethodAndRemovesMethod() {
        val facetFactory = new ActionParameterAutoCompleteFacetViaMethodFactory(getMetaModelContext());

        class Customer {
            @SuppressWarnings("unused")
            public void someAction(final int x, final long y) {
            }

            @SuppressWarnings("unused")
            public List<Integer> autoComplete0SomeAction(final String searchArg) {
                return Collections.emptyList();
            }
        }

        final Method actionMethod = findMethodExactOrFail(Customer.class, "someAction", new Class[] { int.class, long.class });
        final Method autoComplete0Method = findMethodExactOrFail(Customer.class, "autoComplete0SomeAction", new Class[] {String.class});

        final FacetedMethod facetHolderWithParms = FacetedMethod
                .createForAction(getMetaModelContext(), Customer.class, _MethodFacades.regular(actionMethod));

        facetFactory.process(ProcessMethodContext
                .forTesting(Customer.class, FeatureType.ACTION, actionMethod, methodRemover, facetHolderWithParms));

        final Facet facet0 = facetHolderWithParms.getParameters().getElseFail(0).getFacet(ActionParameterAutoCompleteFacet.class);
        assertNotNull(facet0);
        assertTrue(facet0 instanceof ActionParameterAutoCompleteFacetViaMethod);
        final ActionParameterAutoCompleteFacetViaMethod actionAutoCompleteFacetViaMethod0 = (ActionParameterAutoCompleteFacetViaMethod) facet0;
        assertMethodEquals(autoComplete0Method, actionAutoCompleteFacetViaMethod0.getMethods().getFirstElseFail().asMethodElseFail());

        assertTrue(methodRemover.getRemovedMethodMethodCalls().contains(autoComplete0Method));
    }

}
