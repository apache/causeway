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
package org.apache.causeway.extensions.excel.fixtures.demoapp.todomodule.fixturescripts;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

import javax.inject.Inject;

import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.extensions.excel.fixtures.demoapp.todomodule.dom.Category;
import org.apache.causeway.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.apache.causeway.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItemMenu;
import org.apache.causeway.extensions.excel.fixtures.demoapp.todomodule.dom.Subcategory;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;

public class ExcelDemoToDoItem_recreate5_for extends FixtureScript {

    private final String user;

    public ExcelDemoToDoItem_recreate5_for() {
        this(null);
    }

    public ExcelDemoToDoItem_recreate5_for(String ownedBy) {
        this.user = ownedBy;
    }


    @Override
    protected void execute(ExecutionContext executionContext) {

        final String ownedBy = this.user != null? this.user : userService.currentUserNameElseNobody();

        executionContext.executeChild(this, new ExcelDemoToDoItem_tearDown2(ownedBy));

        installFor(ownedBy, executionContext);

        transactionService.flushTransaction();
    }

    private void installFor(String user, ExecutionContext executionContext) {

        ExcelDemoToDoItem t1 = createToDoItemForUser("Buy milk", Category.Domestic, Subcategory.Shopping, user, daysFromToday(0), new BigDecimal("0.75"), executionContext);
        ExcelDemoToDoItem t2 = createToDoItemForUser("Buy bread", Category.Domestic, Subcategory.Shopping, user, daysFromToday(0), new BigDecimal("1.75"), executionContext);
        ExcelDemoToDoItem t3 = createToDoItemForUser("Buy stamps", Category.Domestic, Subcategory.Shopping, user, daysFromToday(0), new BigDecimal("10.00"), executionContext);
        t3.setComplete(true);
        ExcelDemoToDoItem t4 = createToDoItemForUser("Pick up laundry", Category.Domestic, Subcategory.Chores, user, daysFromToday(6), new BigDecimal("7.50"), executionContext);
        ExcelDemoToDoItem t5 = createToDoItemForUser("Mow lawn", Category.Domestic, Subcategory.Garden, user, daysFromToday(6), null, executionContext);

        createToDoItemForUser("Vacuum house", Category.Domestic, Subcategory.Housework, user, daysFromToday(3), null, executionContext);
        createToDoItemForUser("Sharpen knives", Category.Domestic, Subcategory.Chores, user, daysFromToday(14), null, executionContext);

        createToDoItemForUser("Write to penpal", Category.Other, Subcategory.Other, user, null, null, executionContext);

        createToDoItemForUser("Write blog post", Category.Professional, Subcategory.Marketing, user, daysFromToday(7), null, executionContext).setComplete(true);
        createToDoItemForUser("Organize brown bag", Category.Professional, Subcategory.Consulting, user, daysFromToday(14), null, executionContext);
        createToDoItemForUser("Submit conference session", Category.Professional, Subcategory.Education, user, daysFromToday(21), null, executionContext);
        createToDoItemForUser("Stage Causeway release", Category.Professional, Subcategory.OpenSource, user, null, null, executionContext);

        t1.add(t2);
        t1.add(t3);
        t1.add(t4);
        t1.add(t5);

        t2.add(t3);
        t2.add(t4);
        t2.add(t5);

        t3.add(t4);

        transactionService.flushTransaction();
    }


    // //////////////////////////////////////

    private ExcelDemoToDoItem createToDoItemForUser(final String description, final Category category, Subcategory subcategory, String user, final LocalDate dueBy, final BigDecimal cost, ExecutionContext executionContext) {
        final ExcelDemoToDoItem toDoItem = demoToDoItemMenu.newToDoItem(description, category, subcategory, user, dueBy, cost);
        executionContext.addResult(this, toDoItem);
        return toDoItem;
    }

    private LocalDate daysFromToday(final int i) {
        final LocalDate date = clockService.getClock().nowAsLocalDate(ZoneId.systemDefault());
        return date.plusDays(i);
    }


    @Inject private ExcelDemoToDoItemMenu demoToDoItemMenu;
    //@Inject private CausewayJdoSupport causewayJdoSupport;
    @Inject private ClockService clockService;

}
