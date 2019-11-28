package org.apache.isis.extensions.excel.dom;

import java.util.List;

import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;

public interface ExcelFixtureRowHandler {
    List<Object> handleRow(
            final FixtureScript.ExecutionContext executionContext,
            final ExcelFixture excelFixture,
            final Object previousRow);
}
