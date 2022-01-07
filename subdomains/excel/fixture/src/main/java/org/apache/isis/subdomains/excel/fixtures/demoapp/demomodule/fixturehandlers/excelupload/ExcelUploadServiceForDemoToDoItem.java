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
package org.apache.isis.subdomains.excel.fixtures.demoapp.demomodule.fixturehandlers.excelupload;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotations.DomainService;
import org.apache.isis.applib.annotations.DomainServiceLayout;
import org.apache.isis.applib.annotations.NatureOfService;
import org.apache.isis.applib.annotations.Optionality;
import org.apache.isis.applib.annotations.Parameter;
import org.apache.isis.applib.annotations.ParameterLayout;
import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.subdomains.excel.testing.ExcelFixture;
import org.apache.isis.subdomains.excel.fixtures.demoapp.demomodule.fixturehandlers.demotodoitem.DemoToDoItemRowHandler;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureResult;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

@DomainService(
        nature = NatureOfService.VIEW,
        logicalTypeName = "libExcelFixture.ExcelUploadServiceForDemoToDoItem"
)
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        named = "Prototyping"
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class ExcelUploadServiceForDemoToDoItem {

    private final FixtureScripts fixtureScripts;

    @Inject
    public ExcelUploadServiceForDemoToDoItem(FixtureScripts fixtureScripts) {
        this.fixtureScripts = fixtureScripts;
    }

    public List<FixtureResult> uploadSpreadsheet(
            @ParameterLayout(named = "spreadsheet")
            final Blob file,
            @ParameterLayout(named = "ExcelFixture parameters")
            @Parameter(optionality = Optionality.OPTIONAL)
            final String parameters){
        FixtureScript script = new ExcelFixture(
                file,
                DemoToDoItemRowHandler.class,
                ExcelUploadRowHandler4ToDoItem.class);
        return fixtureScripts.runFixtureScript(script, parameters);
    }

}
