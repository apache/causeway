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
package org.apache.causeway.extensions.excel.fixtures.demoapp.demomodule.fixturehandlers.excelupload;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.extensions.excel.fixtures.demoapp.demomodule.fixturehandlers.demotodoitem.DemoToDoItemRowHandler;
import org.apache.causeway.extensions.excel.testing.ExcelFixture;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureResult;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts;

@DomainService(
        nature = NatureOfService.VIEW
)
@Named("libExcelFixture.ExcelUploadServiceForDemoToDoItem")
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        named = "Prototyping"
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class ExcelUploadServiceForDemoToDoItem {

    private final FixtureScripts fixtureScripts;

    @Inject
    public ExcelUploadServiceForDemoToDoItem(final FixtureScripts fixtureScripts) {
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
