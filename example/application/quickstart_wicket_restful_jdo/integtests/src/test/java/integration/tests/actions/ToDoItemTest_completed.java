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
package integration.tests.actions;

import dom.todo.ToDoItem;
import dom.todo.ToDoItemSubscriptions;
import dom.todo.ToDoItems;
import fixture.todo.integtests.ToDoItemsIntegTestFixture;
import integration.tests.ToDoIntegTest;

import java.util.EventObject;
import java.util.List;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.RecoverableException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ToDoItemTest_completed extends ToDoIntegTest {

    @Before
    public void setUpData() throws Exception {
        scenarioExecution().install(new ToDoItemsIntegTestFixture());
    }

    @Inject
    private ToDoItems toDoItems;
    @Inject
    private ToDoItemSubscriptions toDoItemSubscriptions;

    private ToDoItem toDoItem;

    @Before
    public void setUp() throws Exception {
        final List<ToDoItem> all = toDoItems.notYetComplete();
        toDoItem = wrap(all.get(0));
    }

    @After
    public void tearDown() throws Exception {
        toDoItemSubscriptions.reset();
    }

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
    public void subscriberReceivesEvent() throws Exception {

        // given
        assertThat(toDoItemSubscriptions.getSubscriberBehaviour(), is(ToDoItemSubscriptions.Behaviour.AcceptEvents));
        assertThat(toDoItem.isComplete(), is(false));

        // when
        toDoItem.completed();

        // then
        assertThat(toDoItem.isComplete(), is(true));

        // and then
        final ToDoItem.CompletedEvent ev = toDoItemSubscriptions.mostRecentlyReceivedEvent(ToDoItem.CompletedEvent.class);
        assertThat(ev, is(not(nullValue())));

        ToDoItem source = ev.getSource();
        assertThat(source, is(equalTo(unwrap(toDoItem))));
        assertThat(ev.getIdentifier().getMemberName(), is("completed"));
    }

    @Test
    public void subscriberVetoesEventWithRecoverableException() throws Exception {

        // given
        toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.RejectEventsWithRecoverableException);

        // then
        expectedExceptions.expect(RecoverableException.class);

        // when
        toDoItem.completed();
    }

    @Test
    public void subscriberVetoesEventWithNonRecoverableException() throws Exception {

        // given
        toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.RejectEventsWithNonRecoverableException);

        // then
        expectedExceptions.expect(NonRecoverableException.class);

        // when
        toDoItem.completed();
    }

    @Test
    public void subscriberThrowingOtherExceptionIsIgnored() throws Exception {

        // given
        toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.ThrowOtherException);

        // when
        toDoItem.completed();

        // then
        // (no expectedExceptions setup, expect to continue)
        assertTrue(true);
    }


}