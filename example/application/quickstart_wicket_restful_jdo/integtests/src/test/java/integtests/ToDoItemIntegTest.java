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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import dom.todo.ToDoItem;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import org.apache.isis.applib.services.wrapper.DisabledException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ToDoItemIntegTest extends AbstractIntegTest {

    @Test
    public void t01_findNotYetCompleted() throws Exception {
        final List<ToDoItem> all = wrap(toDoItems).notYetComplete();
        assertThat(all.size(), is(5));
    }

    @Test
    public void t02_update() throws Exception {
        // given
        final List<ToDoItem> all = wrap(toDoItems).notYetComplete();
        final ToDoItem toDoItem = wrap(all.get(0));
        
        assertThat(toDoItem.getDescription(), is("Buy milk"));
        
        // when
        toDoItem.setDescription("Buy milk and butter");
        
        // then
        assertThat(toDoItem.getDescription(), is("Buy milk and butter"));
    }


    @Test
    public void t03_complete() throws Exception {
        // given
        final List<ToDoItem> all = wrap(toDoItems).notYetComplete();
        final ToDoItem toDoItem = wrap(all.get(0));
        
        assertThat(toDoItem.getDescription(), is("Buy milk and butter"));
        assertThat(toDoItem.isComplete(), is(false));
        
        // when
        toDoItem.completed();
        
        // then
        assertThat(toDoItem.isComplete(), is(true));

        final List<ToDoItem> all2 = wrap(toDoItems).notYetComplete();
        assertThat(all2.size(), is(4));
    }


    @Test
    public void t04_cannotCompleteAndObjectAlreadyCompleted() throws Exception {
        // given
        final List<ToDoItem> all = wrap(toDoItems).notYetComplete(); // 4 left
        final ToDoItem toDoItem = wrap(all.get(0));
        
        toDoItem.completed();
        
        // when, then should fail
        try {
            toDoItem.completed();
            fail("completed should have been disabled");
        } catch(DisabledException ex) {
            assertThat(ex.getMessage(), is("Already completed"));
        }
    }

}