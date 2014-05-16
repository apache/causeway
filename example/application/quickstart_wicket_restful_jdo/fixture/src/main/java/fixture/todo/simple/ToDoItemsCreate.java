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
package fixture.todo.simple;

import java.math.BigDecimal;

import dom.todo.ToDoItem;
import dom.todo.ToDoItem.Category;
import dom.todo.ToDoItem.Subcategory;
import dom.todo.ToDoItems;

import org.joda.time.LocalDate;

import org.apache.isis.applib.fixturescripts.FixtureResultList;
import org.apache.isis.applib.fixturescripts.SimpleFixtureScript;
import org.apache.isis.applib.fixturescripts.FixtureScript.ExecutionContext;

public class ToDoItemsCreate extends SimpleFixtureScript {

    //region > factory methods & constructor
    public static ToDoItemsCreate forCurrent() {
        return new ToDoItemsCreate(null);
    }

    public static ToDoItemsCreate forUser(final String user) {
        return new ToDoItemsCreate(user);
    }

    private final String user;
    
    private ToDoItemsCreate(final String user) {
        super(null, Util.localNameFor("create", user));
        this.user = user;
    }
    //endregion

    //region > execute
    @Override
    protected void execute(ExecutionContext executionContext) {
        final String ownedBy = Util.coalesce(user, executionContext.getParameters(), getContainer().getUser().getName());
        installFor(ownedBy, executionContext);
        getContainer().flush();
    }

    private void installFor(final String user, final ExecutionContext executionContext) {

        createToDoItemForUser("Buy milk", Category.Domestic, Subcategory.Shopping, user, Util.daysFromToday(0), new BigDecimal("0.75"), executionContext);
        createToDoItemForUser("Buy bread", Category.Domestic, Subcategory.Shopping, user, Util.daysFromToday(0), new BigDecimal("1.75"), executionContext);
        createToDoItemForUser("Buy stamps", Category.Domestic, Subcategory.Shopping, user, Util.daysFromToday(0), new BigDecimal("10.00"), executionContext);
        createToDoItemForUser("Pick up laundry", Category.Domestic, Subcategory.Chores, user, Util.daysFromToday(6), new BigDecimal("7.50"), executionContext);
        createToDoItemForUser("Mow lawn", Category.Domestic, Subcategory.Garden, user, Util.daysFromToday(6), null, executionContext);
        createToDoItemForUser("Vacuum house", Category.Domestic, Subcategory.Housework, user, Util.daysFromToday(3), null, executionContext);
        createToDoItemForUser("Sharpen knives", Category.Domestic, Subcategory.Chores, user, Util.daysFromToday(14), null, executionContext);

        createToDoItemForUser("Write to penpal", Category.Other, Subcategory.Other, user, null, null, executionContext);

        createToDoItemForUser("Write blog post", Category.Professional, Subcategory.Marketing, user, Util.daysFromToday(7), null, executionContext);
        createToDoItemForUser("Organize brown bag", Category.Professional, Subcategory.Consulting, user, Util.daysFromToday(14), null, executionContext);
        createToDoItemForUser("Submit conference session", Category.Professional, Subcategory.Education, user, Util.daysFromToday(21), null, executionContext);
        createToDoItemForUser("Stage Isis release", Category.Professional, Subcategory.OpenSource, user, null, null, executionContext);

        getContainer().flush();
    }

    private ToDoItem createToDoItemForUser(
            final String description, 
            final Category category, Subcategory subcategory, 
            final String user, 
            final LocalDate dueBy, 
            final BigDecimal cost, 
            final ExecutionContext executionContext) {
        ToDoItem newToDo = toDoItems.newToDo(description, category, subcategory, user, dueBy, cost);
        return executionContext.add(this, newToDo);
    }
    //endregion

    //region > injected services
    @javax.inject.Inject
    private ToDoItems toDoItems;
    //endregion


}