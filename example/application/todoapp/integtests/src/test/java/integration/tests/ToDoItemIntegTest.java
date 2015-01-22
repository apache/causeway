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
package integration.tests;

import dom.todo.ToDoItem;
import dom.todo.ToDoItemSubscriptions;
import dom.todo.ToDoItems;
import fixture.todo.scenarios.ToDoItemsRecreateAndCompleteSeveral;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.EventObject;
import java.util.List;
import javax.activation.MimeType;
import javax.inject.Inject;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.auto.Mock;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.eventbus.AbstractInteractionEvent;
import org.apache.isis.applib.services.eventbus.ActionDomainEvent;
import org.apache.isis.applib.services.eventbus.CollectionDomainEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.eventbus.PropertyDomainEvent;
import org.apache.isis.applib.value.Blob;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ToDoItemIntegTest extends AbstractToDoIntegTest {

    ToDoItemsRecreateAndCompleteSeveral fixtureScript;

    @Before
    public void setUpData() throws Exception {
        fixtureScript = new ToDoItemsRecreateAndCompleteSeveral();
        fixtureScripts.runFixtureScript(fixtureScript, null);
    }

    @Inject
    FixtureScripts fixtureScripts;
    @Inject
    ToDoItems toDoItems;
    @Inject
    ToDoItemSubscriptions toDoItemSubscriptions;

    ToDoItem toDoItem;

    @Before
    public void setUp() throws Exception {
        final List<ToDoItem> all = toDoItems.notYetComplete();
        toDoItem = wrap(all.get(0));
    }

    @After
    public void tearDown() throws Exception {
        toDoItemSubscriptions.reset();
    }


    public static class Title extends ToDoItemIntegTest {

        private LocalDate dueBy;

        @Before
        public void setUp() throws Exception {
            super.setUp();
            final List<ToDoItem> all = wrap(toDoItems).notYetComplete();
            toDoItem = wrap(all.get(0));

            toDoItem = wrap(fixtureScript.lookup("to-do-items-recreate-and-complete-several/to-do-item-for-buy-bread/item-1", ToDoItem.class));
            assertThat(toDoItem, is(not(nullValue())));

            nextTransaction();

            dueBy = toDoItem.getDueBy();
        }


        @Test
        public void includesDescription() throws Exception {

            // given
            assertThat(container().titleOf(toDoItem), containsString("Buy bread due by"));

            // when
            unwrap(toDoItem).setDescription("Buy bread and butter");

            // then
            assertThat(container().titleOf(toDoItem), containsString("Buy bread and butter due by"));
        }

        @Test
        public void includesDueDateIfAny() throws Exception {

            // given
            assertThat(container().titleOf(toDoItem), containsString("due by " + dueBy.toString("yyyy-MM-dd")));

            // when
            final LocalDate fiveDaysFromNow = Clock.getTimeAsLocalDate().plusDays(5);
            unwrap(toDoItem).setDueBy(fiveDaysFromNow);

            // then
            assertThat(container().titleOf(toDoItem), containsString("due by " + fiveDaysFromNow.toString("yyyy-MM-dd")));
        }


        @Test
        public void ignoresDueDateIfNone() throws Exception {

            // when
            // (since wrapped, will call clearDueBy)
            toDoItem.setDueBy(null);

            // then
            assertThat(container().titleOf(toDoItem), not(containsString("due by")));
        }

        @Test
        public void usesWhetherCompleted() throws Exception {

            // given
            assertThat(container().titleOf(toDoItem), not(containsString("Completed!")));

            // when
            toDoItem.completed();

            // then
            assertThat(container().titleOf(toDoItem), not(containsString("due by")));
            assertThat(container().titleOf(toDoItem), containsString("Buy bread - Completed!"));
        }
    }

    public static class Actions {

        public static class Completed extends ToDoItemIntegTest {

            @Test
            public void happyCase() throws Exception {

                // given
                assertThat(toDoItem.isComplete(), is(false));

                // when
                toDoItem.completed();

                // then
                assertThat(toDoItem.isComplete(), is(true));
            }

            @Test
            public void cannotCompleteIfAlreadyCompleted() throws Exception {

                // given
                unwrap(toDoItem).setComplete(true);

                // when, then should fail
                expectedExceptions.expectMessage("Already completed");
                toDoItem.completed();

                // and then
                final EventObject ev = toDoItemSubscriptions.mostRecentlyReceivedEvent(EventObject.class);
                assertThat(ev, is(nullValue()));
            }


            @Test
            public void cannotSetPropertyDirectly() throws Exception {

                // given

                // when, then should fail
                expectedExceptions.expectMessage("Always disabled");
                toDoItem.setComplete(true);

                // and then
                final EventObject ev = toDoItemSubscriptions.mostRecentlyReceivedEvent(EventObject.class);
                assertThat(ev, is(nullValue()));
            }

            @Test
            public void subscriberReceivesEvents() throws Exception {

                // given
                toDoItemSubscriptions.reset();
                assertThat(toDoItemSubscriptions.getSubscriberBehaviour(), is(ToDoItemSubscriptions.Behaviour.AnyExecuteAccept));
                assertThat(unwrap(toDoItem).isComplete(), is(false));

                // when
                toDoItem.completed();

                // then
                assertThat(unwrap(toDoItem).isComplete(), is(true));

                // and then
                final List<ToDoItem.CompletedEvent> receivedEvents = toDoItemSubscriptions.receivedEvents(ToDoItem.CompletedEvent.class);

                // hide, disable, validate, executing, executed
                // sent to both the general on(ActionInteractionEvent ev)
                // and also the specific on(final ToDoItem.CompletedEvent ev)
                assertThat(receivedEvents.size(), is(5*2));
                final ToDoItem.CompletedEvent ev = receivedEvents.get(0);

                ToDoItem source = ev.getSource();
                assertThat(source, is(equalTo(unwrap(toDoItem))));
                assertThat(ev.getIdentifier().getMemberName(), is("completed"));
            }

            @Test
            public void subscriberVetoesEventWithRecoverableException() throws Exception {

                // given
                toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.AnyExecuteVetoWithRecoverableException);

                // then
                expectedExceptions.expect(RecoverableException.class);

                // when
                toDoItem.completed();
            }

            @Test
            public void subscriberVetoesEventWithNonRecoverableException() throws Exception {

                // given
                toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.AnyExecuteVetoWithNonRecoverableException);

                // then
                expectedExceptions.expect(NonRecoverableException.class);

                // when
                toDoItem.completed();
            }

            @Test
            public void subscriberVetoesEventWithAnyOtherException() throws Exception {

                // given
                toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.AnyExecuteVetoWithOtherException);

                // then
                expectedExceptions.expect(RuntimeException.class);

                // when
                toDoItem.completed();
            }

        }

        /**
         * This test demonstrates how a single service can be replaced, eg to use a mock.
         */
        public static class Completed_withMockService extends ToDoItemIntegTest {

            private EventBusService originalEventBusService;
            @Mock
            private EventBusService mockEventBusService;

            @Before
            public void setUpMockEventBusService() throws Exception {
                originalEventBusService = scenarioExecution().service(EventBusService.class);

                context.checking(new Expectations() {{
                    ignoring(mockEventBusService).register(with(any(Object.class)));
                    ignoring(mockEventBusService).unregister(with(any(Object.class)));
                }});

                scenarioExecution().replaceService(originalEventBusService, mockEventBusService);
                scenarioExecution().closeSession();
                scenarioExecution().openSession();

                final List<ToDoItem> all = toDoItems.notYetComplete();
                toDoItem = wrap(all.get(0));
            }


            @After
            public void reinstateOriginalEventBusService() throws Exception {
                scenarioExecution().replaceService(mockEventBusService, originalEventBusService);
            }

            @Test
            public void raisesEvent() throws Exception {

                final Sequence busRulesThenExec = context.sequence("busRulesThenExec");
                // then
                context.checking(new Expectations() {{
                    oneOf(mockEventBusService).post(with(completedEvent(AbstractInteractionEvent.Phase.HIDE)));
                    inSequence(busRulesThenExec);
                    oneOf(mockEventBusService).post(with(completedEvent(AbstractInteractionEvent.Phase.DISABLE)));
                    inSequence(busRulesThenExec);
                    oneOf(mockEventBusService).post(with(completedEvent(AbstractInteractionEvent.Phase.VALIDATE)));
                    inSequence(busRulesThenExec);
                    oneOf(mockEventBusService).post(with(completedEvent(AbstractInteractionEvent.Phase.EXECUTING)));
                    inSequence(busRulesThenExec);
                    oneOf(mockEventBusService).post(with(completedEvent(AbstractInteractionEvent.Phase.EXECUTED)));
                    inSequence(busRulesThenExec);
                }});

                // when
                toDoItem.completed();
            }

            private Matcher<Object> completedEvent(final AbstractInteractionEvent.Phase phase) {
                return new TypeSafeMatcher<Object>() {
                    @Override
                    protected boolean matchesSafely(Object item) {
                        if (!(item instanceof ToDoItem.CompletedEvent)) {
                            return false;
                        }

                        final ToDoItem.CompletedEvent completedEvent = (ToDoItem.CompletedEvent) item;
                        return completedEvent.getPhase() == phase;

                    }

                    @Override
                    public void describeTo(Description description) {
                        description.appendText(" instance of a ToDoItem.CompletedEvent, " + phase);
                    }
                };
            }
        }


        public static class Duplicate extends ToDoItemIntegTest {

            ToDoItem duplicateToDoItem;

            @Inject
            private ClockService clockService;

            @Test
            public void happyCase() throws Exception {

                // given
                final LocalDate todaysDate = clockService.now();
                toDoItem.setDueBy(todaysDate);
                toDoItem.updateCost(new BigDecimal("123.45"));

                duplicateToDoItem = toDoItem.duplicate(
                        unwrap(toDoItem).default0Duplicate(),
                        unwrap(toDoItem).default1Duplicate(),
                        unwrap(toDoItem).default2Duplicate(),
                        unwrap(toDoItem).default3Duplicate(),
                        new BigDecimal("987.65"));

                // then
                assertThat(duplicateToDoItem.getDescription(), is(toDoItem.getDescription() + " - Copy"));
                assertThat(duplicateToDoItem.getCategory(), is(toDoItem.getCategory()));
                assertThat(duplicateToDoItem.getDueBy(), is(todaysDate));
                assertThat(duplicateToDoItem.getCost(), is(new BigDecimal("987.65")));
            }
        }

        public static class NotYetCompleted extends ToDoItemIntegTest {

            @Test
            public void happyCase() throws Exception {

                // given
                unwrap(toDoItem).setComplete(true);

                // when
                toDoItem.notYetCompleted();

                // then
                assertThat(toDoItem.isComplete(), is(false));
            }

            @Test
            public void cannotUndoIfNotYetCompleted() throws Exception {

                // given
                assertThat(toDoItem.isComplete(), is(false));

                // when, then should fail
                expectedExceptions.expectMessage("Not yet completed");
                toDoItem.notYetCompleted();
            }

            /**
             * Even though {@link dom.todo.ToDoItem#notYetCompleted()} is not annotated with
             * {@link org.apache.isis.applib.annotation.ActionInteraction}, an event is still raised.
             */
            @Test
            public void subscriberReceivesEvent() throws Exception {

                // given
                assertThat(toDoItemSubscriptions.getSubscriberBehaviour(), is(ToDoItemSubscriptions.Behaviour.AnyExecuteAccept));
                unwrap(toDoItem).setComplete(true);

                // when
                toDoItem.notYetCompleted();

                // then
                assertThat(unwrap(toDoItem).isComplete(), is(false));

                // and then
                final ActionDomainEvent<ToDoItem> ev = toDoItemSubscriptions.mostRecentlyReceivedEvent(ActionDomainEvent.class);
                assertThat(ev, is(not(nullValue())));

                ToDoItem source = ev.getSource();
                assertThat(source, is(equalTo(unwrap(toDoItem))));
                assertThat(ev.getIdentifier().getMemberName(), is("notYetCompleted"));
            }
        }
    }

    public static class Collections {

        public static class Dependencies {
            public static class Add extends ToDoItemIntegTest {

                private ToDoItem otherToDoItem;

                @Before
                public void setUp() throws Exception {
                    super.setUp();
                    final List<ToDoItem> items = wrap(toDoItems).notYetComplete();
                    otherToDoItem = wrap(items.get(1));
                }

                @After
                public void tearDown() throws Exception {
                    unwrap(toDoItem).getDependencies().clear();
                    super.tearDown();
                }

                @Test
                public void happyCase() throws Exception {

                    // given
                    assertThat(toDoItem.getDependencies().size(), is(0));

                    // when
                    toDoItem.add(otherToDoItem);

                    // then
                    assertThat(toDoItem.getDependencies().size(), is(1));
                    assertThat(toDoItem.getDependencies().iterator().next(), is(unwrap(otherToDoItem)));
                }


                @Test
                public void cannotDependOnSelf() throws Exception {

                    // then
                    expectedExceptions.expectMessage("Can't set up a dependency to self");

                    // when
                    toDoItem.add(toDoItem);
                }

                @Test
                public void cannotAddIfComplete() throws Exception {

                    // given
                    unwrap(toDoItem).setComplete(true);

                    // then
                    expectedExceptions.expectMessage("Cannot add dependencies for items that are complete");

                    // when
                    toDoItem.add(otherToDoItem);
                }


                @Test
                public void subscriberReceivesEvent() throws Exception {

                    // given
                    toDoItemSubscriptions.reset();

                    // when
                    toDoItem.add(otherToDoItem);

                    // then received events
                    @SuppressWarnings("unchecked")
                    final List<EventObject> receivedEvents = toDoItemSubscriptions.receivedEvents();

                    assertThat(receivedEvents.size(), is(7));
                    assertThat(receivedEvents.get(0) instanceof ActionDomainEvent, is(true)); // ToDoItem#add() executed
                    assertThat(receivedEvents.get(1) instanceof CollectionDomainEvent, is(true)); // ToDoItem#dependencies add, executed
                    assertThat(receivedEvents.get(2) instanceof CollectionDomainEvent, is(true)); // ToDoItem#dependencies add, executing
                    assertThat(receivedEvents.get(3) instanceof ActionDomainEvent, is(true)); // ToDoItem#add executing
                    assertThat(receivedEvents.get(4) instanceof ActionDomainEvent, is(true)); // ToDoItem#add validate
                    assertThat(receivedEvents.get(5) instanceof ActionDomainEvent, is(true)); // ToDoItem#add disable
                    assertThat(receivedEvents.get(6) instanceof ActionDomainEvent, is(true)); // ToDoItem#add hide

                    // inspect the collection interaction (posted programmatically in ToDoItem#add)
                    final CollectionDomainEvent<ToDoItem,ToDoItem> ciEv = (CollectionDomainEvent<ToDoItem, ToDoItem>) toDoItemSubscriptions.mostRecentlyReceivedEvent(CollectionDomainEvent.class);
                    assertThat(ciEv, is(notNullValue()));

                    assertThat(ciEv.getSource(), is(equalTo(unwrap(toDoItem))));
                    assertThat(ciEv.getIdentifier().getMemberName(), is("dependencies"));
                    assertThat(ciEv.getOf(), is(CollectionDomainEvent.Of.ADD_TO));
                    assertThat(ciEv.getValue(), is(unwrap(otherToDoItem)));

                    // inspect the action interaction (posted declaratively by framework)
                    final ActionDomainEvent<ToDoItem> aiEv = (ActionDomainEvent<ToDoItem>) toDoItemSubscriptions.mostRecentlyReceivedEvent(ActionDomainEvent.class);
                    assertThat(aiEv, is(notNullValue()));

                    assertThat(aiEv.getSource(), is(equalTo(unwrap(toDoItem))));
                    assertThat(aiEv.getIdentifier().getMemberName(), is("add"));
                    assertThat(aiEv.getArguments().size(), is(1));
                    assertThat(aiEv.getArguments().get(0), is(unwrap((Object)otherToDoItem)));
                    assertThat(aiEv.getCommand(), is(notNullValue()));
                }

                @Test
                public void subscriberVetoesEventWithRecoverableException() throws Exception {

                    // given
                    toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.AnyExecuteVetoWithRecoverableException);

                    // then
                    expectedExceptions.expect(RecoverableException.class);

                    // when
                    toDoItem.add(otherToDoItem);
                }

                @Test
                public void subscriberVetoesEventWithNonRecoverableException() throws Exception {

                    // given
                    toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.AnyExecuteVetoWithNonRecoverableException);

                    // then
                    expectedExceptions.expect(NonRecoverableException.class);

                    // when
                    toDoItem.add(otherToDoItem);
                }

                @Test
                public void subscriberVetoesEventWithAnyOtherException() throws Exception {

                    // given
                    toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.AnyExecuteVetoWithOtherException);

                    // then
                    expectedExceptions.expect(RuntimeException.class);

                    // when
                    toDoItem.add(otherToDoItem);
                }
            }
            public static class Remove extends ToDoItemIntegTest {

                private ToDoItem otherToDoItem;
                private ToDoItem yetAnotherToDoItem;

                @Before
                public void setUp() throws Exception {
                    super.setUp();
                    final List<ToDoItem> items = wrap(toDoItems).notYetComplete();
                    otherToDoItem = wrap(items.get(1));
                    yetAnotherToDoItem = wrap(items.get(2));

                    toDoItem.add(otherToDoItem);
                }

                @After
                public void tearDown() throws Exception {
                    unwrap(toDoItem).getDependencies().clear();
                    super.tearDown();
                }

                @Test
                public void happyCase() throws Exception {

                    // given
                    assertThat(toDoItem.getDependencies().size(), is(1));

                    // when
                    toDoItem.remove(otherToDoItem);

                    // then
                    assertThat(toDoItem.getDependencies().size(), is(0));
                }


                @Test
                public void cannotRemoveItemIfNotADependency() throws Exception {

                    // then
                    expectedExceptions.expectMessage("Not a dependency");

                    // when
                    toDoItem.remove(yetAnotherToDoItem);
                }

                @Test
                public void cannotRemoveDependencyIfComplete() throws Exception {

                    // given
                    unwrap(toDoItem).setComplete(true);

                    // then
                    expectedExceptions.expectMessage("Cannot remove dependencies for items that are complete");

                    // when
                    toDoItem.remove(otherToDoItem);
                }

                @Test
                public void subscriberVetoesEventWithRecoverableException() throws Exception {

                    // given
                    toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.AnyExecuteVetoWithRecoverableException);

                    // then
                    expectedExceptions.expect(RecoverableException.class);

                    // when
                    toDoItem.remove(otherToDoItem);
                }

                @Test
                public void subscriberVetoesEventWithNonRecoverableException() throws Exception {

                    // given
                    toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.AnyExecuteVetoWithNonRecoverableException);

                    // then
                    expectedExceptions.expect(NonRecoverableException.class);

                    // when
                    toDoItem.remove(otherToDoItem);
                }

                @Test
                public void subscriberVetoesEventWithAnyOtherException() throws Exception {

                    // given
                    toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.AnyExecuteVetoWithOtherException);

                    // then
                    expectedExceptions.expect(RuntimeException.class);

                    // when
                    toDoItem.remove(otherToDoItem);
                }
            }
        }

    }

    public static class Properties {

        public static class Attachment extends ToDoItemIntegTest {

            @Test
            public void happyCase() throws Exception {

                byte[] bytes = "{\"foo\": \"bar\"}".getBytes(Charset.forName("UTF-8"));
                final Blob newAttachment = new Blob("myfile.json", new MimeType("application/json"), bytes);

                // when
                toDoItem.setAttachment(newAttachment);

                // then
                assertThat(toDoItem.getAttachment(), is(newAttachment));
            }

            @Test
            public void canBeNull() throws Exception {

                // when
                toDoItem.setAttachment((Blob)null);

                // then
                assertThat(toDoItem.getAttachment(), is((Blob)null));
            }
        }

        public static class Category extends ToDoItemIntegTest {

            @Test
            public void cannotModify() throws Exception {

                // when, then
                expectedExceptions.expectMessage(containsString("Reason: Use action to update both category and subcategory."));
                toDoItem.setCategory(ToDoItem.Category.Professional);
            }
        }

        public static class Cost extends ToDoItemIntegTest {

            private BigDecimal cost;

            @Before
            public void setUp() throws Exception {
                super.setUp();
                cost = toDoItem.getCost();
            }

            @Test
            public void happyCaseUsingProperty() throws Exception {

                final BigDecimal newCost = new BigDecimal("123.45");

                // when
                toDoItem.updateCost(newCost);

                // then
                assertThat(toDoItem.getCost(), is(newCost));
            }

            @Test
            public void happyCaseUsingAction() throws Exception {

                final BigDecimal newCost = new BigDecimal("123.45");

                // when
                toDoItem.updateCost(newCost);

                // then
                assertThat(toDoItem.getCost(), is(newCost));
            }

            @Test
            public void canBeNull() throws Exception {

                // when
                toDoItem.updateCost((BigDecimal)null);

                // then
                assertThat(toDoItem.getCost(), is((BigDecimal)null));
            }

            @Test
            public void defaultForAction() throws Exception {

                // then
                assertThat(unwrap(toDoItem).default0UpdateCost(), is(cost));
            }

        }

        public static class Description extends ToDoItemIntegTest {

            @Test
            public void happyCase() throws Exception {

                // given
                assertThat(toDoItem.getDescription(), is("Buy bread"));

                // when
                toDoItem.setDescription("Buy bread and butter");

                // then
                assertThat(toDoItem.getDescription(), is("Buy bread and butter"));
            }


            @Test
            public void failsRegex() throws Exception {

                // when
                expectedExceptions.expectMessage("Doesn't match pattern");
                toDoItem.setDescription("exclamation marks are not allowed!!!");
            }

            @Test
            public void cannotBeNull() throws Exception {

                // when, then
                expectedExceptions.expectMessage("Mandatory");
                toDoItem.setDescription(null);
            }

            @Test
            public void cannotUseModify() throws Exception {

                expectedExceptions.expectMessage("Cannot invoke supporting method for 'Description'; use only property accessor/mutator");

                // given
                assertThat(toDoItem.getDescription(), is("Buy bread"));

                // when
                toDoItem.modifyDescription("Buy bread and butter");

                // then
                assertThat(toDoItem.getDescription(), is("Buy bread"));
            }

            @Test
            public void cannotUseClear() throws Exception {

                expectedExceptions.expectMessage("Cannot invoke supporting method for 'Description'; use only property accessor/mutator");

                // given
                assertThat(toDoItem.getDescription(), is("Buy bread"));

                // when
                toDoItem.clearDescription();

                // then
                assertThat(toDoItem.getDescription(), is("Buy bread"));
            }


            @Test
            public void onlyJustShortEnough() throws Exception {

                // when, then
                toDoItem.setDescription(characters(100));
            }

            @Test
            public void tooLong() throws Exception {

                // then
                expectedExceptions.expectMessage("The value proposed exceeds the maximum length of 100");

                // when
                toDoItem.setDescription(characters(101));
            }


            @Test
            public void subscriberReceivesEvent() throws Exception {

                // given
                assertThat(toDoItemSubscriptions.getSubscriberBehaviour(), is(ToDoItemSubscriptions.Behaviour.AnyExecuteAccept));
                assertThat(toDoItem.getDescription(), is("Buy bread"));

                // when
                toDoItem.setDescription("Buy bread and butter");

                // then published and received
                @SuppressWarnings("unchecked")
                final PropertyDomainEvent<ToDoItem,String> ev = toDoItemSubscriptions.mostRecentlyReceivedEvent(PropertyDomainEvent.class);
                assertThat(ev, is(not(nullValue())));

                ToDoItem source = ev.getSource();
                assertThat(source, is(equalTo(unwrap(toDoItem))));
                assertThat(ev.getIdentifier().getMemberName(), is("description"));
                assertThat(ev.getOldValue(), is("Buy bread"));
                assertThat(ev.getNewValue(), is("Buy bread and butter"));
            }

            @Test
            public void subscriberVetoesEventWithRecoverableException() throws Exception {

                // given
                toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.AnyExecuteVetoWithRecoverableException);

                // then
                expectedExceptions.expect(RecoverableException.class);

                // when
                toDoItem.setDescription("Buy bread and butter");
            }


            @Test
            public void subscriberVetoesEventWithNonRecoverableException() throws Exception {

                // given
                toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.AnyExecuteVetoWithNonRecoverableException);

                // then
                expectedExceptions.expect(NonRecoverableException.class);

                // when
                toDoItem.setDescription("Buy bread and butter");
            }


            @Test
            public void subscriberVetoesEventWithAnyOtherException() throws Exception {

                // given
                toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.AnyExecuteVetoWithOtherException);

                // then
                expectedExceptions.expect(RuntimeException.class);

                // when
                toDoItem.setDescription("Buy bread and butter");
            }


            private static String characters(final int n) {
                StringBuffer buf = new StringBuffer();
                for(int i=0; i<n; i++) {
                    buf.append("a");
                }
                return buf.toString();
            }
        }

        public static class DueBy extends ToDoItemIntegTest {

            @Inject
            private ClockService clockService;

            @Test
            public void happyCase() throws Exception {

                // when
                final LocalDate fiveDaysFromNow = clockService.now().plusDays(5);
                toDoItem.setDueBy(fiveDaysFromNow);

                // then
                assertThat(toDoItem.getDueBy(), is(fiveDaysFromNow));
            }


            @Test
            public void canBeNull() throws Exception {

                // when
                toDoItem.setDueBy((LocalDate)null);

                // then
                assertThat(toDoItem.getDueBy(), is((LocalDate)null));
            }

            @Test
            public void canBeUpToSixDaysInPast() throws Exception {

                final LocalDate nowAsLocalDate = clockService.now();
                final LocalDate sixDaysAgo = nowAsLocalDate.plusDays(-5);

                // when
                toDoItem.setDueBy(sixDaysAgo);

                // then
                assertThat(toDoItem.getDueBy(), is(sixDaysAgo));
            }


            @Test
            public void cannotBeMoreThanSixDaysInPast() throws Exception {

                final LocalDate sevenDaysAgo = Clock.getTimeAsLocalDate().plusDays(-7);

                // when, then
                expectedExceptions.expectMessage("Due by date cannot be more than one week old");
                toDoItem.setDueBy(sevenDaysAgo);
            }
        }

        public static class Notes extends ToDoItemIntegTest {

            @Test
            public void happyCase() throws Exception {

                final String newNotes = "Lorem ipsum yada yada";

                // when
                toDoItem.setNotes(newNotes);

                // then
                assertThat(toDoItem.getNotes(), is(newNotes));
            }

            @Test
            public void canBeNull() throws Exception {

                // when
                toDoItem.setNotes((String)null);

                // then
                assertThat(toDoItem.getNotes(), is((String)null));
            }

            @Test
            public void suscriberReceivedDefaultEvent() throws Exception {

                final String newNotes = "Lorem ipsum yada yada";

                // when
                toDoItem.setNotes(newNotes);

                // then
                assertThat(unwrap(toDoItem).getNotes(), is(newNotes));

                // and then receive the default event.
                @SuppressWarnings("unchecked")
                final PropertyDomainEvent.Default ev = toDoItemSubscriptions.mostRecentlyReceivedEvent(PropertyDomainEvent.Default.class);
                assertThat(ev, is(notNullValue()));

                assertThat(ev.getSource(), is((Object)unwrap(toDoItem)));
                assertThat(ev.getNewValue(), is((Object)newNotes));
            }


        }

        public static class OwnedBy extends ToDoItemIntegTest {

            @Test
            public void cannotModify() throws Exception {

                // when, then
                expectedExceptions.expectMessage("Reason: Hidden on Everywhere. Identifier: dom.todo.ToDoItem#ownedBy()");
                toDoItem.setOwnedBy("other");
            }


        }

        public static class Subcategory extends ToDoItemIntegTest {

            @Test
            public void cannotModify() throws Exception {

                // when, then
                expectedExceptions.expectMessage(containsString("Reason: Use action to update both category and subcategory."));
                toDoItem.setSubcategory(ToDoItem.Subcategory.Chores);
            }
        }

    }




}