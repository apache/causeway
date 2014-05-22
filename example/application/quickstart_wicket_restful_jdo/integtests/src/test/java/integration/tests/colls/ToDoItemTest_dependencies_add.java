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
import org.apache.isis.applib.services.eventbus.CollectionAddedToEvent;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ToDoItemTest_dependencies_add extends ToDoIntegTest {

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

    @Before
    public void setUp() throws Exception {
        final List<ToDoItem> items = wrap(toDoItems).notYetComplete();
        toDoItem = wrap(items.get(0));
        otherToDoItem = wrap(items.get(1));
    }

    @After
    public void tearDown() throws Exception {
        unwrap(toDoItem).getDependencies().clear();
        toDoItemSubscriptions.reset();
    }

    @Test
    public void happyCase() throws Exception {

        // given
        assertThat(toDoItem.getDependencies().size(), is(0));
        
        // when
        toDoItem.add(otherToDoItem);
        
        // then
        assertThat(toDoItem.getDependencies().size(), is(1));
        assertThat(toDoItem.getDependencies().first(), is(unwrap(otherToDoItem)));
        
        // and then
        @SuppressWarnings("unchecked")
        final CollectionAddedToEvent<ToDoItem,ToDoItem> ev = toDoItemSubscriptions.mostRecentlyReceivedEvent(CollectionAddedToEvent.class);
        assertThat(ev, is(not(nullValue()))); 
        
        ToDoItem source = ev.getSource();
        assertThat(source, is(equalTo(unwrap(toDoItem))));
        assertThat(ev.getIdentifier().getMemberName(), is("dependencies"));
        assertThat(ev.getValue(), is(unwrap(otherToDoItem)));
    }


    @Test
    public void cannotDependOnSelf() throws Exception {

        // then
        expectedExceptions.expectMessage("Can't set up a dependency to self");

        // when
        toDoItem.add(toDoItem);
    }

    @Test
    public void cannotAddDependencyIfComplete() throws Exception {

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
        assertThat(toDoItemSubscriptions.getSubscriberBehaviour(), is(ToDoItemSubscriptions.Behaviour.AcceptEvents));

        // when
        toDoItem.add(otherToDoItem);

        // then
        @SuppressWarnings("unchecked")
        final CollectionAddedToEvent<ToDoItem,ToDoItem> ev = toDoItemSubscriptions.mostRecentlyReceivedEvent(CollectionAddedToEvent.class);
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
        toDoItem.add(otherToDoItem);
    }

    @Test
    public void subscriberVetoesEventWithNonRecoverableException() throws Exception {

        // given
        toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.RejectEventsWithNonRecoverableException);

        // then
        expectedExceptions.expect(NonRecoverableException.class);

        // when
        toDoItem.add(otherToDoItem);
    }

    @Test
    public void subscriberThrowingOtherExceptionIsIgnored() throws Exception {

        // given
        toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.ThrowOtherException);

        // when
        toDoItem.add(otherToDoItem);

        // then
        // (no expectedExceptions setup, expect to continue)
        assertTrue(true);
    }

}