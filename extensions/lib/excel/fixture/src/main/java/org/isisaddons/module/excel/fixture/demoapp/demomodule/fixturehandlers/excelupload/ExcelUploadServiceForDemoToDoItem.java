package org.isisaddons.module.excel.fixture.demoapp.demomodule.fixturehandlers.excelupload;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.fixturescripts.FixtureResult;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.value.Blob;

import org.isisaddons.module.excel.dom.ExcelFixture;
import org.isisaddons.module.excel.fixture.demoapp.demomodule.fixturehandlers.demotodoitem.DemoToDoItemRowHandler;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY,
        objectType = "libExcelFixture.ExcelUploadServiceForDemoToDoItem"
)
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        named = "Prototyping",
        menuOrder = "900"
)
public class ExcelUploadServiceForDemoToDoItem {

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

    @Inject
    private FixtureScripts fixtureScripts;
}
