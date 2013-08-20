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
package integration.tests.repo;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import integration.tests.ToDoIntegTest;
import dom.todo.ToDoItem;
import dom.todo.ToDoItems;
import dom.todo.ToDoItem.Category;
import dom.todo.ToDoItem.Subcategory;

import org.junit.Test;

public class ToDoItemsTest_newToDo_and_delete extends ToDoIntegTest {

    @Test
    public void happyCase() throws Exception {
        
        // given
        int size = wrap(service(ToDoItems.class)).notYetComplete().size();
        
        // when
        final ToDoItem newToDo = wrap(service(ToDoItems.class)).newToDo("new todo", Category.Professional, Subcategory.OpenSource, null, null);

        // then
        assertThat(newToDo.getDescription(), is("new todo"));
        assertThat(newToDo.getCategory(), is(Category.Professional));
        assertThat(wrap(service(ToDoItems.class)).notYetComplete().size(), is(size+1));
        
        // when
        newToDo.delete();

        // then
        assertThat(wrap(service(ToDoItems.class)).notYetComplete().size(), is(size));
    }

}