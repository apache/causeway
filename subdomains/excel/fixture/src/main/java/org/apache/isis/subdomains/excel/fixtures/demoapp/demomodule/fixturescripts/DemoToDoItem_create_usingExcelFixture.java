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
package org.apache.isis.subdomains.excel.fixtures.demoapp.demomodule.fixturescripts;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;

import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.subdomains.excel.testing.ExcelFixture;
import org.apache.isis.subdomains.excel.fixtures.demoapp.demomodule.fixturehandlers.demotodoitem.DemoToDoItemRowHandler;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

import lombok.Getter;

public class DemoToDoItem_create_usingExcelFixture extends FixtureScript {

    private final String user;

    public DemoToDoItem_create_usingExcelFixture() {
        this(null);
    }

    public DemoToDoItem_create_usingExcelFixture(String ownedBy) {
        this.user = ownedBy;
    }


    @Getter
    private List<ExcelDemoToDoItem> todoItems = Lists.newArrayList();

    @Override
    public void execute(ExecutionContext executionContext) {

        final String ownedBy = this.user != null ? this.user : userService.getUser().getName();

        installFor(ownedBy, executionContext);

        transactionService.flushTransaction();
    }

    private void installFor(String user, ExecutionContext ec) {

        ec.setParameter("user", user);

        this.todoItems.addAll(load(ec, "ToDoItems.xlsx"));
        this.todoItems.addAll(load(ec, "MoreToDoItems.xlsx"));

        transactionService.flushTransaction();
    }

    private List<ExcelDemoToDoItem> load(
            final ExecutionContext executionContext,
            final String resourceName) {
        final URL excelResource = Resources.getResource(getClass(), resourceName);
        final ExcelFixture excelFixture = new ExcelFixture(excelResource, DemoToDoItemRowHandler.class);
        excelFixture.setExcelResourceName(resourceName);
        executionContext.executeChild(this, excelFixture);

        return (List<ExcelDemoToDoItem>) excelFixture.getObjects();
    }


    @Inject UserService userService;
    @Inject TransactionService transactionService;


}
