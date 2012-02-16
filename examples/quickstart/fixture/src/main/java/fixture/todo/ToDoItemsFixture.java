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
import dom.todo.ToDoItems;

import org.apache.isis.applib.fixtures.AbstractFixture;

public class ToDoItemsFixture extends AbstractFixture {

    @Override
    public void install() {
        createToDoItem("Buy milk", "Domestic", "sven");
        createToDoItem("Pick up laundry", "Domestic", "sven");
        createToDoItem("Buy stamps", "Domestic", "sven");
        createToDoItem("Write blog post", "Professional", "sven");
        createToDoItem("Organize brown bag", "Professional", "sven");
        
        createToDoItem("Book car in for service", "Domestic", "dick");
        createToDoItem("Buy birthday present for sven", "Domestic", "dick");
        createToDoItem("Write presentation for conference", "Professional", "dick");

        createToDoItem("Write thank you notes", "Domestic", "bob");
        createToDoItem("Look into solar panels", "Domestic", "bob");

        createToDoItem("Pitch book idea to publisher", "Professional", "joe");
    }

    private ToDoItem createToDoItem(final String description, String category, String ownedBy) {
        return toDoItems.newToDo(description, category, ownedBy);
    }

    private ToDoItems toDoItems;

    public void setToDoItemRepository(final ToDoItems toDoItems) {
        this.toDoItems = toDoItems;
    }

}
