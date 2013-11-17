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

import org.junit.Before;
import org.junit.Test;

public class ToDoItemTest_description extends ToDoIntegTest {

    private ToDoItem toDoItem;

    @Before
    public void setUp() throws Exception {
        scenarioExecution().install(new ToDoItemsFixture());

        final List<ToDoItem> all = wrap(service(ToDoItems.class)).notYetComplete();
        toDoItem = wrap(all.get(0));
    }

    @Test
    public void happyCase() throws Exception {
        
        // given
        assertThat(toDoItem.getDescription(), is("Buy bread"));
        
        // when
        toDoItem.setDescription("Buy bread and butter");
        
        // then
        assertThat(toDoItem.getDescription(), is("Buy bread and butter"));
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

    @Test
    public void onlyJustShortEnough() throws Exception {
        
        // when, then
        toDoItem.setDescription(characters(100));
    }

    @Test
    public void tooLong() throws Exception {
        
        // when, then
        expectedExceptions.expectMessage("The value proposed exceeds the maximum length of 100");
        toDoItem.setDescription(characters(101));
    }
    
    private static String characters(final int n) {
        StringBuffer buf = new StringBuffer();
        for(int i=0; i<n; i++) {
            buf.append("a");
        }
        return buf.toString();
    }

}