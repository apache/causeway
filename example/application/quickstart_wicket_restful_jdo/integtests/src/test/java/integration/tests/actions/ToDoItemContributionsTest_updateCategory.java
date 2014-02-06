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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import integration.tests.ToDoIntegTest;

import java.util.List;

import dom.todo.ToDoItem;
import dom.todo.ToDoItemContributions;
import dom.todo.ToDoItems;
import dom.todo.ToDoItem.Category;
import dom.todo.ToDoItem.Subcategory;
import fixture.todo.ToDoItemsFixture;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.clock.Clock;

public class ToDoItemContributionsTest_updateCategory extends ToDoIntegTest {

    private ToDoItem toDoItem;
    private ToDoItemContributions toDoItemContributions;

    @Before
    public void setUp() throws Exception {
        scenarioExecution().install(new ToDoItemsFixture());

        final ToDoItems toDoItems = wrap(service(ToDoItems.class));
        toDoItemContributions = wrap(service(ToDoItemContributions.class));
        final List<ToDoItem> all = toDoItems.notYetComplete();
        toDoItem = wrap(all.get(0));
    }

    @Test
    public void happyCase() throws Exception {
        
        // when
        toDoItemContributions.updateCategory(toDoItem, Category.Professional, Subcategory.Consulting);
        
        // then
        assertThat(toDoItem.getCategory(), is(Category.Professional));
        assertThat(toDoItem.getSubcategory(), is(Subcategory.Consulting));
        
        // when
        toDoItemContributions.updateCategory(toDoItem, Category.Domestic, Subcategory.Chores);
        
        // then
        assertThat(toDoItem.getCategory(), is(Category.Domestic));
        assertThat(toDoItem.getSubcategory(), is(Subcategory.Chores));
    }


    @Test
    public void categoryCannotBeNull() throws Exception {
        
        // when, then
        expectedExceptions.expectMessage("Category is mandatory");
        toDoItemContributions.updateCategory(toDoItem, null, Subcategory.Chores);
    }

    @Test
    public void subcategoryCanBeNull() throws Exception {
        
        // when, then
        toDoItemContributions.updateCategory(toDoItem, Category.Professional, null);
    }
    
    @Test
    public void subcategoryMustBelongToCategory() throws Exception {
        
        // when, then
        expectedExceptions.expectMessage(containsString("Invalid subcategory"));
        toDoItemContributions.updateCategory(toDoItem, Category.Professional, Subcategory.Chores);
    }
    
}