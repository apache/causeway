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

import java.math.BigDecimal;
import java.util.List;

import dom.todo.ToDoItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ToDoItem_notes extends AbstractIntegTest {

    private ToDoItem toDoItem;
    private String notes;

    @Before
    public void setUp() throws Exception {
        // given
        final List<ToDoItem> all = wrap(toDoItems).notYetComplete();
        toDoItem = wrap(all.get(0));

        // to reset after
        notes = toDoItem.getNotes();
    }

    @After
    public void tearDown() throws Exception {
        unwrap(toDoItem).setNotes(notes);
    }

    @Test
    public void happyCase() throws Exception {
        
        final String newNotes = "Lorem ipsum yada yada";
        
        // when
        toDoItem.setNotes(newNotes);
        
        // then
        assertThat(toDoItem.getNotes(), is(newNotes));
    }

    @Test
    public void canBeNull() throws Exception {
        
        // when
        toDoItem.setNotes((String)null);
        
        // then
        assertThat(toDoItem.getNotes(), is((String)null));
    }

    
}