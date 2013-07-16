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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import integration.tests.ToDoIntegTest;

import java.util.List;

import dom.todo.ToDoItem;
import dom.todo.ToDoItems;
import fixture.todo.ToDoItemsFixture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ToDoItemTest_dependencies_add extends ToDoIntegTest {

    private ToDoItem toDoItem;
    private ToDoItem otherToDoItem;
    

    @Before
    public void setUp() throws Exception {
        scenarioExecution().install(new ToDoItemsFixture());

        final List<ToDoItem> items = wrap(service(ToDoItems.class)).notYetComplete();
        toDoItem = wrap(items.get(0));
        otherToDoItem = items.get(1); // wrapping this seems to trip up cglib :-(
    }

    @After
    public void tearDown() throws Exception {
        unwrap(toDoItem).getDependencies().clear();
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
    }


    @Test
    public void cannotDependOnSelf() throws Exception {

        // when, then
        expectedExceptions.expectMessage("Can't set up a dependency to self");
        toDoItem.add(toDoItem);
    }

    @Test
    public void cannotAddDependencyIfComplete() throws Exception {

        // given
        unwrap(toDoItem).setComplete(true);
        
        // when, then
        expectedExceptions.expectMessage("Cannot add dependencies for items that are complete");
        toDoItem.add(otherToDoItem);
    }

}