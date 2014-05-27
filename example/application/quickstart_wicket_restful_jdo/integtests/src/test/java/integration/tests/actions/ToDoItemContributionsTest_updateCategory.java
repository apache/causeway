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

import dom.todo.ToDoItem;
import dom.todo.ToDoItem.Category;
import dom.todo.ToDoItem.Subcategory;
import dom.todo.ToDoItemContributions;
import dom.todo.ToDoItems;
import fixture.todo.integtests.ToDoItemsIntegTestFixture;
import integration.tests.ToDoIntegTest;

import java.util.List;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ToDoItemContributionsTest_updateCategory extends ToDoIntegTest {

    @Before
    public void setUpData() throws Exception {
        scenarioExecution().install(new ToDoItemsIntegTestFixture().withTracing());
    }

    @Inject
    private ToDoItems toDoItems;
    @Inject
    private ToDoItemContributions toDoItemContributions;

    private ToDoItemContributions toDoItemContributionsWrapper;
    private ToDoItem toDoItem;

    @Before
    public void setUp() throws Exception {
        final List<ToDoItem> all = toDoItems.notYetComplete();
        toDoItem = wrap(all.get(0));

        toDoItemContributionsWrapper = wrap(toDoItemContributions);
    }

    @Test
    public void happyCase() throws Exception {
        
        // when
        toDoItemContributionsWrapper.updateCategory(toDoItem, Category.Professional, Subcategory.Consulting);
        
        // then
        assertThat(toDoItem.getCategory(), is(Category.Professional));
        assertThat(toDoItem.getSubcategory(), is(Subcategory.Consulting));
        
        // when
        toDoItemContributionsWrapper.updateCategory(toDoItem, Category.Domestic, Subcategory.Chores);
        
        // then
        assertThat(toDoItem.getCategory(), is(Category.Domestic));
        assertThat(toDoItem.getSubcategory(), is(Subcategory.Chores));
    }


    @Test
    public void categoryCannotBeNull() throws Exception {
        
        // when, then
        expectedExceptions.expectMessage("'Category' is mandatory");
        toDoItemContributionsWrapper.updateCategory(toDoItem, null, Subcategory.Chores);
    }

    @Test
    public void subcategoryCanBeNull() throws Exception {
        
        // when, then
        toDoItemContributionsWrapper.updateCategory(toDoItem, Category.Professional, null);
    }
    
    @Test
    public void subcategoryMustBelongToCategory() throws Exception {
        
        // when, then
        expectedExceptions.expectMessage(containsString("Invalid subcategory"));
        toDoItemContributionsWrapper.updateCategory(toDoItem, Category.Professional, Subcategory.Chores);
    }
    
}