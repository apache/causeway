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
package org.apache.isis.subdomains.excel.fixtures.demoapp.demomodule.fixturehandlers.demotodoitem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.ViewModel;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.Category;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItemMenu;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.Subcategory;
import org.apache.isis.subdomains.excel.testing.ExcelFixture;
import org.apache.isis.subdomains.excel.testing.ExcelFixtureRowHandler;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import lombok.Getter;
import lombok.Setter;

@ViewModel
public class DemoToDoItemRowHandler implements ExcelFixtureRowHandler {

    @Getter @Setter
    private String description;

    @Getter @Setter
    private Category category;

    @Getter @Setter
    private Subcategory subcategory;

    @Getter @Setter
    private Integer daysFromToday;

    @Getter @Setter
    private BigDecimal cost;

    @Override
    public List<Object> handleRow(
            final FixtureScript.ExecutionContext executionContext,
            final ExcelFixture excelFixture,
            final Object previousRow) {
        final DemoToDoItemRowHandler previous = (DemoToDoItemRowHandler) previousRow;
        if(category == null) {
            category = previous.category;
        }
        if(subcategory == null) {
            subcategory = previous.subcategory;
        }

        final LocalDate dueBy = daysFromToday(daysFromToday);
        final String user = executionContext.getParameter("user");
        final String username = user != null && user.length() > 0 ? user : userService.getUser().getName();
        ExcelDemoToDoItem toDoItem = toDoItemRepository.findToDoItemsByDescription(description);
        if(toDoItem != null) {
            toDoItem.setCategory(category);
            toDoItem.setSubcategory(subcategory);
            toDoItem.setDueBy(dueBy);
            toDoItem.setCost(cost);
            toDoItem.setOwnedBy(username);
        } else {
            toDoItem = toDoItemRepository.newToDoItem(description, category, subcategory, username, dueBy, cost);
        }
        executionContext.addResult(excelFixture, toDoItem);
        return Collections.<Object>singletonList(toDoItem);
    }

    private LocalDate daysFromToday(final Integer i) {
        if(i == null) {
            return null;
        }
        final LocalDate date = clockService.now();
        return date.plusDays(i);
    }


    @Inject private ExcelDemoToDoItemMenu toDoItemRepository;
    @Inject private UserService userService;
    @Inject private ClockService clockService;
}
