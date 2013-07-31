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
import dom.todo.ToDoItems;
import fixture.todo.ToDoItemsFixture;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.clock.Clock;

public class ToDoItemTest_dueBy extends ToDoIntegTest {

    private ToDoItem toDoItem;

    @Before
    public void setUp() throws Exception {
        scenarioExecution().install(new ToDoItemsFixture());

        final List<ToDoItem> all = wrap(service(ToDoItems.class)).notYetComplete();
        toDoItem = wrap(all.get(0));
    }

    @Test
    public void happyCase() throws Exception {
        
        // when
        final LocalDate fiveDaysFromNow = Clock.getTimeAsLocalDate().plusDays(5);
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
        
        final LocalDate nowAsLocalDate = Clock.getTimeAsLocalDate();
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