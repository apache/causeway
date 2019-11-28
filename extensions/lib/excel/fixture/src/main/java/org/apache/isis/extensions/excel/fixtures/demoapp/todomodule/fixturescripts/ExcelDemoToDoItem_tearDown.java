package org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.fixturescripts;

import org.apache.isis.extensions.fixtures.legacy.teardown.TeardownFixtureAbstract2;
import org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem;

public class ExcelDemoToDoItem_tearDown extends TeardownFixtureAbstract2 {

    @Override
    protected void execute(final ExecutionContext executionContext) {
        deleteFrom(ExcelDemoToDoItem.class);
    }

}
