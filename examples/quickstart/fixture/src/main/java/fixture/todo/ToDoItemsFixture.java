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
        createToDoItem("Buy milk");
        createToDoItem("Pick up laundry");
        createToDoItem("Buy stamps");
        createToDoItem("Write blog post");
        createToDoItem("Organize brown bag");
    }
    
    private ToDoItem createToDoItem(String description) {
        return toDoItems.newToDo(description);
    }

    
    private ToDoItems toDoItems;
    public void setToDoItemRepository(ToDoItems toDoItems) {
        this.toDoItems = toDoItems;
    }
    
}
