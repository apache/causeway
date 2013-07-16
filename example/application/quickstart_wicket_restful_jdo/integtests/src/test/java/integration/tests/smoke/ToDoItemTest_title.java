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
package integration.tests.smoke;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import integration.tests.ToDoIntegTest;

import java.util.List;

import dom.todo.ToDoItem;
import dom.todo.ToDoItems;
import fixture.todo.ToDoItemsFixture;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.clock.Clock;

public class ToDoItemTest_title extends ToDoIntegTest {

    private ToDoItem toDoItem;
    private LocalDate dueBy;


    @Before
    public void setUp() throws Exception {

        scenarioExecution().install(new ToDoItemsFixture());
        
        final List<ToDoItem> all = wrap(service(ToDoItems.class)).notYetComplete();
        toDoItem = wrap(all.get(0));

        dueBy = toDoItem.getDueBy();
    }

    
    @Test
    public void includesDescription() throws Exception {

        // given
        assertThat(container().titleOf(toDoItem), containsString("Buy bread due by"));

        // when
        unwrap(toDoItem).setDescription("Buy bread and butter");
        
        // then
        assertThat(container().titleOf(toDoItem), containsString("Buy bread and butter due by"));
    }

    @Test
    public void includesDueDateIfAny() throws Exception {

        // given
        assertThat(container().titleOf(toDoItem), containsString("due by " + dueBy.toString("yyyy-MM-dd")));

        // when
        final LocalDate fiveDaysFromNow = Clock.getTimeAsLocalDate().plusDays(5);
        unwrap(toDoItem).setDueBy(fiveDaysFromNow);

        // then
        assertThat(container().titleOf(toDoItem), containsString("due by " + fiveDaysFromNow.toString("yyyy-MM-dd")));
    }


    @Test
    public void ignoresDueDateIfNone() throws Exception {

        // when
        // (since wrapped, will call clearDueBy) 
        toDoItem.setDueBy(null);

        // then
        assertThat(container().titleOf(toDoItem), not(containsString("due by")));
    }

    @Test
    public void usesWhetherCompleted() throws Exception {

        // given
        assertThat(container().titleOf(toDoItem), not(containsString("Completed!")));

        // when
        toDoItem.completed();

        // then
        assertThat(container().titleOf(toDoItem), not(containsString("due by")));
        assertThat(container().titleOf(toDoItem), containsString("Buy bread - Completed!"));
    }

}