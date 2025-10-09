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

import jakarta.inject.Inject;

import org.apache.causeway.extensions.excel.testing.ExcelFixture;
import org.apache.causeway.persistence.jpa.applib.services.JpaSupportService;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;

public class ExcelDemoToDoItem_tearDown2 extends FixtureScript {

    @Inject JpaSupportService jpaSupport;

    private final String user;

    public ExcelDemoToDoItem_tearDown2() {
        this(null);
    }

    public ExcelDemoToDoItem_tearDown2(final String ownedBy) {
        this.user = ownedBy;
    }

    @Override
    public void execute(final ExecutionContext executionContext) {

        final String ownedBy = this.user != null ? this.user : userService.currentUserNameElseNobody();

        var em = jpaSupport.getEntityManagerElseFail(ExcelFixture.class);

//FIXME
//        jdoSupport.executeUpdate(String.format(
//                "delete "
//                        + "from \"excelFixture\".\"ExcelDemoToDoItemDependencies\" "
//                        + "where \"dependingId\" IN "
//                        + "(select \"id\" from \"excelFixture\".\"ExcelDemoToDoItem\" where \"ownedBy\" = '%s') ",
//                ownedBy));
//
//        jdoSupport.executeUpdate(String.format(
//                "delete from \"excelFixture\".\"ExcelDemoToDoItem\" "
//                        + "where \"ownedBy\" = '%s'", ownedBy));
    }

}
