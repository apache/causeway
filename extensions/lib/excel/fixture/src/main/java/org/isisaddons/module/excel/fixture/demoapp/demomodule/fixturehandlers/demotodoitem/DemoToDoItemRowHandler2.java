package org.isisaddons.module.excel.fixture.demoapp.demomodule.fixturehandlers.demotodoitem;

import java.math.BigDecimal;

import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;
import org.isisaddons.module.excel.dom.ExcelFixture2;
import org.isisaddons.module.excel.dom.ExcelMetaDataEnabled;
import org.isisaddons.module.excel.dom.FixtureAwareRowHandler;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.Category;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.Subcategory;

import lombok.Getter;
import lombok.Setter;

public class DemoToDoItemRowHandler2 implements FixtureAwareRowHandler<DemoToDoItemRowHandler2>, ExcelMetaDataEnabled {

    @Getter @Setter
    private String excelSheetName;

    @Getter @Setter
    private Integer excelRowNumber;

    @Getter @Setter
    private String description;

    @Getter @Setter
    private Category category;

    @Getter @Setter
    private Subcategory subcategory;

    @Getter @Setter
    private BigDecimal cost;

    @Override
    public void handleRow(final DemoToDoItemRowHandler2 previousRow) {
        final DemoToDoItemRowHandler2 previous = previousRow;
        if(category == null) {
            category = previous.category;
        }
        if(subcategory == null) {
            subcategory = previous.subcategory;
        }

        executionContext.addResult(excelFixture2, this);
    }


    /**
     * To allow for usage within fixture scripts also.
     */
    @Setter
    private FixtureScript.ExecutionContext executionContext;

    /**
     * To allow for usage within fixture scripts also.
     */
    @Setter
    private ExcelFixture2 excelFixture2;

}
