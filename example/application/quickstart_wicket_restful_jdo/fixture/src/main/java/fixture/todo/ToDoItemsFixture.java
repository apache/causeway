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

import java.math.BigDecimal;
import java.util.List;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.fixtures.AbstractFixture;
import org.joda.time.LocalDate;

import dom.todo.ToDoItem;
import dom.todo.ToDoItem.Category;
import dom.todo.ToDoItems;
import dom.todo.ToDoItems.NotifyUserIfNone;

public class ToDoItemsFixture extends AbstractFixture {

    @Override
    public void install() {

        removeAllToDosForCurrentUser();

        installFor(getContainer().getUser().getName());
        
        getContainer().flush();
    }

    public void installFor(String user) {

        removeAllToDosFor(user);

        createToDoItemForUser("Buy milk", Category.Domestic, user, daysFromToday(0), new BigDecimal("1.50"));
        createToDoItemForUser("Buy stamps", Category.Domestic, user, daysFromToday(0), new BigDecimal("10.00"));
        createToDoItemForUser("Pick up laundry", Category.Other, user, daysFromToday(6), new BigDecimal("7.50"));
        createToDoItemForUser("Write blog post", Category.Professional, user, null, null);
        createToDoItemForUser("Organize brown bag", Category.Professional, user, daysFromToday(14), null);

        getContainer().flush();
    }

    // {{ helpers
    private void removeAllToDosForCurrentUser() {
        
        final List<ToDoItem> allToDos = toDoItems.allToDos(NotifyUserIfNone.NO);
        for (final ToDoItem toDoItem : allToDos) {
            getContainer().remove(toDoItem);
        }
    }

    private void removeAllToDosFor(String user) {
        final List<ToDoItem> items = allMatches(ToDoItem.class, ToDoItem.thoseOwnedBy(user));
        for (final ToDoItem toDoItem : items) {
            getContainer().remove(toDoItem);
        }
    }

    private ToDoItem createToDoItemForUser(final String description, final Category category, String user, final LocalDate dueBy, final BigDecimal cost) {
        return toDoItems.newToDo(description, category, user, dueBy, cost);
    }

    private static LocalDate daysFromToday(final int i) {
        final LocalDate date = new LocalDate(Clock.getTimeAsDateTime());
        return date.plusDays(i);
    }

    // }}

    // {{ injected: ToDoItems
    private ToDoItems toDoItems;

    public void setToDoItems(final ToDoItems toDoItems) {
        this.toDoItems = toDoItems;
    }
    // }}
    

}
