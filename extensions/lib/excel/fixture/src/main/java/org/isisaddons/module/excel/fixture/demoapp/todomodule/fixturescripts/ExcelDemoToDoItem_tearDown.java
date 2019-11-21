package org.isisaddons.module.excel.fixture.demoapp.todomodule.fixturescripts;

import org.apache.isis.applib.fixturescripts.teardown.TeardownFixtureAbstract2;

import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.ExcelDemoToDoItem;

public class ExcelDemoToDoItem_tearDown extends TeardownFixtureAbstract2 {

    @Override
    protected void execute(final ExecutionContext executionContext) {
        deleteFrom(ExcelDemoToDoItem.class);
    }

}
