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
package org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.fixturescripts;

import javax.inject.Inject;

import org.apache.isis.persistence.jdo.applib.services.IsisJdoSupport;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

public class ExcelDemoToDoItem_tearDown2 extends FixtureScript {

    private final String user;

    public ExcelDemoToDoItem_tearDown2() {
        this(null);
    }

    public ExcelDemoToDoItem_tearDown2(String ownedBy) {
        this.user = ownedBy;
    }

    @Override
    public void execute(ExecutionContext executionContext) {

        final String ownedBy = this.user != null ? this.user : userService.getUserNameElseNobody();

        isisJdoSupport.executeUpdate(String.format(
                "delete "
                        + "from \"excelFixture\".\"ExcelDemoToDoItemDependencies\" "
                        + "where \"dependingId\" IN "
                        + "(select \"id\" from \"excelFixture\".\"ExcelDemoToDoItem\" where \"ownedBy\" = '%s') ",
                ownedBy));

        isisJdoSupport.executeUpdate(String.format(
                "delete from \"excelFixture\".\"ExcelDemoToDoItem\" "
                        + "where \"ownedBy\" = '%s'", ownedBy));
    }


    @Inject IsisJdoSupport isisJdoSupport;

}
