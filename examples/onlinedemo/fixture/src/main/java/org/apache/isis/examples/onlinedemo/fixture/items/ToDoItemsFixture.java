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


package org.apache.isis.examples.onlinedemo.fixture.items;


import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.examples.onlinedemo.dom.items.Categories;
import org.apache.isis.examples.onlinedemo.dom.items.Category;
import org.apache.isis.examples.onlinedemo.dom.items.ToDoItem;
import org.apache.isis.examples.onlinedemo.dom.items.ToDoItems;


public class ToDoItemsFixture extends AbstractFixture {

    @Override
    public void install() {
        Category domesticCategory = createCategory("Domestic");
        Category professionalCategory = createCategory("Professional");
        
        createToDoItem("Buy milk", domesticCategory);
        createToDoItem("Pick up laundry", domesticCategory);
        createToDoItem("Buy stamps", domesticCategory);
        createToDoItem("Write blog post", professionalCategory);
        createToDoItem("Organize brown bag", professionalCategory);
    }

    private Category createCategory(String description) {
        return categories.newCategory(description);
    }

    private ToDoItem createToDoItem(String description, Category category) {
        return toDoItems.newToDo(description, category);
    }


    // {{ injected: Categories
    private Categories categories;

    public void setCategories(final Categories categories) {
        this.categories = categories;
    }
    // }}


    // {{ injected: ToDoItems
    private ToDoItems toDoItems;
    public void setToDoItems(ToDoItems toDoItems) {
        this.toDoItems = toDoItems;
    }
    // }}
    
}
