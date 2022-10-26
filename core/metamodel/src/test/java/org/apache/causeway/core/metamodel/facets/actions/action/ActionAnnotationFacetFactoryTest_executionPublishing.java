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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.commons.internal.base._Blackhole;
import org.apache.causeway.core.config.metamodel.facets.ActionConfigOptions;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacetAbstract;
import org.apache.causeway.core.metamodel.facets.members.publish.execution.ExecutionPublishingActionFacetForActionAnnotation;
import org.apache.causeway.core.metamodel.facets.members.publish.execution.ExecutionPublishingActionFacetFromConfiguration;
import org.apache.causeway.core.metamodel.facets.members.publish.execution.ExecutionPublishingFacet;

import lombok.val;

class ActionAnnotationFacetFactoryTest_executionPublishing
extends ActionAnnotationFacetFactoryTest {

    private void processExecutionPublishing(
            final ActionAnnotationFacetFactory facetFactory, final ProcessMethodContext processMethodContext) {
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processExecutionPublishing(processMethodContext, actionIfAny);
    }

    @Test
    void given_HasInteractionId_thenIgnored() {

        final Method actionMethod = findMethod(SomeHasInteractionId.class, "someAction");

        processExecutionPublishing(facetFactory, ProcessMethodContext
                .forTesting(SomeHasInteractionId.class, null, actionMethod, mockMethodRemover, facetedMethod));

        assertFalse(ExecutionPublishingFacet.isPublishingEnabled(facetedMethod));

        expectNoMethodsRemoved();
    }

    @Test
    void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andSafeSemantics_thenNone() {

        // given
        allowingPublishingConfigurationToReturn(ActionConfigOptions.PublishingPolicy.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});

        // when
        processExecutionPublishing(facetFactory, ProcessMethodContext
                .forTesting(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

        // then
        assertFalse(ExecutionPublishingFacet.isPublishingEnabled(facetedMethod));
    }

    @Test
    void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andNonSafeSemantics_thenAdded() {

        // given
        allowingPublishingConfigurationToReturn(ActionConfigOptions.PublishingPolicy.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.IDEMPOTENT, facetedMethod) {});

        // when
        processExecutionPublishing(facetFactory, ProcessMethodContext
                .forTesting(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(ExecutionPublishingFacet.class);
        assertNotNull(facet);
        final ExecutionPublishingActionFacetFromConfiguration facetImpl = (ExecutionPublishingActionFacetFromConfiguration) facet;
        _Blackhole.consume(facetImpl);
    }

    @Test
    void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andNoSemantics_thenException() {

        // given
        allowingPublishingConfigurationToReturn(ActionConfigOptions.PublishingPolicy.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        // when
        assertThrows(IllegalStateException.class, ()->
        processExecutionPublishing(facetFactory, ProcessMethodContext
                .forTesting(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod)));

    }

    @Test
    void given_noAnnotation_and_configurationSetToNone_thenNone() {

        // given
        allowingPublishingConfigurationToReturn(ActionConfigOptions.PublishingPolicy.NONE);
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        // when
        processExecutionPublishing(facetFactory, ProcessMethodContext
                .forTesting(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

        // then
        assertFalse(ExecutionPublishingFacet.isPublishingEnabled(facetedMethod));

        expectNoMethodsRemoved();

    }

    @Test
    void given_noAnnotation_and_configurationSetToAll_thenFacetAdded() {

        // given
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        allowingPublishingConfigurationToReturn(ActionConfigOptions.PublishingPolicy.ALL);

        // when
        processExecutionPublishing(facetFactory, ProcessMethodContext
                .forTesting(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(ExecutionPublishingFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ExecutionPublishingActionFacetFromConfiguration);
    }

    @Test
    void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andSafeSemantics_thenNone() {

        class Customer {
            @Action(executionPublishing = org.apache.causeway.applib.annotation.Publishing.AS_CONFIGURED)
            public void someAction() {
            }
        }

        allowingPublishingConfigurationToReturn(ActionConfigOptions.PublishingPolicy.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});

        processExecutionPublishing(facetFactory, ProcessMethodContext
                .forTesting(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        assertFalse(ExecutionPublishingFacet.isPublishingEnabled(facetedMethod));

        expectNoMethodsRemoved();
    }

    @Test
    void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andNonSafeSemantics_thenAdded() {

        // given
        class Customer {
            @Action(
                    executionPublishing = org.apache.causeway.applib.annotation.Publishing.AS_CONFIGURED
                    )
            public void someAction() {
            }
        }

        allowingPublishingConfigurationToReturn(ActionConfigOptions.PublishingPolicy.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.IDEMPOTENT, facetedMethod) {});

        // when
        processExecutionPublishing(facetFactory, ProcessMethodContext
                .forTesting(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(ExecutionPublishingFacet.class);
        assertNotNull(facet);
        final ExecutionPublishingActionFacetForActionAnnotation facetImpl = (ExecutionPublishingActionFacetForActionAnnotation) facet;
        _Blackhole.consume(facetImpl);

        expectNoMethodsRemoved();
    }

    @Test
    void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andNoSemantics_thenException() {

        class Customer {
            @Action(executionPublishing = org.apache.causeway.applib.annotation.Publishing.AS_CONFIGURED)
            public void someAction() {
            }
        }

        allowingPublishingConfigurationToReturn(ActionConfigOptions.PublishingPolicy.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(Customer.class, "someAction");

        assertThrows(IllegalStateException.class, ()->
            processExecutionPublishing(facetFactory, ProcessMethodContext
                    .forTesting(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod)));
    }

    @Test
    void given_asConfigured_and_configurationSetToNone_thenNone() {

        class Customer {
            @Action(executionPublishing = org.apache.causeway.applib.annotation.Publishing.AS_CONFIGURED)
            public void someAction() {
            }
        }

        allowingPublishingConfigurationToReturn(ActionConfigOptions.PublishingPolicy.NONE);
        final Method actionMethod = findMethod(Customer.class, "someAction");

        processExecutionPublishing(facetFactory, ProcessMethodContext
                .forTesting(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        assertFalse(ExecutionPublishingFacet.isPublishingEnabled(facetedMethod));

        expectNoMethodsRemoved();

    }

    @Test
    void given_asConfigured_and_configurationSetToAll_thenFacetAdded() {

        // given
        class Customer {
            @Action(
                    executionPublishing = org.apache.causeway.applib.annotation.Publishing.AS_CONFIGURED
                    )
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        allowingPublishingConfigurationToReturn(ActionConfigOptions.PublishingPolicy.ALL);

        // when
        processExecutionPublishing(facetFactory, ProcessMethodContext
                .forTesting(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(ExecutionPublishingFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ExecutionPublishingActionFacetForActionAnnotation);

        expectNoMethodsRemoved();
    }

    @Test
    void given_enabled_irrespectiveOfConfiguration_thenFacetAdded() {

        // given
        class Customer {
            @Action(
                    executionPublishing = org.apache.causeway.applib.annotation.Publishing.ENABLED
                    )
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        // even though configuration is disabled
        allowingPublishingConfigurationToReturn(ActionConfigOptions.PublishingPolicy.NONE);

        // when
        processExecutionPublishing(facetFactory, ProcessMethodContext
                .forTesting(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(ExecutionPublishingFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ExecutionPublishingActionFacetForActionAnnotation);
    }

    @Test
    void given_disabled_irrespectiveOfConfiguration_thenNone() {

        // given
        class Customer {
            @Action(
                    executionPublishing = org.apache.causeway.applib.annotation.Publishing.DISABLED
                    )
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        // even though configuration is disabled
        allowingPublishingConfigurationToReturn(ActionConfigOptions.PublishingPolicy.NONE);

        // when
        processExecutionPublishing(facetFactory, ProcessMethodContext
                .forTesting(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        assertFalse(ExecutionPublishingFacet.isPublishingEnabled(facetedMethod));
    }

}
