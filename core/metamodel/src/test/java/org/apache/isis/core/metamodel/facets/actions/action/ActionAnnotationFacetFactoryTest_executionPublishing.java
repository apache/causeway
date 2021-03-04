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
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.commons.internal.base._Blackhole;
import org.apache.isis.core.config.metamodel.facets.PublishingPolicies;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacetAbstract;
import org.apache.isis.core.metamodel.facets.members.publish.execution.ExecutionPublishingActionFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.members.publish.execution.ExecutionPublishingActionFacetFromConfiguration;
import org.apache.isis.core.metamodel.facets.members.publish.execution.ExecutionPublishingFacet;

import lombok.val;

public class ActionAnnotationFacetFactoryTest_executionPublishing extends ActionAnnotationFacetFactoryTest {

    private void processExecutionPublishing(
            ActionAnnotationFacetFactory facetFactory, ProcessMethodContext processMethodContext) {
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processExecutionPublishing(processMethodContext, actionIfAny);
    }

    @Test
    public void given_HasUniqueId_thenIgnored() {

        final Method actionMethod = findMethod(SomeHasInteractionId.class, "someAction");

        processExecutionPublishing(facetFactory, new ProcessMethodContext(SomeHasInteractionId.class, null, actionMethod, mockMethodRemover, facetedMethod));

        assertFalse(ExecutionPublishingFacet.isPublishingEnabled(facetedMethod));

        expectNoMethodsRemoved();
    }

    @Test
    public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andSafeSemantics_thenNone() {

        // given
        allowingPublishingConfigurationToReturn(PublishingPolicies.ActionPublishingPolicy.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});

        // when
        processExecutionPublishing(facetFactory, new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

        // then
        assertFalse(ExecutionPublishingFacet.isPublishingEnabled(facetedMethod));
    }

    @Test
    public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andNonSafeSemantics_thenAdded() {

        // given
        allowingPublishingConfigurationToReturn(PublishingPolicies.ActionPublishingPolicy.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.IDEMPOTENT, facetedMethod) {});

        // when
        processExecutionPublishing(facetFactory, new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(ExecutionPublishingFacet.class);
        assertNotNull(facet);
        final ExecutionPublishingActionFacetFromConfiguration facetImpl = (ExecutionPublishingActionFacetFromConfiguration) facet;
        _Blackhole.consume(facetImpl);
    }

    @Test(expected=IllegalStateException.class)
    public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andNoSemantics_thenException() {

        // given
        allowingPublishingConfigurationToReturn(PublishingPolicies.ActionPublishingPolicy.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        // when
        processExecutionPublishing(facetFactory, new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

    }

    @Test
    public void given_noAnnotation_and_configurationSetToNone_thenNone() {

        // given
        allowingPublishingConfigurationToReturn(PublishingPolicies.ActionPublishingPolicy.NONE);
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        // when
        processExecutionPublishing(facetFactory, new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

        // then
        assertFalse(ExecutionPublishingFacet.isPublishingEnabled(facetedMethod));

        expectNoMethodsRemoved();

    }

    @Test
    public void given_noAnnotation_and_configurationSetToAll_thenFacetAdded() {

        // given
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        allowingPublishingConfigurationToReturn(PublishingPolicies.ActionPublishingPolicy.ALL);

        // when
        processExecutionPublishing(facetFactory, new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(ExecutionPublishingFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ExecutionPublishingActionFacetFromConfiguration);
    }

    @Test
    public void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andSafeSemantics_thenNone() {

        class Customer {
            @Action(executionPublishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED)
            public void someAction() {
            }
        }

        allowingPublishingConfigurationToReturn(PublishingPolicies.ActionPublishingPolicy.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});

        processExecutionPublishing(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        assertFalse(ExecutionPublishingFacet.isPublishingEnabled(facetedMethod));

        expectNoMethodsRemoved();
    }

    @Test
    public void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andNonSafeSemantics_thenAdded() {

        // given
        class Customer {
            @Action(
                    executionPublishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED
                    )
            public void someAction() {
            }
        }

        allowingPublishingConfigurationToReturn(PublishingPolicies.ActionPublishingPolicy.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.IDEMPOTENT, facetedMethod) {});

        // when
        processExecutionPublishing(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(ExecutionPublishingFacet.class);
        assertNotNull(facet);
        final ExecutionPublishingActionFacetForActionAnnotation facetImpl = (ExecutionPublishingActionFacetForActionAnnotation) facet;
        _Blackhole.consume(facetImpl);

        expectNoMethodsRemoved();
    }

    @Test(expected=IllegalStateException.class)
    public void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andNoSemantics_thenException() {

        class Customer {
            @Action(executionPublishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED)
            public void someAction() {
            }
        }

        allowingPublishingConfigurationToReturn(PublishingPolicies.ActionPublishingPolicy.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(Customer.class, "someAction");

        processExecutionPublishing(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));
    }

    @Test
    public void given_asConfigured_and_configurationSetToNone_thenNone() {

        class Customer {
            @Action(executionPublishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED)
            public void someAction() {
            }
        }

        allowingPublishingConfigurationToReturn(PublishingPolicies.ActionPublishingPolicy.NONE);
        final Method actionMethod = findMethod(Customer.class, "someAction");

        processExecutionPublishing(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        assertFalse(ExecutionPublishingFacet.isPublishingEnabled(facetedMethod));

        expectNoMethodsRemoved();

    }

    @Test
    public void given_asConfigured_and_configurationSetToAll_thenFacetAdded() {

        // given
        class Customer {
            @Action(
                    executionPublishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED
                    )
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        allowingPublishingConfigurationToReturn(PublishingPolicies.ActionPublishingPolicy.ALL);

        // when
        processExecutionPublishing(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(ExecutionPublishingFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ExecutionPublishingActionFacetForActionAnnotation);

        expectNoMethodsRemoved();
    }

    @Test
    public void given_enabled_irrespectiveOfConfiguration_thenFacetAdded() {

        // given
        class Customer {
            @Action(
                    executionPublishing = org.apache.isis.applib.annotation.Publishing.ENABLED
                    )
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        // even though configuration is disabled
        allowingPublishingConfigurationToReturn(PublishingPolicies.ActionPublishingPolicy.NONE);

        // when
        processExecutionPublishing(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(ExecutionPublishingFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ExecutionPublishingActionFacetForActionAnnotation);
    }

    @Test
    public void given_disabled_irrespectiveOfConfiguration_thenNone() {

        // given
        class Customer {
            @Action(
                    executionPublishing = org.apache.isis.applib.annotation.Publishing.DISABLED
                    )
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        // even though configuration is disabled
        allowingPublishingConfigurationToReturn(PublishingPolicies.ActionPublishingPolicy.NONE);

        // when
        processExecutionPublishing(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        assertFalse(ExecutionPublishingFacet.isPublishingEnabled(facetedMethod));
    }

}
