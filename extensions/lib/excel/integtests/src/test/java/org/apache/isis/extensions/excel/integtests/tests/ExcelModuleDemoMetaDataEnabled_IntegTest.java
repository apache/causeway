package org.apache.isis.extensions.excel.integtests.tests;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.extensions.fixtures.fixturescripts.FixtureResult;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts;
import org.assertj.core.api.Assertions;

import org.apache.isis.extensions.excel.integtests.ExcelModuleIntegTestAbstract;
import org.apache.isis.extensions.excel.fixtures.demoapp.demomodule.fixturehandlers.demotodoitem.DemoToDoItemRowHandler2;
import org.apache.isis.extensions.excel.fixtures.demoapp.demomodule.fixturescripts.DemoFixture_extending_ExcelFixture2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.core.Is.is;

public class ExcelModuleDemoMetaDataEnabled_IntegTest extends ExcelModuleIntegTestAbstract {

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

        Assertions.assertThat(fixtureResults.size()).isEqualTo(8);

        List<DemoToDoItemRowHandler2> resultToTest = new ArrayList<>();
        for (FixtureResult fr : fixtureResults){
            resultToTest.add((DemoToDoItemRowHandler2) fr.getObject());
        }

        Assertions.assertThat(resultToTest.get(0).getExcelRowNumber()).isEqualTo(1);
        Assertions.assertThat(resultToTest.get(0).getExcelSheetName()).isEqualTo("Sheet2");

        Assertions.assertThat(resultToTest.get(6).getExcelRowNumber()).isEqualTo(7);
        Assertions.assertThat(resultToTest.get(6).getExcelSheetName()).isEqualTo("Sheet2");

        Assertions.assertThat(resultToTest.get(7).getExcelRowNumber()).isEqualTo(3);
        Assertions.assertThat(resultToTest.get(7).getExcelSheetName()).isEqualTo("Sheet3");
        Assertions.assertThat(resultToTest.get(7).getDescription()).isEqualTo("Another Item");

    }

    @Inject
    protected FixtureScripts fixtureScripts;




}
