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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import integration.tests.ToDoIntegTest;

import java.util.List;

import dom.todo.ToDoItem;
import dom.todo.ToDoItemContributions;
import dom.todo.ToDoItems;
import fixture.todo.ToDoItemsFixture;

import org.junit.Before;
import org.junit.Test;

public class ToDoItemContributionsTest_priority extends ToDoIntegTest {

    private ToDoItemContributions toDoItemContributions;
    
    private List<ToDoItem> notYetComplete;

    @Before
    public void setUp() throws Exception {
        scenarioExecution().install(new ToDoItemsFixture());

        final ToDoItems toDoItems = wrap(service(ToDoItems.class));
        toDoItemContributions = service(ToDoItemContributions.class);
        notYetComplete = toDoItems.notYetComplete();
    }

    @Test
    public void happyCase() throws Exception {
        assertPriority(0, 1);
        assertPriority(1, 2);
        assertPriority(2, 4);
        assertPriority(3, 6);
        assertPriority(4, 5);
        assertPriority(5, 7);
        assertPriority(6, 9);
        assertPriority(7, 8);
        assertPriority(8, 3);
        assertPriority(9, 10);
    }

    private void assertPriority(final int n, final int priority) {
        assertThat(toDoItemContributions.relativePriority(notYetComplete.get(n)), is(Integer.valueOf(priority)));
    }
    
}