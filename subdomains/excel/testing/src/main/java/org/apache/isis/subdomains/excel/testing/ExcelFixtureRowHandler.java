package org.apache.isis.subdomains.excel.testing;

import java.util.List;

import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;

public interface ExcelFixtureRowHandler {
    List<Object> handleRow(
            final FixtureScript.ExecutionContext executionContext,
            final ExcelFixture excelFixture,
            final Object previousRow);
}
