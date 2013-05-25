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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ToDoItem_description extends AbstractIntegTest {

    private ToDoItem toDoItem;
    private String description;

    @Before
    public void setUp() throws Exception {
        // given
        final List<ToDoItem> all = wrap(toDoItems).notYetComplete();
        toDoItem = wrap(all.get(0));

        // to reset after
        description = toDoItem.getDescription();
    }

    @After
    public void tearDown() throws Exception {
        unwrap(toDoItem).setDescription(description);
    }
    
    @Test
    public void happyCase() throws Exception {
        
        // given
        assertThat(toDoItem.getDescription(), is("Buy milk"));
        
        // when
        toDoItem.setDescription("Buy milk and butter");
        
        // then
        assertThat(toDoItem.getDescription(), is("Buy milk and butter"));
    }


    @Test
    public void failsRegex() throws Exception {
        
        // when
        expectedExceptions.expectMessage("Doesn't match pattern");
        toDoItem.setDescription("exclamation marks are not allowed!!!");
    }

    @Test
    public void cannotBeNull() throws Exception {
        
        // when, then
        expectedExceptions.expectMessage("Mandatory");
        toDoItem.setDescription(null);
    }

}