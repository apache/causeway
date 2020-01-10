package org.apache.isis.subdomains.excel.testing;

import java.util.List;

import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;

public interface ExcelFixtureWorkbookHandler {
    void workbookHandled(
            final FixtureScript.ExecutionContext executionContext,
            final ExcelFixture2 excelFixture,
            List<List<?>> rows);
}
