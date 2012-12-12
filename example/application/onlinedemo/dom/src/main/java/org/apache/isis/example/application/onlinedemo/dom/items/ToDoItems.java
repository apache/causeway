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

package org.apache.isis.example.application.onlinedemo.dom.items;

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.value.Date;

/**
 * A repository for {@link ToDoItem}s.
 * 
 * <p>
 * The implementation depends on the configured object store.
 */
@Named("ToDos")
public class ToDoItems extends AbstractFactoryAndRepository {

    
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
    @ActionSemantics(Of.SAFE) 
    @MemberOrder(sequence = "1") // order in the UI
    public List<ToDoItem> toDosForToday() {
        return allMatches(ToDoItem.class, Filters.and(ToDoItem.thoseOwnedBy(currentUser()), ToDoItem.thoseDue()));
    }

    // }}

    // {{ NewToDo (action)
    @MemberOrder(sequence = "2")
    public ToDoItem newToDo(@Named("Description") String description, Category category, @Named("Due by") @Optional Date dueBy) {
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
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "3")
    public List<ToDoItem> allToDos() {
        final String currentUser = currentUser();
        final List<ToDoItem> items = allMatches(ToDoItem.class, ToDoItem.thoseOwnedBy(currentUser));
        Collections.sort(items);
        return items;
    }

    // }}

    // {{ SimilarTo (action)
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "4")
    public List<ToDoItem> similarTo(ToDoItem toDoItem) {
        return allMatches(ToDoItem.class, ToDoItem.thoseSimilarTo(toDoItem));
    }

    // }}

    // {{ RemoveCompleted (action)
    @ActionSemantics(Of.IDEMPOTENT) // same post-conditions
    @MemberOrder(sequence = "5")
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
