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
import java.util.UUID;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.CommandExecuteIn;
import org.apache.isis.applib.annotation.CommandPersistence;
import org.apache.isis.applib.annotation.CommandReification;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.HasUniqueId;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromArray;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromGenerics;
import org.apache.isis.core.metamodel.facets.actions.action.command.CommandFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.command.CommandFacetForActionAnnotationAsConfigured;
import org.apache.isis.core.metamodel.facets.actions.action.command.CommandFacetFromConfiguration;
import org.apache.isis.core.metamodel.facets.actions.action.hidden.HiddenFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.actions.action.publishing.PublishedActionFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.publishing.PublishedActionFacetFromConfiguration;
import org.apache.isis.core.metamodel.facets.actions.action.typeof.TypeOfFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.core.metamodel.facets.actions.prototype.PrototypeFacet;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacetAbstract;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import static org.apache.isis.core.commons.matchers.IsisMatchers.classEqualTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"hiding", "serial"})
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
        
        // PRODUCTION
        
        _Config.clear();
        
        facetFactory = new ActionAnnotationFacetFactory();

        context.checking(new Expectations() {{
            allowing(mockServicesInjector).lookupServiceElseFail(AuthenticationSessionProvider.class);
            will(returnValue(mockAuthenticationSessionProvider));
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

    class SomeTransactionalId implements HasUniqueId {
        public void someAction() {
        }

        @Override
        public UUID getUniqueId() {
            return null;
        }

        
    }

    public static class Invocation extends ActionAnnotationFacetFactoryTest {

        @Test
        public void withPostsActionInvokedEvent() {

            class Customer {

				class SomeActionInvokedDomainEvent extends ActionDomainEvent<Customer> { }

                @Action(domainEvent = SomeActionInvokedDomainEvent.class)
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // expect
            allowingLoadSpecificationRequestsFor(cls, actionMethod.getReturnType());
            expectRemoveMethod(actionMethod);

            _Config.put("isis.reflector.facet.actionAnnotation.domainEvent.postForDefault", true);

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(
            		cls, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processInvocation(processMethodContext);

            // then
            final ActionDomainEventFacet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof ActionDomainEventFacetAbstract);
            final ActionDomainEventFacetAbstract domainEventFacetImpl = (ActionDomainEventFacetAbstract) domainEventFacet;
            assertThat(domainEventFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));

            final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
            Assert.assertNotNull(invocationFacet);
            Assert.assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromActionAnnotation);
            final ActionInvocationFacetForDomainEventFromActionAnnotation invocationFacetImpl = (ActionInvocationFacetForDomainEventFromActionAnnotation) invocationFacet;
            assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
        }

        @Test
        public void withActionInteractionEvent() {

            class Customer {

                class SomeActionInvokedDomainEvent extends ActionDomainEvent<Customer> { }

                @Action(domainEvent = SomeActionInvokedDomainEvent.class)
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
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(
            		cls, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processInvocation(processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof ActionDomainEventFacetForActionAnnotation);
            final ActionDomainEventFacetForActionAnnotation domainEventFacetImpl = 
            		(ActionDomainEventFacetForActionAnnotation) domainEventFacet;
            assertThat(domainEventFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));

            final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
            Assert.assertNotNull(invocationFacet);
            
            Assert.assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromActionAnnotation);
            final ActionInvocationFacetForDomainEventFromActionAnnotation invocationFacetImpl = 
            		(ActionInvocationFacetForDomainEventFromActionAnnotation) invocationFacet;
            assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
        }
 
        @Test
        public void withActionDomainEvent() {

            class Customer {

                class SomeActionInvokedDomainEvent extends ActionDomainEvent<Customer> {
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
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(
            		cls, null, actionMethod, mockMethodRemover, facetedMethod);
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
                @SuppressWarnings("unused")
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // expect
            allowingLoadSpecificationRequestsFor(cls, actionMethod.getReturnType());
            expectRemoveMethod(actionMethod);

            _Config.put("isis.reflector.facet.actionAnnotation.domainEvent.postForDefault", true);

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(
            		cls, null, actionMethod, mockMethodRemover, facetedMethod);
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
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(
            		cls, null, actionMethod, mockMethodRemover, facetedMethod);
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
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(
            		cls, null, actionMethod, mockMethodRemover, facetedMethod);
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
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(
            		cls, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processRestrictTo(processMethodContext);

            // then
            final PrototypeFacet facet = facetedMethod.getFacet(PrototypeFacet.class);
            assertNull(facet);
        }

        @Test
        public void whenNotPresent() {

            class Customer {
                @SuppressWarnings("unused")
                public void someAction() {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(
            		cls, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processRestrictTo(processMethodContext);

            // then
            final PrototypeFacet facet = facetedMethod.getFacet(PrototypeFacet.class);
            assertNull(facet);
        }

    }

    public static class Semantics extends ActionAnnotationFacetFactoryTest {

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
            facetFactory.processSemantics(processMethodContext);

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
            facetFactory.processSemantics(processMethodContext);

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
            facetFactory.processSemantics(processMethodContext);

            // then
            final ActionSemanticsFacet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
            Assert.assertNotNull(facet);
            assertThat(facet.value(), is(SemanticsOf.NON_IDEMPOTENT));
        }

    }

    public static class Command extends ActionAnnotationFacetFactoryTest {

        @Test
        public void givenHasTransactionId_thenIgnored() {
            // given
            final Method actionMethod = findMethod(SomeTransactionalId.class, "someAction");

            // when
            facetFactory.processCommand(new ProcessMethodContext(SomeTransactionalId.class, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNull(facet);

        }

        @Test
        public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andSafeSemantics_thenNone() {

            // given
            allowingCommandConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});

            // when
            facetFactory.processCommand(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                    actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNull(facet);
        }

        @Test
        public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andNonSafeSemantics_thenAdded() {

            // given
            allowingCommandConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.IDEMPOTENT, facetedMethod) {});

            // when
            facetFactory.processCommand(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                    actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNotNull(facet);
            assert(facet instanceof  CommandFacetFromConfiguration);
            final CommandFacetFromConfiguration facetImpl = (CommandFacetFromConfiguration) facet;
            assertThat(facetImpl.persistence(), is(org.apache.isis.applib.annotation.CommandPersistence.PERSISTED));
            assertThat(facetImpl.executeIn(), is(org.apache.isis.applib.annotation.CommandExecuteIn.FOREGROUND));
        }

        @Test(expected=IllegalStateException.class)
        public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andNoSemantics_thenException() {

            // given
            allowingCommandConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            // when
            facetFactory.processCommand(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                    actionMethod, mockMethodRemover, facetedMethod));
        }

        @Test
        public void given_noAnnotation_and_configurationSetToNone_thenNone() {

            // given
            allowingCommandConfigurationToReturn("none");
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            // when
            facetFactory.processCommand(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                    actionMethod, mockMethodRemover, facetedMethod));

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
            facetFactory.processCommand(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                    actionMethod, mockMethodRemover, facetedMethod));

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

            facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});

            facetFactory.processCommand(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

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

            facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.IDEMPOTENT, facetedMethod) {});

            // when
            facetFactory.processCommand(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNotNull(facet);
            final CommandFacetForActionAnnotationAsConfigured facetImpl = (CommandFacetForActionAnnotationAsConfigured) facet;
            assertThat(facetImpl.persistence(), is(org.apache.isis.applib.annotation.CommandPersistence.IF_HINTED));
            assertThat(facetImpl.executeIn(), is(org.apache.isis.applib.annotation.CommandExecuteIn.BACKGROUND));
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

            facetFactory.processCommand(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));
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

            facetFactory.processCommand(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

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
            facetFactory.processCommand(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNotNull(facet);
            final CommandFacetForActionAnnotationAsConfigured facetImpl = (CommandFacetForActionAnnotationAsConfigured) facet;
            assertThat(facetImpl.persistence(), is(org.apache.isis.applib.annotation.CommandPersistence.IF_HINTED));
            assertThat(facetImpl.executeIn(), is(org.apache.isis.applib.annotation.CommandExecuteIn.BACKGROUND));
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
            facetFactory.processCommand(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

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
            facetFactory.processCommand(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(CommandFacet.class);
            assertNull(facet);
        }


    }

    public static class Publishing extends ActionAnnotationFacetFactoryTest {

        @Test
        public void givenHasTransactionId_thenIgnored() {

            final Method actionMethod = findMethod(SomeTransactionalId.class, "someAction");

            facetFactory.processPublishing(new ProcessMethodContext(SomeTransactionalId.class, null, actionMethod, mockMethodRemover, facetedMethod));

            final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
            assertNull(facet);

            expectNoMethodsRemoved();
        }

        @Test
        public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andSafeSemantics_thenNone() {

            // given
            allowingPublishingConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});

            // when
            facetFactory.processPublishing(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                    actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
            assertNull(facet);
        }

        @Test
        public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andNonSafeSemantics_thenAdded() {

            // given
            allowingPublishingConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.IDEMPOTENT, facetedMethod) {});

            // when
            facetFactory.processPublishing(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                    actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
            assertNotNull(facet);
            final PublishedActionFacetFromConfiguration facetImpl = (PublishedActionFacetFromConfiguration) facet;
        }

        @Test(expected=IllegalStateException.class)
        public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andNoSemantics_thenException() {

            // given
            allowingPublishingConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            // when
            facetFactory.processPublishing(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                    actionMethod, mockMethodRemover, facetedMethod));

        }

        @Test
        public void given_noAnnotation_and_configurationSetToNone_thenNone() {

            // given
            allowingPublishingConfigurationToReturn("none");
            final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

            // when
            facetFactory.processPublishing(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
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

            allowingPublishingConfigurationToReturn("all");

            // when
            facetFactory.processPublishing(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
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

            allowingPublishingConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(Customer.class, "someAction");

            facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});

            facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

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

            allowingPublishingConfigurationToReturn("ignoreQueryOnly");
            final Method actionMethod = findMethod(Customer.class, "someAction");

            facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.IDEMPOTENT, facetedMethod) {});

            // when
            facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
            assertNotNull(facet);
            final PublishedActionFacetForActionAnnotation facetImpl = (PublishedActionFacetForActionAnnotation) facet;

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

            facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));
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

            facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

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

            allowingPublishingConfigurationToReturn("all");

            // when
            facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

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
            allowingPublishingConfigurationToReturn("none");

            // when
            facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

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
            facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

            // then
            final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
            assertNull(facet);
        }

    }

    public static class TypeOf extends ActionAnnotationFacetFactoryTest {


        @Test
        public void whenDeprecatedTypeOfAnnotationOnActionNotReturningCollection() {

            class Customer {
                @SuppressWarnings("unused")
                public Customer someAction() {
                    return null;
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
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
                @SuppressWarnings("rawtypes")
                @Action(typeOf = Order.class)
                public Collection someAction() {
                    return null;
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
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
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
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
                @SuppressWarnings("unused")
                public Order[] someAction() {
                    return null;
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
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
                @SuppressWarnings("unused")
                public Collection<Order> someAction() {
                    return null;
                }
            }

            // given
            final Class<?> cls = Customer.class;
            actionMethod = findMethod(cls, "someAction");

            // when
            final ProcessMethodContext processMethodContext = new ProcessMethodContext(cls, null, actionMethod, mockMethodRemover, facetedMethod);
            facetFactory.processTypeOf(processMethodContext);

            // then
            final TypeOfFacet facet = facetedMethod.getFacet(TypeOfFacet.class);
            Assert.assertNotNull(facet);
            Assert.assertTrue(facet instanceof TypeOfFacetInferredFromGenerics);
            assertThat(facet.value(), classEqualTo(Order.class));
        }

    }

    void allowingCommandConfigurationToReturn(final String value) {
        _Config.put("isis.services.command.actions", value);
    }

    void allowingPublishingConfigurationToReturn(final String value) {
        _Config.put("isis.services.publish.actions", value);
    }

}
