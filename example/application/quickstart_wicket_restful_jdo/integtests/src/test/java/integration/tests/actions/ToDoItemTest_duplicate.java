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
import dom.todo.ToDoItems;
import fixture.todo.integtests.ToDoItemsIntegTestFixture;
import integration.tests.ToDoIntegTest;

import java.math.BigDecimal;
import java.util.List;
import javax.inject.Inject;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.applib.services.clock.ClockService;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ToDoItemTest_duplicate extends ToDoIntegTest {

    @Before
    public void setUpData() throws Exception {
        scenarioExecution().install(new ToDoItemsIntegTestFixture());
    }

    @Inject
    private ToDoItems toDoItems;
    @Inject
    private ClockService clockService;

    private ToDoItem toDoItem;
    private ToDoItem duplicateToDoItem;

    @Before
    public void setUp() throws Exception {
        final List<ToDoItem> all = wrap(toDoItems).notYetComplete();
        toDoItem = wrap(all.get(0));
    }

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