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

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.CommandReification;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actions.action.command.CommandFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;

import lombok.val;

public class ActionAnnotationFacetFactoryTest_Command extends ActionAnnotationFacetFactoryTest {

    private void processCommand(
            ActionAnnotationFacetFactory facetFactory, ProcessMethodContext processMethodContext) {
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processCommand(processMethodContext, actionIfAny);
    }
    
    @Test
    public void given_HasUniqueId_thenIgnored() {
        // given
        final Method actionMethod = findMethod(SomeHasUniqueId.class, "someAction");

        // when
        processCommand(facetFactory, new ProcessMethodContext(SomeHasUniqueId.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(CommandFacet.class);
        assertNull(facet);
    }

    @Test
    public void given_annotation_but_command_not_specified_then_facet_not_added() {

        // given
        class Customer {
            @Action()
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        // when
        processCommand(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(CommandFacet.class);
        assertNull(facet);
    }

    @Test
    public void given_annotation_with_command_enabled_then_facet_added() {

        // given
        class Customer {
            @Action(command = CommandReification.ENABLED)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        // when
        processCommand(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(CommandFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof CommandFacetForActionAnnotation);
    }


    @Test
    public void given_annotation_with_command_disabled_then_facet_not_added() {

        // given
        class Customer {
            @Action(command = CommandReification.DISABLED)
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        // when
        processCommand(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(CommandFacet.class);
        assertNull(facet);
    }



}