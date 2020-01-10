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
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureResult;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts;

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
