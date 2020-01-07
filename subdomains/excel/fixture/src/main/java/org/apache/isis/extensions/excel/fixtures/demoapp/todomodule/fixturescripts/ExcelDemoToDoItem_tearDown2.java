package org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.fixturescripts;

import javax.inject.Inject;

import org.apache.isis.applib.services.jdosupport.IsisJdoSupport;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;

public class ExcelDemoToDoItem_tearDown2 extends FixtureScript {

    private final String user;

    public ExcelDemoToDoItem_tearDown2() {
        this(null);
    }

    public ExcelDemoToDoItem_tearDown2(String ownedBy) {
        this.user = ownedBy;
    }

    @Override
    public void execute(ExecutionContext executionContext) {

        final String ownedBy = this.user != null ? this.user : userService.getUser().getName();

        isisJdoSupport.executeUpdate(String.format(
                "delete "
                        + "from \"excelFixture\".\"ExcelDemoToDoItemDependencies\" "
                        + "where \"dependingId\" IN "
                        + "(select \"id\" from \"excelFixture\".\"ExcelDemoToDoItem\" where \"ownedBy\" = '%s') ",
                ownedBy));

        isisJdoSupport.executeUpdate(String.format(
                "delete from \"excelFixture\".\"ExcelDemoToDoItem\" "
                        + "where \"ownedBy\" = '%s'", ownedBy));
    }


    @Inject IsisJdoSupport isisJdoSupport;

}
