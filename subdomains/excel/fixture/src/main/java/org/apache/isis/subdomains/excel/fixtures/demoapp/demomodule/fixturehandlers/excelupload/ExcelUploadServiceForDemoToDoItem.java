package org.apache.isis.subdomains.excel.fixtures.demoapp.demomodule.fixturehandlers.excelupload;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.extensions.excel.dom.ExcelFixture;
import org.apache.isis.subdomains.excel.fixtures.demoapp.demomodule.fixturehandlers.demotodoitem.DemoToDoItemRowHandler;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureResult;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "libExcelFixture.ExcelUploadServiceForDemoToDoItem"
)
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        named = "Prototyping"
)
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
