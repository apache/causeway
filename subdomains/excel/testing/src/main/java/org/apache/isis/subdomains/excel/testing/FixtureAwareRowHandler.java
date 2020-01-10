package org.apache.isis.subdomains.excel.testing;


import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;
import org.apache.isis.subdomains.excel.applib.dom.RowHandler;

public interface FixtureAwareRowHandler<T extends FixtureAwareRowHandler<T>> extends RowHandler<T> {

    void setExecutionContext(FixtureScript.ExecutionContext ec);
    void setExcelFixture2(ExcelFixture2 excelFixture2);

}
