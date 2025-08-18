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
package org.apache.causeway.extensions.excel.fixtures.demoapp.demomodule.fixturescripts;

import java.net.URL;
import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.extensions.excel.fixtures.demoapp.demomodule.fixturehandlers.demotodoitem.DemoToDoItemRowHandler;
import org.apache.causeway.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.apache.causeway.extensions.excel.testing.ExcelFixture;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;

import lombok.Getter;

public class DemoToDoItem_create_usingExcelFixture extends FixtureScript {

    private final String user;

    public DemoToDoItem_create_usingExcelFixture() {
        this(null);
    }

    public DemoToDoItem_create_usingExcelFixture(final String ownedBy) {
        this.user = ownedBy;
    }

    @Getter
    private List<ExcelDemoToDoItem> todoItems = _Lists.newArrayList();

    @Override
    public void execute(final ExecutionContext executionContext) {

        final String ownedBy = this.user != null ? this.user : userService.currentUserNameElseNobody();

        installFor(ownedBy, executionContext);

        transactionService.flushTransaction();
    }

    private void installFor(final String user, final ExecutionContext ec) {

        ec.setParameter("user", user);

        this.todoItems.addAll(load(ec, "ToDoItems.xlsx"));
        this.todoItems.addAll(load(ec, "MoreToDoItems.xlsx"));

        transactionService.flushTransaction();
    }

    private List<ExcelDemoToDoItem> load(
            final ExecutionContext executionContext,
            final String resourceName) {

        final URL excelResource = _Resources.lookupResourceUrl(getClass(), resourceName).orElse(null);
        final ExcelFixture excelFixture = new ExcelFixture(excelResource, DemoToDoItemRowHandler.class);
        excelFixture.setExcelResourceName(resourceName);
        executionContext.executeChild(this, excelFixture);

        return excelFixture.getObjects();
    }

    @Inject UserService userService;
    @Inject TransactionService transactionService;

}
