package org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.fixturescripts;

import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.apache.isis.testing.fixtures.applib.teardown.TeardownFixtureAbstract;

public class ExcelDemoToDoItem_tearDown extends TeardownFixtureAbstract {

    @Override
    protected void execute(final ExecutionContext executionContext) {
        deleteFrom(ExcelDemoToDoItem.class);
    }

}
