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
package org.apache.isis.subdomains.excel.integtests.tests;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.isis.subdomains.excel.fixtures.demoapp.demomodule.fixturehandlers.demotodoitem.DemoToDoItemRowHandler2;
import org.apache.isis.subdomains.excel.fixtures.demoapp.demomodule.fixturescripts.DemoFixture_extending_ExcelFixture2;
import org.apache.isis.subdomains.excel.integtests.ExcelModuleIntegTestAbstract;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureResult;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import lombok.val;

public class ExcelModuleDemoMetaDataEnabled_IntegTest extends ExcelModuleIntegTestAbstract {

    @Inject protected FixtureScripts fixtureScripts;
    
    List<FixtureResult> fixtureResults;

    @BeforeEach
    public void setUpData() throws Exception {
        FixtureScript script = new FixtureScript() {
            @Override
            protected void execute(final FixtureScript.ExecutionContext executionContext) {
                executionContext.executeChild(this, new DemoFixture_extending_ExcelFixture2());
                fixtureResults = executionContext.getResults();
            }
        };
        fixtureScripts.runFixtureScript(script, "");
    }


    @Test
    public void testResults() throws Exception{

        assertThat(fixtureResults.size()).isEqualTo(8);

        val resultToTest = new ArrayList<DemoToDoItemRowHandler2>();
        for (val fixtureResult : fixtureResults){
            
            val rowHandler = (DemoToDoItemRowHandler2) fixtureResult.getObject();
            resultToTest.add(rowHandler);
        }

        assertThat(resultToTest.get(0).getExcelRowNumber()).isEqualTo(1);
        assertThat(resultToTest.get(0).getExcelSheetName()).isEqualTo("Sheet2");

        assertThat(resultToTest.get(6).getExcelRowNumber()).isEqualTo(7);
        assertThat(resultToTest.get(6).getExcelSheetName()).isEqualTo("Sheet2");

        assertThat(resultToTest.get(7).getExcelRowNumber()).isEqualTo(3);
        assertThat(resultToTest.get(7).getExcelSheetName()).isEqualTo("Sheet3");
        assertThat(resultToTest.get(7).getDescription()).isEqualTo("Another Item");

    }

}
