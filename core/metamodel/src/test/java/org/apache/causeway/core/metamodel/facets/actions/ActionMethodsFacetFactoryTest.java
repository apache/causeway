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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
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

class ActionMethodsFacetFactoryTest
extends FacetFactoryTestAbstract {

    @Test
    void installsValidateMethodNoArgsFacetAndRemovesMethod() {
        var facetFactory = new ActionValidationFacetViaMethodFactory(getMetaModelContext());

        @SuppressWarnings("unused")
        class Customer {
            public void someAction() {}
            public String validateSomeAction() { return null;}
        }

        final ResolvedMethod validateMethod = findMethodExactOrFail(Customer.class, "validateSomeAction");

        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod) -> {
            //when
            facetFactory.process(processMethodContext);

            //then
            final Facet facet = facetedMethod.getFacet(ActionValidationFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof ActionValidationFacetViaMethod);
            var actionValidationFacetViaMethod = (ActionValidationFacetViaMethod) facet;
            assertMethodEqualsFirstIn(validateMethod, actionValidationFacetViaMethod);
            assertMethodWasRemoved(validateMethod);
        });

    }

    @Test
    void installsValidateMethodSomeArgsFacetAndRemovesMethod() {
        var facetFactory = new ActionValidationFacetViaMethodFactory(getMetaModelContext());

        @SuppressWarnings("unused")
        class Customer {
            public void someAction(final int x, final int y) {}
            public String validateSomeAction(final int x, final int y) { return null;}
        }

        final ResolvedMethod validateMethod = findMethodExactOrFail(Customer.class, "validateSomeAction", new Class[] { int.class, int.class });

        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod) -> {
            //when
            facetFactory.process(processMethodContext);

            //then
            final Facet facet = facetedMethod.getFacet(ActionValidationFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof ActionValidationFacetViaMethod);
            var actionValidationFacetViaMethod = (ActionValidationFacetViaMethod) facet;
            assertMethodEqualsFirstIn(validateMethod, actionValidationFacetViaMethod);
            assertMethodWasRemoved(validateMethod);
        });
    }

    @Test
    void installsParameterDefaultsMethodAndRemovesMethod() {
        var facetFactory = new ActionParameterDefaultsFacetViaMethodFactory(getMetaModelContext());

        @SuppressWarnings("unused")
        class Customer {
            public void someAction(final int x, final long y) {}
            public int default0SomeAction() { return 0; }
            public long default1SomeAction() { return 0; }
        }

        final ResolvedMethod default0Method = findMethodExactOrFail(Customer.class, "default0SomeAction");
        final ResolvedMethod default1Method = findMethodExactOrFail(Customer.class, "default1SomeAction");

        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod) -> {
            //when
            facetFactory.process(processMethodContext);
            //then
            final Facet facet0 = facetedMethod.getParameters().getElseFail(0).getFacet(ActionParameterDefaultsFacet.class);
            assertNotNull(facet0);
            assertTrue(facet0 instanceof ActionParameterDefaultsFacetViaMethod);
            final ActionParameterDefaultsFacetViaMethod actionDefaultFacetViaMethod0 = (ActionParameterDefaultsFacetViaMethod) facet0;
            assertMethodEqualsFirstIn(default0Method, actionDefaultFacetViaMethod0);
            assertMethodWasRemoved(default0Method);

            final Facet facet1 = facetedMethod.getParameters().getElseFail(1).getFacet(ActionParameterDefaultsFacet.class);
            assertNotNull(facet1);
            assertTrue(facet1 instanceof ActionParameterDefaultsFacetViaMethod);
            final ActionParameterDefaultsFacetViaMethod actionDefaultFacetViaMethod1 = (ActionParameterDefaultsFacetViaMethod) facet1;
            assertMethodEqualsFirstIn(default1Method, actionDefaultFacetViaMethod1);
            assertMethodWasRemoved(default1Method);
        });
    }

    @Test
    void installsParameterChoicesMethodAndRemovesMethod() {
        var facetFactory = new ActionParameterChoicesFacetViaMethodFactory(getMetaModelContext());

        @SuppressWarnings("unused")
        class Customer {
            public void someAction(final int x, final long y, final long z) {}
            public Collection<Integer> choices0SomeAction() { return Collections.emptyList(); }
            public List<Long> choices1SomeAction() { return Collections.emptyList(); }
            public Set<Long> choices2SomeAction() { return Collections.emptySet(); }
        }

        final ResolvedMethod choices0Method = findMethodExactOrFail(Customer.class, "choices0SomeAction");
        final ResolvedMethod choices1Method = findMethodExactOrFail(Customer.class, "choices1SomeAction");
        final ResolvedMethod choices2Method = findMethodExactOrFail(Customer.class, "choices2SomeAction");

        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod) -> {
            //when
            facetFactory.process(processMethodContext);
            //then
            final Facet facet0 = facetedMethod.getParameters().getElseFail(0).getFacet(ActionParameterChoicesFacet.class);
            assertNotNull(facet0);
            assertTrue(facet0 instanceof ActionParameterChoicesFacetViaMethod);
            var actionChoicesFacetViaMethod0 = (ActionParameterChoicesFacetViaMethod) facet0;
            assertMethodEqualsFirstIn(choices0Method, actionChoicesFacetViaMethod0);
            assertMethodWasRemoved(choices0Method);

            final Facet facet1 = facetedMethod.getParameters().getElseFail(1).getFacet(ActionParameterChoicesFacet.class);
            assertNotNull(facet1);
            assertTrue(facet1 instanceof ActionParameterChoicesFacetViaMethod);
            var actionChoicesFacetViaMethod1 = (ActionParameterChoicesFacetViaMethod) facet1;
            assertMethodEqualsFirstIn(choices1Method, actionChoicesFacetViaMethod1);
            assertMethodWasRemoved(choices1Method);

            final Facet facet2 = facetedMethod.getParameters().getElseFail(2).getFacet(ActionParameterChoicesFacet.class);
            assertNotNull(facet2);
            assertTrue(facet2 instanceof ActionParameterChoicesFacetViaMethod);
            var actionChoicesFacetViaMethod2 = (ActionParameterChoicesFacetViaMethod) facet2;
            assertMethodEqualsFirstIn(choices2Method, actionChoicesFacetViaMethod2);
            assertMethodWasRemoved(choices2Method);
        });
    }

    @Test
    void installsParameterAutoCompleteMethodAndRemovesMethod() {
        var facetFactory = new ActionParameterAutoCompleteFacetViaMethodFactory(getMetaModelContext());

        @SuppressWarnings("unused")
        class Customer {
            public void someAction(final int x, final long y) {}
            public List<Integer> autoComplete0SomeAction(final String searchArg) { return Collections.emptyList();}
        }

        final ResolvedMethod autoComplete0Method = findMethodExactOrFail(Customer.class, "autoComplete0SomeAction", new Class[] {String.class});

        actionScenario(Customer.class, "someAction", (processMethodContext, facetHolder, facetedMethod) -> {
            //when
            facetFactory.process(processMethodContext);
            //then
            final Facet facet0 = facetedMethod.getParameters().getElseFail(0).getFacet(ActionParameterAutoCompleteFacet.class);
            assertNotNull(facet0);
            assertTrue(facet0 instanceof ActionParameterAutoCompleteFacetViaMethod);
            final ActionParameterAutoCompleteFacetViaMethod actionAutoCompleteFacetViaMethod0 = (ActionParameterAutoCompleteFacetViaMethod) facet0;
            assertMethodEqualsFirstIn(autoComplete0Method, actionAutoCompleteFacetViaMethod0);
            assertMethodWasRemoved(autoComplete0Method);
        });
    }

}
