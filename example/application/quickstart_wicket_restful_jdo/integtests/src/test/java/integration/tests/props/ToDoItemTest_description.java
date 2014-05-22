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
package integration.tests.props;

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
import org.apache.isis.applib.services.eventbus.PropertyChangedEvent;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ToDoItemTest_description extends ToDoIntegTest {

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
        final List<ToDoItem> all = wrap(toDoItems).notYetComplete();
        toDoItem = wrap(all.get(0));
    }

    @After
    public void tearDown() throws Exception {
        toDoItemSubscriptions.reset();
    }

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
        assertThat(toDoItemSubscriptions.getSubscriberBehaviour(), is(ToDoItemSubscriptions.Behaviour.AcceptEvents));
        assertThat(toDoItem.getDescription(), is("Buy bread"));

        // when
        toDoItem.setDescription("Buy bread and butter");

        // then published and received
        @SuppressWarnings("unchecked")
        final PropertyChangedEvent<ToDoItem,String> ev = toDoItemSubscriptions.mostRecentlyReceivedEvent(PropertyChangedEvent.class);
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
        toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.RejectEventsWithRecoverableException);

        // then
        expectedExceptions.expect(RecoverableException.class);

        // when
        toDoItem.setDescription("Buy bread and butter");
    }


    @Test
    public void subscriberVetoesEventWithNonRecoverableException() throws Exception {

        // given
        toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.RejectEventsWithNonRecoverableException);

        // then
        expectedExceptions.expect(NonRecoverableException.class);

        // when
        toDoItem.setDescription("Buy bread and butter");
    }


    @Test
    public void subscriberThrowingOtherExceptionIsIgnored() throws Exception {

        // given
        toDoItemSubscriptions.subscriberBehaviour(ToDoItemSubscriptions.Behaviour.ThrowOtherException);

        // when
        toDoItem.setDescription("Buy bread and butter");

        // then
        // (no expectedExceptions setup, expect to continue)
        assertTrue(true);
    }


    private static String characters(final int n) {
        StringBuffer buf = new StringBuffer();
        for(int i=0; i<n; i++) {
            buf.append("a");
        }
        return buf.toString();
    }

}