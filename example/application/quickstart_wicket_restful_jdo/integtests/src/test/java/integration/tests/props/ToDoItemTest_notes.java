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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.applib.services.eventbus.PropertyChangedEvent;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ToDoItemTest_notes extends ToDoIntegTest {

    @Before
    public void setUpData() throws Exception {
        scenarioExecution().install(new ToDoItemsIntegTestFixture());
    }

    private ToDoItem toDoItem;
    private ToDoItemSubscriptions toDoItemSubscriptions;

    @Before
    public void setUp() throws Exception {
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
        
        final String newNotes = "Lorem ipsum yada yada";
        
        // when
        toDoItem.setNotes(newNotes);
        
        // then
        assertThat(toDoItem.getNotes(), is(newNotes));

        // and then not published so not received
        @SuppressWarnings("unchecked")
        final PropertyChangedEvent<ToDoItem,String> ev = toDoItemSubscriptions.mostRecentlyReceivedEvent(PropertyChangedEvent.class);
        assertThat(ev, is(nullValue())); 
    }

    @Test
    public void canBeNull() throws Exception {
        
        // when
        toDoItem.setNotes((String)null);
        
        // then
        assertThat(toDoItem.getNotes(), is((String)null));
    }

    
}