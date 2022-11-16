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

import javax.inject.Named;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.extensions.excel.applib.CausewayModuleExtExcelApplib;
import org.apache.causeway.extensions.excel.applib.Mode;
import org.apache.causeway.extensions.excel.applib.WorksheetSpec;
import org.apache.causeway.extensions.excel.fixtures.demoapp.demomodule.fixturehandlers.demotodoitem.DemoToDoItemRowHandler2;
import org.apache.causeway.extensions.excel.testing.ExcelFixture2;

import lombok.Getter;
import lombok.Setter;

@Named(DemoFixture_extending_ExcelFixture2.LOGICAL_TYPE_NAME)
@DomainObject
public class DemoFixture_extending_ExcelFixture2 extends ExcelFixture2 {

    static final String LOGICAL_TYPE_NAME = CausewayModuleExtExcelApplib.NAMESPACE + ".DemoFixture_extending_ExcelFixture2";

    public DemoFixture_extending_ExcelFixture2(){
        this.resourceName = "ToDoItems.xlsx";
    }

    @Getter @Setter
    private String resourceName;

    @Override
    protected void execute(final ExecutionContext executionContext) {

        setExcelResource(_Resources.getResourceUrl(getClass(), getResourceName()));

        setMatcher(sheetName -> {

            if (sheetName.startsWith("Sheet")) {
                return new WorksheetSpec(
                        DemoFixture_extending_ExcelFixture2.this.rowFactoryFor(DemoToDoItemRowHandler2.class, executionContext),
                        sheetName,
                        Mode.RELAXED);
            } else
                return null;
        });

        setSequencer(specs -> specs);

        super.execute(executionContext);
    }

}
