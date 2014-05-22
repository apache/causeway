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
package integration.tests.colls;

import dom.todo.ToDoItem;
import dom.todo.ToDoItemSubscriptions;
import dom.todo.ToDoItems;
import fixture.todo.integtests.ToDoItemsIntegTestFixture;
import integration.tests.ToDoIntegTest;

import java.util.List;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.services.eventbus.CollectionRemovedFromEvent;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ToDoItemTest_dependencies_remove extends ToDoIntegTest {

    @Before
    public void setUpData() throws Exception {
        scenarioExecution().install(new ToDoItemsIntegTestFixture());
    }

    @Inject
    private ToDoItems toDoItems;
    @Inject
    private ToDoItemSubscriptions toDoItemSubscriptions;

    private ToDoItem toDoItem;
    private ToDoItem otherToDoItem;
    private ToDoItem yetAnotherToDoItem;


    @Before
    public void setUp() throws Exception {

        final List<ToDoItem> items = wrap(toDoItems).notYetComplete();
        toDoItem = wrap(items.get(0));
        otherToDoItem = wrap(items.get(1));
        yetAnotherToDoItem = wrap(items.get(2));
        
        toDoItem.add(otherToDoItem);
    }

    @After
    public void tearDown() throws Exception {
        unwrap(toDoItem).getDependencies().clear();
        toDoItemSubscriptions.reset();
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
    public void subscriberReceivesEvent() throws Exception {

        // given
        assertThat(toDoItemSubscriptions.getSubscriberBehaviour(), is(ToDoItemSubscriptions.Behaviour.AcceptEvents));
        assertThat(toDoItem.getDependencies().size(), is(1));

        // when
        toDoItem.remove(otherToDoItem);

        // then
        @SuppressWarnings("unchecked")
        final CollectionRemovedFromEvent<ToDoItem,ToDoItem> ev = toDoItemSubscriptions.mostRecentlyReceivedEvent(CollectionRemovedFromEvent.class);
        assertThat(ev, is(not(nullValue())));

        ToDoItem source = ev.getSource();
        assertThat(source, is(equalTo(unwrap(toDoItem))));
        assertThat(ev.getIdentifier().getMemberName(), is("dependencies"));
        assertThat(ev.getValue(), is(unwrap(otherToDoItem)));
    }

    @Test
    public void subscriberVetoesEventWithRecoverableException() throws Exception {

        // given
        toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.RejectEventsWithRecoverableException);

        // then
        expectedExceptions.expect(RecoverableException.class);

        // when
        toDoItem.remove(otherToDoItem);
    }

    @Test
    public void subscriberVetoesEventWithNonRecoverableException() throws Exception {

        // given
        toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.RejectEventsWithNonRecoverableException);

        // then
        expectedExceptions.expect(NonRecoverableException.class);

        // when
        toDoItem.remove(otherToDoItem);
    }

    @Test
    public void subscriberThrowingOtherExceptionIsIgnored() throws Exception {

        // given
        toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.ThrowOtherException);

        // when
        toDoItem.remove(otherToDoItem);

        // then
        // (no expectedExceptions setup, expect to continue)
        assertTrue(true);
    }


}