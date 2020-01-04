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

import java.lang.reflect.Method;

import org.junit.Test;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.commons.internal.base._Blackhole;
import org.apache.isis.config.metamodel.facets.PublishActionsConfiguration;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.metamodel.facets.actions.action.publishing.PublishedActionFacetForActionAnnotation;
import org.apache.isis.metamodel.facets.actions.action.publishing.PublishedActionFacetFromConfiguration;
import org.apache.isis.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.metamodel.facets.actions.semantics.ActionSemanticsFacetAbstract;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import lombok.val;

public class ActionAnnotationFacetFactoryTest_Publishing extends ActionAnnotationFacetFactoryTest {

    private void processPublishing(
            ActionAnnotationFacetFactory facetFactory, ProcessMethodContext processMethodContext) {
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processPublishing(processMethodContext, actionIfAny);
    }
    
    @Test
    public void givenHasTransactionId_thenIgnored() {

        final Method actionMethod = findMethod(SomeTransactionalId.class, "someAction");

        processPublishing(facetFactory, new ProcessMethodContext(SomeTransactionalId.class, null, actionMethod, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNull(facet);

        expectNoMethodsRemoved();
    }

    @Test
    public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andSafeSemantics_thenNone() {

        // given
        allowingPublishingConfigurationToReturn(PublishActionsConfiguration.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});

        // when
        processPublishing(facetFactory, new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNull(facet);
    }

    @Test
    public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andNonSafeSemantics_thenAdded() {

        // given
        allowingPublishingConfigurationToReturn(PublishActionsConfiguration.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.IDEMPOTENT, facetedMethod) {});

        // when
        processPublishing(facetFactory, new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNotNull(facet);
        final PublishedActionFacetFromConfiguration facetImpl = (PublishedActionFacetFromConfiguration) facet;
        _Blackhole.consume(facetImpl);
    }

    @Test(expected=IllegalStateException.class)
    public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andNoSemantics_thenException() {

        // given
        allowingPublishingConfigurationToReturn(PublishActionsConfiguration.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        // when
        processPublishing(facetFactory, new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

    }

    @Test
    public void given_noAnnotation_and_configurationSetToNone_thenNone() {

        // given
        allowingPublishingConfigurationToReturn(PublishActionsConfiguration.NONE);
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        // when
        processPublishing(facetFactory, new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNull(facet);

        expectNoMethodsRemoved();

    }

    @Test
    public void given_noAnnotation_and_configurationSetToAll_thenFacetAdded() {

        // given
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        allowingPublishingConfigurationToReturn(PublishActionsConfiguration.ALL);

        // when
        processPublishing(facetFactory, new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PublishedActionFacetFromConfiguration);
    }

    @Test
    public void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andSafeSemantics_thenNone() {

        class Customer {
            @Action(publishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED)
            public void someAction() {
            }
        }

        allowingPublishingConfigurationToReturn(PublishActionsConfiguration.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});

        processPublishing(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNull(facet);

        expectNoMethodsRemoved();
    }

    @Test
    public void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andNonSafeSemantics_thenAdded() {

        // given
        class Customer {
            @Action(
                    publishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED
                    )
            public void someAction() {
            }
        }

        allowingPublishingConfigurationToReturn(PublishActionsConfiguration.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.IDEMPOTENT, facetedMethod) {});

        // when
        processPublishing(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNotNull(facet);
        final PublishedActionFacetForActionAnnotation facetImpl = (PublishedActionFacetForActionAnnotation) facet;
        _Blackhole.consume(facetImpl);

        expectNoMethodsRemoved();
    }

    @Test(expected=IllegalStateException.class)
    public void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andNoSemantics_thenException() {

        class Customer {
            @Action(publishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED)
            public void someAction() {
            }
        }

        allowingPublishingConfigurationToReturn(PublishActionsConfiguration.IGNORE_QUERY_ONLY);
        final Method actionMethod = findMethod(Customer.class, "someAction");

        processPublishing(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));
    }

    @Test
    public void given_asConfigured_and_configurationSetToNone_thenNone() {

        class Customer {
            @Action(publishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED)
            public void someAction() {
            }
        }

        allowingPublishingConfigurationToReturn(PublishActionsConfiguration.NONE);
        final Method actionMethod = findMethod(Customer.class, "someAction");

        processPublishing(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNull(facet);

        expectNoMethodsRemoved();

    }

    @Test
    public void given_asConfigured_and_configurationSetToAll_thenFacetAdded() {

        // given
        class Customer {
            @Action(
                    publishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED
                    )
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        allowingPublishingConfigurationToReturn(PublishActionsConfiguration.ALL);

        // when
        processPublishing(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PublishedActionFacetForActionAnnotation);

        expectNoMethodsRemoved();
    }

    @Test
    public void given_enabled_irrespectiveOfConfiguration_thenFacetAdded() {

        // given
        class Customer {
            @Action(
                    publishing = org.apache.isis.applib.annotation.Publishing.ENABLED
                    )
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        // even though configuration is disabled
        allowingPublishingConfigurationToReturn(PublishActionsConfiguration.NONE);

        // when
        processPublishing(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PublishedActionFacetForActionAnnotation);
    }

    @Test
    public void given_disabled_irrespectiveOfConfiguration_thenNone() {

        // given
        class Customer {
            @Action(
                    publishing = org.apache.isis.applib.annotation.Publishing.DISABLED
                    )
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        // even though configuration is disabled
        allowingPublishingConfigurationToReturn(PublishActionsConfiguration.NONE);

        // when
        processPublishing(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNull(facet);
    }

}