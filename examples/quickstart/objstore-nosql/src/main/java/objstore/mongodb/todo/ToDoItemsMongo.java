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

package objstore.mongodb.todo;

import java.util.List;

import com.google.common.base.Objects;

import dom.todo.ToDoItem;
import dom.todo.ToDoItem.Category;
import dom.todo.ToDoItems;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.filter.Filter;

public class ToDoItemsMongo extends AbstractFactoryAndRepository implements ToDoItems {

    // {{ Id, iconName
    @Override
    public String getId() {
        return "toDoItems";
    }

    public String iconName() {
        return "ToDoItem";
    }

    // }}

    @Override
    public List<ToDoItem> notYetDone() {
        final String userName = getContainer().getUser().getName();
        return allMatches(ToDoItem.class, new Filter<ToDoItem>() {
            @Override
            public boolean accept(final ToDoItem t) {
                return Objects.equal(t.getOwnedBy(), userName) && !t.getDone();
            }
        });
    }

    // {{ NewToDo  (action)
    @Override
    public ToDoItem newToDo(final String description, Category category) {
        final String ownedBy = getContainer().getUser().getName();
        return newToDo(description, category, ownedBy);
    }
    // }}

    // {{ NewToDo  (hidden)
    @Override
    public ToDoItem newToDo(final String description, Category category, String ownedBy) {
        final ToDoItem toDoItem = newTransientInstance(ToDoItem.class);
        toDoItem.setDescription(description);
        toDoItem.setCategory(category);
        toDoItem.setOwnedBy(ownedBy);
        persist(toDoItem);
        return toDoItem;
    }
    // }}

    // {{ SimilarTo (action)
    @Override
    public List<ToDoItem> similarTo(final ToDoItem toDoItem) {
        return allMatches(ToDoItem.class, new Filter<ToDoItem>() {
            @Override
            public boolean accept(ToDoItem t) {
                return t != toDoItem && Objects.equal(toDoItem.getCategory(), t.getCategory()) && Objects.equal(toDoItem.getOwnedBy(), t.getOwnedBy());
            }
        });
    }
    // }}


}
