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

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.examples.onlinedemo.dom.demo.DemoFixtures;
import org.apache.isis.examples.onlinedemo.dom.items.Categories;
import org.apache.isis.examples.onlinedemo.dom.items.ToDoItem;
import org.apache.isis.examples.onlinedemo.dom.items.ToDoItems;

public class DemoFixturesDefault extends AbstractFactoryAndRepository implements DemoFixtures {

    // {{ Id, iconName
    @Override
    public String getId() {
        return "demo";
    }

    public String iconName() {
        return "demo";
    }

    // }}

    @Override
    public List<ToDoItem> resetFixtures() {
        final ToDoItemsFixture fixture = new ToDoItemsFixture();
        injectServicesInto(fixture);

        fixture.install();

        return toDoItems.allToDos();
    }

    // {{ helpers
    private void injectServicesInto(final ToDoItemsFixture fixture) {
        fixture.setCategories(categories);
        fixture.setContainer(getContainer());
        fixture.setToDoItems(toDoItems);
    }

    // }}

    // {{ injected: Categories
    private Categories categories;

    public void setCategories(final Categories categories) {
        this.categories = categories;
    }

    // }}

    // {{ injected: ToDoItems
    private ToDoItems toDoItems;

    public void setToDoItems(final ToDoItems toDoItems) {
        this.toDoItems = toDoItems;
    }
    // }}

}
