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
package org.apache.isis.subdomains.excel.fixtures.demoapp.demomodule.contributions;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.subdomains.excel.applib.dom.ExcelService;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItemMenu;

@Mixin(method = "act")
public class ExcelDemoToDoItem_export {

    private final ExcelDemoToDoItem toDoItem;

    public ExcelDemoToDoItem_export(final ExcelDemoToDoItem toDoItem) {
        this.toDoItem = toDoItem;
    }

    @Action
    @ActionLayout(contributed = Contributed.AS_ACTION)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Blob act() {
        throw new UnsupportedOperationException();
//        if(actionInvocationContext.isLast()) {
//            // ie current object only
//            final List toDoItems = actionInvocationContext.getDomainObjects();
//            return excelService.toExcel(toDoItems, ExcelDemoToDoItem.class, ExcelDemoToDoItem.class.getSimpleName(), "toDoItems.xlsx");
//        } else {
//            return null;
//        }
    }

    @Inject ExcelService excelService;
    @Inject ExcelDemoToDoItemMenu excelModuleDemoToDoItems;

}
