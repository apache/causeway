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
package integtests.props;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import integtests.AbstractIntegTest;

import java.util.List;

import dom.todo.ToDoItem;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.clock.Clock;

public class ToDoItem_dueBy extends AbstractIntegTest {

    private ToDoItem toDoItem;
    private LocalDate dueBy;

    @Before
    public void setUp() throws Exception {
        // given
        final List<ToDoItem> all = wrap(toDoItems).notYetComplete();
        toDoItem = wrap(all.get(0));

        // to reset after
        dueBy = toDoItem.getDueBy();
    }

    @After
    public void tearDown() throws Exception {
        unwrap(toDoItem).setDueBy(dueBy);
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
        
        final LocalDate sixDaysAgo = Clock.getTimeAsLocalDate().plusDays(-6);

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