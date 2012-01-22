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

package org.apache.isis.examples.onlinedemo.objstore.dflt.items;

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.value.Date;
import org.apache.isis.examples.onlinedemo.dom.items.Category;
import org.apache.isis.examples.onlinedemo.dom.items.ToDoItem;
import org.apache.isis.examples.onlinedemo.dom.items.ToDoItems;

public class ToDoItemsDefault extends AbstractFactoryAndRepository implements ToDoItems {

    // {{ Id, iconName
    @Override
    public String getId() {
        return "toDoItems";
    }

    public String iconName() {
        return "ToDoItem";
    }

    // }}

    // {{ ToDosForToday (action)
    @Override
    public List<ToDoItem> toDosForToday() {
        return allMatches(ToDoItem.class, Filters.and(ToDoItem.thoseOwnedBy(currentUser()), ToDoItem.thoseDue()));
    }

    // }}

    // {{ NewToDo (action)
    @Override
    public ToDoItem newToDo(final String description, final Category category, final Date dueBy) {
        final ToDoItem toDoItem = newTransientInstance(ToDoItem.class);
        toDoItem.setDescription(description);
        toDoItem.setCategory(category);
        toDoItem.setDueBy(dueBy);
        toDoItem.setUserName(currentUser());
        persist(toDoItem);
        return toDoItem;
    }

    // }}

    // {{ AllToDos (action)
    @Override
    public List<ToDoItem> allToDos() {
        final String currentUser = currentUser();
        final List<ToDoItem> items = allMatches(ToDoItem.class, ToDoItem.thoseOwnedBy(currentUser));
        Collections.sort(items);
        return items;
    }

    // }}

    // {{ SimilarTo (action)

    @Override
    public List<ToDoItem> similarTo(final ToDoItem toDoItem) {
        return allMatches(ToDoItem.class, ToDoItem.thoseSimilarTo(toDoItem));
    }

    // }}

    // {{ RemoveCompleted (action)

    @Override
    public void removeCompleted() {
        final List<ToDoItem> complete = allMatches(ToDoItem.class, ToDoItem.thoseComplete());
        for (final ToDoItem toDoItem : complete) {
            getContainer().remove(toDoItem);
        }
        final int size = complete.size();
        getContainer().informUser("" + size + " item" + (size != 1 ? "s" : "") + " removed");
    }

    // }}

    // {{ helpers
    private String currentUser() {
        return getContainer().getUser().getName();
    }
    // }}

}
