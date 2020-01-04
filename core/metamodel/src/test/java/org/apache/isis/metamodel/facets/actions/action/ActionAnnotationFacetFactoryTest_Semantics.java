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
package org.apache.isis.metamodel.facets.actions.action;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.metamodel.facets.actions.semantics.ActionSemanticsFacet;

import lombok.val;

public class ActionAnnotationFacetFactoryTest_Semantics extends ActionAnnotationFacetFactoryTest {

    private void processSemantics(
            ActionAnnotationFacetFactory facetFactory, ProcessMethodContext processMethodContext) {
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processSemantics(processMethodContext, actionIfAny);
    }
    
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
        processSemantics(facetFactory, processMethodContext);

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
        processSemantics(facetFactory, processMethodContext);

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
        processSemantics(facetFactory, processMethodContext);

        // then
        final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
        Assert.assertNotNull(facet);
        assertThat(facet.value(), is(SemanticsOf.NON_IDEMPOTENT));
    }

}