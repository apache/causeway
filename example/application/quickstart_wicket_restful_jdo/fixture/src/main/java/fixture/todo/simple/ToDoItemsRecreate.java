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

import dom.todo.ToDoItem;
import dom.todo.ToDoItem.Category;
import dom.todo.ToDoItem.Subcategory;
import dom.todo.ToDoItems;

import java.math.BigDecimal;
import org.joda.time.LocalDate;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.services.clock.ClockService;

public class ToDoItemsRecreate extends FixtureScript {

    //region > constructor
    private final String user;

    /**
     * @param user - if null then executes for the current user or will use any {@link #run(String) parameters} provided when run.
     */
    public ToDoItemsRecreate(final String user) {
        super(null, Util.localNameFor("create", user));
        this.user = user;
    }
    //endregion

    //region > execute
    @Override
    protected void execute(ExecutionContext executionContext) {
        final String ownedBy = Util.coalesce(user, executionContext.getParameters(), getContainer().getUser().getName());

        // prereqs
        execute(new ToDoItemsDelete(null), executionContext);

        // this fixture
        createToDoItem("Buy milk", Category.Domestic, Subcategory.Shopping, ownedBy, nowPlusDays(0), new BigDecimal("0.75"), executionContext);
        createToDoItem("Buy bread", Category.Domestic, Subcategory.Shopping, ownedBy, nowPlusDays(0), new BigDecimal("1.75"), executionContext);
        createToDoItem("Buy stamps", Category.Domestic, Subcategory.Shopping, ownedBy, nowPlusDays(0), new BigDecimal("10.00"), executionContext);
        createToDoItem("Pick up laundry", Category.Domestic, Subcategory.Chores, ownedBy, nowPlusDays(6), new BigDecimal("7.50"), executionContext);
        createToDoItem("Mow lawn", Category.Domestic, Subcategory.Garden, ownedBy, nowPlusDays(6), null, executionContext);
        createToDoItem("Vacuum house", Category.Domestic, Subcategory.Housework, ownedBy, nowPlusDays(3), null, executionContext);
        createToDoItem("Sharpen knives", Category.Domestic, Subcategory.Chores, ownedBy, nowPlusDays(14), null, executionContext);

        createToDoItem("Write to penpal", Category.Other, Subcategory.Other, ownedBy, null, null, executionContext);

        createToDoItem("Write blog post", Category.Professional, Subcategory.Marketing, ownedBy, nowPlusDays(7), null, executionContext);
        createToDoItem("Organize brown bag", Category.Professional, Subcategory.Consulting, ownedBy, nowPlusDays(14), null, executionContext);
        createToDoItem("Submit conference session", Category.Professional, Subcategory.Education, ownedBy, nowPlusDays(21), null, executionContext);
        createToDoItem("Stage Isis release", Category.Professional, Subcategory.OpenSource, ownedBy, null, null, executionContext);
    }

    private ToDoItem createToDoItem(
            final String description,
            final Category category, Subcategory subcategory,
            final String user,
            final LocalDate dueBy,
            final BigDecimal cost,
            final ExecutionContext executionContext) {
        ToDoItem newToDo = toDoItems.newToDo(description, category, subcategory, user, dueBy, cost);
        return executionContext.add(this, newToDo);
    }

    private LocalDate nowPlusDays(int days) {
        return clockService.now().plusDays(days);
    }
    //endregion

    //region > injected services
    @javax.inject.Inject
    private ToDoItems toDoItems;

    @javax.inject.Inject
    private ClockService clockService;
    //endregion


}