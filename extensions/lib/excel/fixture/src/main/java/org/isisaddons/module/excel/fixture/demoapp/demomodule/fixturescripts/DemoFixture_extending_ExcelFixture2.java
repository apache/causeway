package org.isisaddons.module.excel.fixture.demoapp.demomodule.fixturescripts;

import com.google.common.io.Resources;

import org.apache.isis.applib.annotation.DomainObject;

import org.isisaddons.module.excel.dom.ExcelFixture2;
import org.isisaddons.module.excel.dom.WorksheetSpec;
import org.isisaddons.module.excel.dom.util.Mode;
import org.isisaddons.module.excel.fixture.demoapp.demomodule.fixturehandlers.demotodoitem.DemoToDoItemRowHandler2;

import lombok.Getter;
import lombok.Setter;

@DomainObject(
        objectType = "isisexcel.DemoFixture_extending_ExcelFixture2"
)
public class DemoFixture_extending_ExcelFixture2 extends ExcelFixture2 {

    public DemoFixture_extending_ExcelFixture2(){
        this.resourceName = "ToDoItems.xlsx";
    }

    @Getter @Setter
    private String resourceName;

    @Override
    protected void execute(final ExecutionContext executionContext) {

        setExcelResource(Resources.getResource(getClass(), getResourceName()));

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
