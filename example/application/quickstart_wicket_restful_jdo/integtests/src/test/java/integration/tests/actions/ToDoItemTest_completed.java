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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import integration.tests.ToDoIntegTest;

import java.util.EventObject;
import java.util.List;

import dom.todo.ToDoItem;
import dom.todo.ToDoItemSubscriptions;
import dom.todo.ToDoItems;
import fixture.todo.integtests.ToDoItemsIntegTestFixture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ToDoItemTest_completed extends ToDoIntegTest {

    private ToDoItem toDoItem;
    private ToDoItemSubscriptions toDoItemSubscriptions;

    @Before
    public void setUp() throws Exception {
        scenarioExecution().install(new ToDoItemsIntegTestFixture());

        final List<ToDoItem> all = wrap(service(ToDoItems.class)).notYetComplete();
        toDoItem = wrap(all.get(0));
        
        toDoItemSubscriptions = service(ToDoItemSubscriptions.class);
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
        
        // and then
        final ToDoItem.CompletedEvent ev = toDoItemSubscriptions.mostRecentlyReceivedEvent(ToDoItem.CompletedEvent.class);
        assertThat(ev, is(not(nullValue()))); 
        
        ToDoItem source = ev.getSource();
        assertThat(source, is(equalTo(unwrap(toDoItem))));
        assertThat(ev.getIdentifier().getMemberName(), is("completed"));
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

}