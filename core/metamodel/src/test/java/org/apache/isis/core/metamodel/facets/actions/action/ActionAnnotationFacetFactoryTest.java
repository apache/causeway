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
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionInteraction;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.CommandExecuteIn;
import org.apache.isis.applib.annotation.CommandPersistence;
import org.apache.isis.applib.annotation.CommandReification;
import org.apache.isis.applib.annotation.Idempotent;
import org.apache.isis.applib.annotation.InvokeOn;
import org.apache.isis.applib.annotation.PostsActionInvokedEvent;
import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.PublishingPayloadFactoryForAction;
import org.apache.isis.applib.annotation.QueryOnly;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.eventbus.ActionDomainEvent;
import org.apache.isis.applib.services.eventbus.ActionInteractionEvent;
import org.apache.isis.applib.services.eventbus.ActionInvokedEvent;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromArray;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromGenerics;
import org.apache.isis.core.metamodel.facets.actions.action.command.CommandFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.command.CommandFacetForActionAnnotationAsConfigured;
import org.apache.isis.core.metamodel.facets.actions.action.command.CommandFacetForCommandAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.command.CommandFacetFromConfiguration;
import org.apache.isis.core.metamodel.facets.actions.action.hidden.HiddenFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetForActionInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromActionInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForPostsActionInvokedEventAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.publishing.PublishedActionFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.publishing.PublishedActionFacetForPublishedActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.publishing.PublishedActionFacetFromConfiguration;
import org.apache.isis.core.metamodel.facets.actions.action.typeof.TypeOfFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.typeof.TypeOfFacetOnActionForTypeOfAnnotation;
import org.apache.isis.core.metamodel.facets.actions.bulk.BulkFacet;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.core.metamodel.facets.actions.prototype.PrototypeFacet;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionPayloadFactoryDefault;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacetAbstract;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import static org.apache.isis.core.commons.matchers.IsisMatchers.classEqualTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ActionAnnotationFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    ActionAnnotationFacetFactory facetFactory;
    Method actionMethod;

    @Mock
    ObjectSpecification mockTypeSpec;
    @Mock
    ObjectSpecification mockReturnTypeSpec;

    void expectRemoveMethod(final Method actionMethod) {
        context.checking(new Expectations() {{
            oneOf(mockMethodRemover).removeMethod(actionMethod);
        }});
    }

    void allowingLoadSpecificationRequestsFor(final Class<?> cls, final Class<?> returnType) {
        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).loadSpecification(cls);
            will(returnValue(mockTypeSpec));

            allowing(mockSpecificationLoader).loadSpecification(returnType);
            will(returnValue(mockReturnTypeSpec));
        }});
    }

    @Before
    public void setUp() throws Exception {
        facetFactory = new ActionAnnotationFacetFactory();

        context.checking(new Expectations() {{
            allowing(mockServicesInjector).lookupService(AuthenticationSessionProvider.class);
            will(returnValue(mockAuthenticationSessionProvider));

            allowing(mockServicesInjector).getConfigurationServiceInternal();
            will(returnValue(mockConfiguration));

            allowing(mockServicesInjector).lookupService(DeploymentCategoryProvider.class);
            will(returnValue(mockDeploymentCategoryProvider));

            allowing(mockDeploymentCategoryProvider).getDeploymentCategory();
            will(returnValue(DeploymentCategory.PRODUCTION));

        }});

        actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.setServicesInjector(mockServicesInjector);
    }

    @After
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    class Customer {
        public void someAction() {
        }
    }

    class SomeTransactionalId implements HasTransactionId {
        public void someAction() {
        }

        @Override
        public UUID getTransactionId() {
            return null;
        }

        @Override
        public void setTransactionId(final UUID transactionId) {
        }
    }

    public static class Invocation extends ActionAnnotationFacetFactoryTest {

        @Test
        public void withPostsActionInvokedEvent() {

            class Customer {

                class SomeActionInvokedDomainEvent extends ActionInvokedEvent<Customer> {
                    public SomeActionInvokedDomainEvent(
                            final Customer source,
                            final Identifier identifier,
                            final Object... arguments) {
                        super(source, identifier, arguments);
                    }
                }

                @PostsActionInvokedEvent(SomeActionInvokedDomainEvent.class)
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // expect
            allowingLoadSpecificationRequestsFor(cls, actionMethod.getReturnType());
            expectRemoveMethod(actionMethod);

            context.checking(new Expectations() {{
                allowing(mockConfiguration).getBoolean("isis.reflector.facet.actionAnnotation.domainEvent.postForDefault", true);
                will(returnValue(true));
            }});


            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processInvocation(processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof ActionDomainEventFacetDefault);
            final ActionDomainEventFacetDefault domainEventFacetImpl = (ActionDomainEventFacetDefault) domainEventFacet;
            assertThat(domainEventFacetImpl.getEventType(), classEqualTo(ActionDomainEvent.Default.class)); // this is discarded at runtime, see ActionInvocationFacetForPostsActionInvokedEventAnnotation#verify(...)

            final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
            Assert.assertNotNull(invocationFacet);
            Assert.assertTrue(invocationFacet instanceof ActionInvocationFacetForPostsActionInvokedEventAnnotation);
            final ActionInvocationFacetForPostsActionInvokedEventAnnotation invocationFacetImpl = (ActionInvocationFacetForPostsActionInvokedEventAnnotation) invocationFacet;
            assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
        }

        @Test
        public void withActionInteractionEvent() {

            class Customer {

                class SomeActionInvokedDomainEvent extends ActionInteractionEvent<Customer> {
                    public SomeActionInvokedDomainEvent(
                            final Customer source,
                            final Identifier identifier,
                            final Object... arguments) {
                        super(source, identifier, arguments);
                    }
                }

                @ActionInteraction(SomeActionInvokedDomainEvent.class)
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // expect
            allowingLoadSpecificationRequestsFor(cls, actionMethod.getReturnType());
            expectRemoveMethod(actionMethod);

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processInvocation(processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof ActionDomainEventFacetForActionInteractionAnnotation);
            final ActionDomainEventFacetForActionInteractionAnnotation domainEventFacetImpl = (ActionDomainEventFacetForActionInteractionAnnotation) domainEventFacet;
            assertThat(domainEventFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));

            final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
            Assert.assertNotNull(invocationFacet);
            Assert.assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromActionInteractionAnnotation);
            final ActionInvocationFacetForDomainEventFromActionInteractionAnnotation invocationFacetImpl = (ActionInvocationFacetForDomainEventFromActionInteractionAnnotation) invocationFacet;
            assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
        }

        @Test
        public void withActionDomainEvent() {

            class Customer {

                class SomeActionInvokedDomainEvent extends ActionDomainEvent<Customer> {
                    public SomeActionInvokedDomainEvent(
                            final Customer source,
                            final Identifier identifier,
                            final Object... arguments) {
                        super(source, identifier, arguments);
                    }
                }

                @Action(domainEvent= SomeActionInvokedDomainEvent.class)
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // expect
            allowingLoadSpecificationRequestsFor(cls, actionMethod.getReturnType());
            expectRemoveMethod(actionMethod);

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processInvocation(processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof ActionDomainEventFacetForActionAnnotation);
            final ActionDomainEventFacetForActionAnnotation domainEventFacetImpl = (ActionDomainEventFacetForActionAnnotation) domainEventFacet;
            assertThat(domainEventFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));

            final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
            Assert.assertNotNull(invocationFacet);
            Assert.assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromActionAnnotation);
            final ActionInvocationFacetForDomainEventFromActionAnnotation invocationFacetImpl = (ActionInvocationFacetForDomainEventFromActionAnnotation) invocationFacet;
            assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
        }

        @Test
        public void withDefaultEvent() {

            class Customer {
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // expect
            allowingLoadSpecificationRequestsFor(cls, actionMethod.getReturnType());
            expectRemoveMethod(actionMethod);

            context.checking(new Expectations() {{
                allowing(mockConfiguration).getBoolean("isis.reflector.facet.actionAnnotation.domainEvent.postForDefault", true);
                will(returnValue(true));
            }});


            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processInvocation(processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof ActionDomainEventFacetDefault);
            final ActionDomainEventFacetDefault domainEventFacetImpl = (ActionDomainEventFacetDefault) domainEventFacet;
            assertThat(domainEventFacetImpl.getEventType(), classEqualTo(ActionDomainEvent.Default.class));

            final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
            Assert.assertNotNull(invocationFacet);
            Assert.assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromDefault);
            final ActionInvocationFacetForDomainEventFromDefault invocationFacetImpl = (ActionInvocationFacetForDomainEventFromDefault) invocationFacet;
            assertThat(invocationFacetImpl.getEventType(), classEqualTo(ActionDomainEvent.Default.class));
        }
    }

    public static class Hidden extends ActionAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            class Customer {
                @Action(hidden = Where.REFERENCES_PARENT)
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processHidden(processMethodContext);

            // then
            final HiddenFacet hiddenFacet = facetedMethod.getFacet(HiddenFacet.class);
            Assert.assertNotNull(hiddenFacet);
            assertThat(hiddenFacet.where(), is(Where.REFERENCES_PARENT));

            final Facet hiddenFacetImpl = facetedMethod.getFacet(HiddenFacetForActionAnnotation.class);
            Assert.assertNotNull(hiddenFacetImpl);
            Assert.assertTrue(hiddenFacet == hiddenFacetImpl);
        }

    }

    public static class RestrictTo extends ActionAnnotationFacetFactoryTest {

        @Test
        public void whenRestrictedToPrototyping() {

            class Customer {
                @Action(restrictTo = org.apache.isis.applib.annotation.RestrictTo.PROTOTYPING)
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processRestrictTo(processMethodContext);

            // then
            final PrototypeFacet facet = facetedMethod.getFacet(PrototypeFacet.class);
            Assert.assertNotNull(facet);
        }

        @Test
        public void whenRestrictedToNoRestriction() {

            class Customer {
                @Action(restrictTo = org.apache.isis.applib.annotation.RestrictTo.NO_RESTRICTIONS)
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processRestrictTo(processMethodContext);

            // then
            final PrototypeFacet facet = facetedMethod.getFacet(PrototypeFacet.class);
            assertNull(facet);
        }

        @Test
        public void whenNotPresent() {

            class Customer {
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processRestrictTo(processMethodContext);

            // then
            final PrototypeFacet facet = facetedMethod.getFacet(PrototypeFacet.class);
            assertNull(facet);
        }

    }

    public static class Semantics extends ActionAnnotationFacetFactoryTest {

        @Test
        public void whenDeprecatedQueryOnly() {

            class Customer {
                @QueryOnly
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processSemantics(processMethodContext);

            // then
            final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
            Assert.assertNotNull(facet);
            assertThat(facet.value(), is(Of.SAFE));
        }

        @Test
        public void whenDeprecatedIdempotent() {

            class Customer {
                @Idempotent
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processSemantics(processMethodContext);

            // then
            final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
            Assert.assertNotNull(facet);
            assertThat(facet.value(), is(Of.IDEMPOTENT));
        }

        @Test
        public void whenDeprecatedAnnotationSafe() {

            class Customer {
                @ActionSemantics(Of.SAFE)
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processSemantics(processMethodContext);

            // then
            final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
            Assert.assertNotNull(facet);
            assertThat(facet.value(), is(Of.SAFE));
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
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processSemantics(processMethodContext);

            // then
            final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
            Assert.assertNotNull(facet);
            assertThat(facet.value(), is(Of.SAFE));
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
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processSemantics(processMethodContext);

            // then
            final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
            Assert.assertNotNull(facet);
            assertThat(facet.value(), is(Of.NON_IDEMPOTENT));
        }

        @Test
        public void whenNoAnnotation() {

            class Customer {
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processSemantics(processMethodContext);

            // then
            final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
            Assert.assertNotNull(facet);
            assertThat(facet.value(), is(Of.NON_IDEMPOTENT));
        }


        @Test
        public void whenDeprecatedActionSemanticsAndAction() {

            class Customer {
                @ActionSemantics(Of.SAFE)
                @Action(semantics = SemanticsOf.IDEMPOTENT)
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processSemantics(processMethodContext);

            // then
            final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
            Assert.assertNotNull(facet);
            assertThat(facet.value(), is(Of.SAFE));
        }

        @Test
        public void whenDeprecatedQueryOnlyAndDeprecatedActionSemantics() {

            class Customer {
                @QueryOnly
                @ActionSemantics(Of.IDEMPOTENT)
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processSemantics(processMethodContext);

            // then
            final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
            Assert.assertNotNull(facet);
            assertThat(facet.value(), is(Of.SAFE));
        }

        @Test
        public void whenDeprecatedIdempotentAndDeprecatedActionSemantics() {

            class Customer {
                @Idempotent
                @ActionSemantics(Of.SAFE)
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processSemantics(processMethodContext);

            // then
            final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
            Assert.assertNotNull(facet);
            assertThat(facet.value(), is(Of.IDEMPOTENT));
        }


    }

    public static class Bulk extends ActionAnnotationFacetFactoryTest {

        @Test
        public void whenDeprecatedBulkAnnotation() {

            class Customer {
                @org.apache.isis.applib.annotation.Bulk()
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processBulk(processMethodContext);

            // then
            final BulkFacet facet = facetedMethod.getFacet(BulkFacet.class);
            Assert.assertNotNull(facet);
            assertThat(facet.value(), is(org.apache.isis.applib.annotation.Bulk.AppliesTo.BULK_AND_REGULAR));

        }

        @Test
        public void whenDeprecatedBulkAnnotationAppliesToBulkOnly() {

            class Customer {
                @org.apache.isis.applib.annotation.Bulk(org.apache.isis.applib.annotation.Bulk.AppliesTo.BULK_ONLY)
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processBulk(processMethodContext);

            // then
            final BulkFacet facet = facetedMethod.getFacet(BulkFacet.class);
            Assert.assertNotNull(facet);
            assertThat(facet.value(), is(org.apache.isis.applib.annotation.Bulk.AppliesTo.BULK_ONLY));
        }

        @Test
        public void whenObjectOnly() {

            class Customer {
                @Action(invokeOn = InvokeOn.OBJECT_ONLY)
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processBulk(processMethodContext);

            // then
            final BulkFacet facet = facetedMethod.getFacet(BulkFacet.class);
            assertNull(facet);

        }

        @Test
        public void whenCollectionOnly() {

            class Customer {
                @Action(invokeOn = InvokeOn.COLLECTION_ONLY)
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processBulk(processMethodContext);

            // then
            final BulkFacet facet = facetedMethod.getFacet(BulkFacet.class);
            Assert.assertNotNull(facet);
            assertThat(facet.value(), is(org.apache.isis.applib.annotation.Bulk.AppliesTo.BULK_ONLY));

        }

        @Test
        public void whenObjectAndCollection() {

            class Customer {
                @Action(invokeOn = InvokeOn.OBJECT_AND_COLLECTION)
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processBulk(processMethodContext);

            // then
            final BulkFacet facet = facetedMethod.getFacet(BulkFacet.class);
            Assert.assertNotNull(facet);
            assertThat(facet.value(), is(org.apache.isis.applib.annotation.Bulk.AppliesTo.BULK_AND_REGULAR));

        }

    }

    public static class Command extends ActionAnnotationFacetFactoryTest {

        @Test
        public void givenHasTransactionId_thenIgnored() {
            // given
            final Method actionMethod = findMethod(SomeTransactionalId.class, "someAction");

            // when
            facetFactory.processCommand(new ProcessMethodContext(SomeTransactionalId.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNull(facet);

        }

        @Test
        public void givenDeprecatedAnnotation() {
            // given
            class Customer {
                @org.apache.isis.applib.annotation.Command(
                        executeIn = org.apache.isis.applib.annotation.Command.ExecuteIn.BACKGROUND,
                        persistence = org.apache.isis.applib.annotation.Command.Persistence.IF_HINTED
                )
                public void someAction() {
                }
            }
            final Method actionMethod = findMethod(Customer.class, "someAction");

            // when
            facetFactory.processCommand(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof CommandFacetForCommandAnnotation);
            final CommandFacetForCommandAnnotation facetImpl = (CommandFacetForCommandAnnotation) facet;

            assertThat(facetImpl.persistence(), is(org.apache.isis.applib.annotation.Command.Persistence.IF_HINTED));

            expectNoMethodsRemoved();
        }

        @Test
        public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andSafeSemantics_thenNone() {

            // given
            allowingCommandConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            facetedMethod.addFacet(new ActionSemanticsFacetAbstract(Of.SAFE, facetedMethod) {});

            // when
            facetFactory.processCommand(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNull(facet);
        }

        @Test
        public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andNonSafeSemantics_thenAdded() {

            // given
            allowingCommandConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            facetedMethod.addFacet(new ActionSemanticsFacetAbstract(Of.IDEMPOTENT, facetedMethod) {});

            // when
            facetFactory.processCommand(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNotNull(facet);
            assert(facet instanceof  CommandFacetFromConfiguration);
            final CommandFacetFromConfiguration facetImpl = (CommandFacetFromConfiguration) facet;
            assertThat(facetImpl.persistence(), is(org.apache.isis.applib.annotation.Command.Persistence.PERSISTED));
            assertThat(facetImpl.executeIn(), is(org.apache.isis.applib.annotation.Command.ExecuteIn.FOREGROUND));
        }

        @Test(expected=IllegalStateException.class)
        public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andNoSemantics_thenException() {

            // given
            allowingCommandConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            // when
            facetFactory.processCommand(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));
        }

        @Test
        public void given_noAnnotation_and_configurationSetToNone_thenNone() {

            // given
            allowingCommandConfigurationToReturn("none");
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            // when
            facetFactory.processCommand(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
            assertNull(facet);
        }

        @Test
        public void given_noAnnotation_and_configurationSetToAll_thenFacetAdded() {

            // given
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            allowingCommandConfigurationToReturn("all");

            // when
            facetFactory.processCommand(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNotNull(facet);
            assert(facet instanceof CommandFacetFromConfiguration);
        }

        @Test
        public void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andSafeSemantics_thenNone() {

            class Customer {
                @Action(
                        command = CommandReification.AS_CONFIGURED
                )
                public void someAction() {
                }
            }

            allowingCommandConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(Customer.class, "someAction");

            facetedMethod.addFacet(new ActionSemanticsFacetAbstract(Of.SAFE, facetedMethod) {});

            facetFactory.processCommand(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNull(facet);
        }

        @Test
        public void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andNonSafeSemantics_thenAdded() {

            // given
            class Customer {
                @Action(
                        command = CommandReification.AS_CONFIGURED,
                        commandPersistence = CommandPersistence.IF_HINTED,
                        commandExecuteIn = CommandExecuteIn.BACKGROUND
                )
                public void someAction() {
                }
            }

            allowingCommandConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(Customer.class, "someAction");

            facetedMethod.addFacet(new ActionSemanticsFacetAbstract(Of.IDEMPOTENT, facetedMethod) {});

            // when
            facetFactory.processCommand(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNotNull(facet);
            final CommandFacetForActionAnnotationAsConfigured facetImpl = (CommandFacetForActionAnnotationAsConfigured) facet;
            assertThat(facetImpl.persistence(), is(org.apache.isis.applib.annotation.Command.Persistence.IF_HINTED));
            assertThat(facetImpl.executeIn(), is(org.apache.isis.applib.annotation.Command.ExecuteIn.BACKGROUND));
        }

        @Test(expected=IllegalStateException.class)
        public void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andNoSemantics_thenException() {

            class Customer {
                @Action(
                        command = CommandReification.AS_CONFIGURED
                )
                public void someAction() {
                }
            }

            allowingCommandConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(Customer.class, "someAction");

            facetFactory.processCommand(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));
        }

        @Test
        public void given_asConfigured_and_configurationSetToNone_thenNone() {

            class Customer {
                @Action(
                        command = CommandReification.AS_CONFIGURED
                )
                public void someAction() {
                }
            }

            allowingCommandConfigurationToReturn("none");
            final Method actionMethod = findMethod(Customer.class, "someAction");

            facetFactory.processCommand(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNull(facet);
        }

        @Test
        public void given_asConfigured_and_configurationSetToAll_thenFacetAdded() {

            // given
            class Customer {
                @Action(
                        command = CommandReification.AS_CONFIGURED,
                        commandPersistence = CommandPersistence.IF_HINTED,
                        commandExecuteIn = CommandExecuteIn.BACKGROUND
                )
                public void someAction() {
                }
            }
            final Method actionMethod = findMethod(Customer.class, "someAction");

            allowingCommandConfigurationToReturn("all");

            // when
            facetFactory.processCommand(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNotNull(facet);
            final CommandFacetForActionAnnotationAsConfigured facetImpl = (CommandFacetForActionAnnotationAsConfigured) facet;
            assertThat(facetImpl.persistence(), is(org.apache.isis.applib.annotation.Command.Persistence.IF_HINTED));
            assertThat(facetImpl.executeIn(), is(org.apache.isis.applib.annotation.Command.ExecuteIn.BACKGROUND));
        }

        @Test
        public void given_enabled_irrespectiveOfConfiguration_thenFacetAdded() {

            // given
            class Customer {
                @Action(
                        command = CommandReification.ENABLED
                )
                public void someAction() {
                }
            }
            final Method actionMethod = findMethod(Customer.class, "someAction");

            // even though configuration is disabled
            allowingCommandConfigurationToReturn("none");

            // when
            facetFactory.processCommand(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof CommandFacetForActionAnnotation);
        }

        @Test
        public void given_disabled_irrespectiveOfConfiguration_thenNone() {

            // given
            class Customer {
                @Action(
                        command = CommandReification.DISABLED
                )
                public void someAction() {
                }
            }
            final Method actionMethod = findMethod(Customer.class, "someAction");

            // even though configuration is disabled
            allowingCommandConfigurationToReturn("none");

            // when
            facetFactory.processCommand(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNull(facet);
        }


    }

    public static class Publishing extends ActionAnnotationFacetFactoryTest {

        public static class CustomerSomeActionPayloadFactory implements PublishedAction.PayloadFactory {
            @Override
            public EventPayload payloadFor(final Identifier actionIdentifier, final Object target, final List<Object> arguments, final Object result) {
                return null;
            }
        }

        public static class CustomerSomeActionPublishingPayloadFactory implements PublishingPayloadFactoryForAction {
            @Override
            public EventPayload payloadFor(final Identifier actionIdentifier, final Object target, final List<Object> arguments, final Object result) {
                return null;
            }
        }

        @Test
        public void givenHasTransactionId_thenIgnored() {

            final Method actionMethod = findMethod(SomeTransactionalId.class, "someAction");

            facetFactory.processPublishing(new ProcessMethodContext(SomeTransactionalId.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
            assertNull(facet);

            expectNoMethodsRemoved();
        }

        @Test
        public void givenDeprecatedAnnotation() {

            // given
            class Customer {
                @org.apache.isis.applib.annotation.PublishedAction(CustomerSomeActionPayloadFactory.class)
                public void someAction() {
                }
            }
            final Method actionMethod = findMethod(Customer.class, "someAction");

            // when
            facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PublishedActionFacetForPublishedActionAnnotation);
            final PublishedActionFacetForPublishedActionAnnotation facetImpl = (PublishedActionFacetForPublishedActionAnnotation) facet;
            assertThat(facetImpl.value(), instanceOf(CustomerSomeActionPayloadFactory.class));

            expectNoMethodsRemoved();
        }

        @Test
        public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andSafeSemantics_thenNone() {

            // given
            allowingPublishingConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            facetedMethod.addFacet(new ActionSemanticsFacetAbstract(Of.SAFE, facetedMethod) {});

            // when
            facetFactory.processPublishing(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
            assertNull(facet);
        }

        @Test
        public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andNonSafeSemantics_thenAdded() {

            // given
            allowingPublishingConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            facetedMethod.addFacet(new ActionSemanticsFacetAbstract(Of.IDEMPOTENT, facetedMethod) {});

            // when
            facetFactory.processPublishing(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
            assertNotNull(facet);
            final PublishedActionFacetFromConfiguration facetImpl = (PublishedActionFacetFromConfiguration) facet;
            final PublishedAction.PayloadFactory payloadFactory = facetImpl.value();
            assertThat(payloadFactory, is(instanceOf(PublishedActionPayloadFactoryDefault.class)));
        }

        @Test(expected=IllegalStateException.class)
        public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andNoSemantics_thenException() {

            // given
            allowingPublishingConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            // when
            facetFactory.processPublishing(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

        }

        @Test
        public void given_noAnnotation_and_configurationSetToNone_thenNone() {

            // given
            allowingPublishingConfigurationToReturn("none");
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            // when
            facetFactory.processPublishing(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
            assertNull(facet);

            expectNoMethodsRemoved();

        }

        @Test
        public void given_noAnnotation_and_configurationSetToAll_thenFacetAdded() {

            // given
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            allowingPublishingConfigurationToReturn("all");

            // when
            facetFactory.processPublishing(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

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

            allowingPublishingConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(Customer.class, "someAction");

            facetedMethod.addFacet(new ActionSemanticsFacetAbstract(Of.SAFE, facetedMethod) {});

            facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
            assertNull(facet);

            expectNoMethodsRemoved();
        }

        @Test
        public void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andNonSafeSemantics_thenAdded() {

            // given
            class Customer {
                @Action(
                        publishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED,
                        publishingPayloadFactory = CustomerSomeActionPublishingPayloadFactory.class
                )
                public void someAction() {
                }
            }

            allowingPublishingConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(Customer.class, "someAction");

            facetedMethod.addFacet(new ActionSemanticsFacetAbstract(Of.IDEMPOTENT, facetedMethod) {});

            // when
            facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
            assertNotNull(facet);
            final PublishedActionFacetForActionAnnotation facetImpl = (PublishedActionFacetForActionAnnotation) facet;
            final PublishedAction.PayloadFactory payloadFactory = facetImpl.value();
            assertThat(payloadFactory, instanceOf(PublishedActionPayloadFactoryDefault.class));

            final PublishedActionPayloadFactoryDefault legacyAdapter = (PublishedActionPayloadFactoryDefault) payloadFactory;
            assertThat(legacyAdapter.getPayloadFactory(), instanceOf(CustomerSomeActionPublishingPayloadFactory.class));

            expectNoMethodsRemoved();
        }

        @Test(expected=IllegalStateException.class)
        public void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andNoSemantics_thenException() {

            class Customer {
                @Action(publishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED)
                public void someAction() {
                }
            }

            allowingPublishingConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(Customer.class, "someAction");

            facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));
        }

        @Test
        public void given_asConfigured_and_configurationSetToNone_thenNone() {

            class Customer {
                @Action(publishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED)
                public void someAction() {
                }
            }

            allowingPublishingConfigurationToReturn("none");
            final Method actionMethod = findMethod(Customer.class, "someAction");

            facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
            assertNull(facet);

            expectNoMethodsRemoved();

        }

        @Test
        public void given_asConfigured_and_configurationSetToAll_thenFacetAdded() {

            // given
            class Customer {
                @Action(
                        publishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED,
                        publishingPayloadFactory = CustomerSomeActionPublishingPayloadFactory.class
                )
                public void someAction() {
                }
            }
            final Method actionMethod = findMethod(Customer.class, "someAction");

            allowingPublishingConfigurationToReturn("all");

            // when
            facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
            assertNotNull(facet);
            assertTrue(facet instanceof PublishedActionFacetForActionAnnotation);

            final PublishedActionFacetForActionAnnotation facetImpl = (PublishedActionFacetForActionAnnotation) facet;
            final PublishedAction.PayloadFactory payloadFactory = facetImpl.value();
            assertThat(payloadFactory, instanceOf(PublishedActionPayloadFactoryDefault.class));

            final PublishedActionPayloadFactoryDefault legacyAdapter = (PublishedActionPayloadFactoryDefault) payloadFactory;
            assertThat(legacyAdapter.getPayloadFactory(), instanceOf(CustomerSomeActionPublishingPayloadFactory.class));

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
            allowingPublishingConfigurationToReturn("none");

            // when
            facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

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
            allowingPublishingConfigurationToReturn("none");

            // when
            facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
            assertNull(facet);
        }

    }

    public static class TypeOf extends ActionAnnotationFacetFactoryTest {

        @Test
        public void whenDeprecatedTypeOfAnnotationOnActionReturningCollection() {

            class Order {
            }
            class Customer {
                @org.apache.isis.applib.annotation.TypeOf(Order.class)
                public Collection someAction() {
                    return null;
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processTypeOf(processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNotNull(facet);
            Assert.assertTrue(facet instanceof TypeOfFacetOnActionForTypeOfAnnotation);
            assertThat(facet.value(), classEqualTo(Order.class));
        }

        @Test
        public void whenDeprecatedTypeOfAnnotationOnActionNotReturningCollection() {

            class Order {
            }
            class Customer {
                @org.apache.isis.applib.annotation.TypeOf(Order.class)
                public Customer someAction() {
                    return null;
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processTypeOf(processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNull(facet);
        }

        @Test
        public void whenActionAnnotationOnActionReturningCollection() {

            class Order {
            }
            class Customer {
                @Action(typeOf = Order.class)
                public Collection someAction() {
                    return null;
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processTypeOf(processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNotNull(facet);
            Assert.assertTrue(facet instanceof TypeOfFacetForActionAnnotation);
            assertThat(facet.value(), classEqualTo(Order.class));
        }

        @Test
        public void whenActionAnnotationOnActionNotReturningCollection() {

            class Order {
            }
            class Customer {
                @Action(typeOf = Order.class)
                public Customer someAction() {
                    return null;
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processTypeOf(processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNull(facet);
        }

        @Test
        public void whenInferFromType() {

            class Order {
            }
            class Customer {
                public Order[] someAction() {
                    return null;
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processTypeOf(processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNotNull(facet);
            Assert.assertTrue(facet instanceof TypeOfFacetInferredFromArray);
            assertThat(facet.value(), classEqualTo(Order.class));
        }

        @Test
        public void whenInferFromGenerics() {

            class Order {
            }
            class Customer {
                public Collection<Order> someAction() {
                    return null;
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processTypeOf(processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNotNull(facet);
            Assert.assertTrue(facet instanceof TypeOfFacetInferredFromGenerics);
            assertThat(facet.value(), classEqualTo(Order.class));
        }

    }

    void allowingCommandConfigurationToReturn(final String value) {
        context.checking(new Expectations() {
            {
                allowing(mockConfiguration).getString("isis.services.command.actions");
                will(returnValue(value));
            }
        });
    }
    void allowingPublishingConfigurationToReturn(final String value) {
        context.checking(new Expectations() {
            {
                allowing(mockConfiguration).getString("isis.services.publish.actions");
                will(returnValue(value));
            }
        });
    }

}
