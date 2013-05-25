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
package integtests;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.List;

import dom.todo.ToDoItem;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.clock.Clock;

public class ToDoItem_title extends AbstractIntegTest {

    private ToDoItem toDoItem;
    private String description;
    private boolean isComplete;
    private LocalDate dueBy;

    @Before
    public void setUp() throws Exception {
        // given
        final List<ToDoItem> all = wrap(toDoItems).notYetComplete();
        toDoItem = wrap(all.get(0));

        // to reset after
        description = toDoItem.getDescription();
        isComplete = toDoItem.isComplete();
        dueBy = toDoItem.getDueBy();
    }

    @After
    public void tearDown() throws Exception {
        unwrap(toDoItem).setDescription(description);
        unwrap(toDoItem).setComplete(isComplete);
        unwrap(toDoItem).setDueBy(dueBy);
    }
    
    
    @Test
    public void includesDescription() throws Exception {

        // given
        assertThat(container.titleOf(toDoItem), containsString("Buy milk due by"));

        // when
        unwrap(toDoItem).setDescription("Buy milk and butter");
        
        // then
        assertThat(container.titleOf(toDoItem), containsString("Buy milk and butter due by"));
    }

    @Test
    public void includesDueDateIfAny() throws Exception {

        // given
        assertThat(container.titleOf(toDoItem), containsString("due by " + dueBy.toString("yyyy-MM-dd")));

        // when
        final LocalDate fiveDaysFromNow = Clock.getTimeAsLocalDate().plusDays(5);
        unwrap(toDoItem).setDueBy(fiveDaysFromNow);

        // then
        assertThat(container.titleOf(toDoItem), containsString("due by " + fiveDaysFromNow.toString("yyyy-MM-dd")));
    }


    @Test
    public void ignoresDueDateIfNone() throws Exception {

        // when
        // (since wrapped, will call clearDueBy) 
        toDoItem.setDueBy(null);

        // then
        assertThat(container.titleOf(toDoItem), not(containsString("due by")));
    }

    @Test
    public void usesWhetherCompleted() throws Exception {

        // given
        assertThat(container.titleOf(toDoItem), not(containsString("Completed!")));

        // when
        toDoItem.completed();

        // then
        assertThat(container.titleOf(toDoItem), not(containsString("due by")));
        assertThat(container.titleOf(toDoItem), containsString("Buy milk - Completed!"));
    }

}