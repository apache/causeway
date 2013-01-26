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

package fixture.todo;

import dom.todo.ToDoItem;
import dom.todo.ToDoItem.Category;
import dom.todo.ToDoItems;

import org.apache.isis.applib.fixtures.AbstractFixture;

public class ToDoItemsFixture extends AbstractFixture {

    @Override
    public void install() {
        createFiveFor("sven");
        createThreeFor("dick");
        createTwoFor("bob");
        createOneFor("joe");

        // for exploration user
        createFiveFor("exploration");
    }

    private void createFiveFor(String ownedBy) {
        createToDoItem("Buy milk", Category.Domestic, ownedBy);
        createToDoItem("Pick up laundry", Category.Domestic, ownedBy);
        createToDoItem("Buy stamps", Category.Domestic, ownedBy);
        createToDoItem("Write blog post", Category.Professional, ownedBy);
        createToDoItem("Organize brown bag", Category.Professional, ownedBy);
    }

    private void createThreeFor(String ownedBy) {
        createToDoItem("Book car in for service", Category.Domestic, ownedBy);
        createToDoItem("Buy birthday present for sven", Category.Domestic, ownedBy);
        createToDoItem("Write presentation for conference", Category.Professional, ownedBy);
    }

    private void createTwoFor(String ownedBy) {
        createToDoItem("Write thank you notes", Category.Domestic, ownedBy);
        createToDoItem("Look into solar panels", Category.Domestic, ownedBy);
    }

    private void createOneFor(String ownedBy) {
        createToDoItem("Pitch book idea to publisher", Category.Professional, ownedBy);
    }

    private ToDoItem createToDoItem(final String description, Category category, String ownedBy) {
        return toDoItems.newToDo(description, category, ownedBy);
    }

    private ToDoItems toDoItems;

    public void setToDoItems(final ToDoItems toDoItems) {
        this.toDoItems = toDoItems;
    }

}
