package org.apache.isis.extensions.excel.fixtures.demoapp.demomodule.fixturehandlers.demotodoitem;

import java.math.BigDecimal;

import org.apache.isis.extensions.excel.dom.ExcelFixture2;
import org.apache.isis.extensions.excel.dom.ExcelMetaDataEnabled;
import org.apache.isis.extensions.excel.dom.FixtureAwareRowHandler;
import org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.dom.Category;
import org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.dom.Subcategory;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class DemoToDoItemRowHandler2 
implements FixtureAwareRowHandler<DemoToDoItemRowHandler2>, ExcelMetaDataEnabled {

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
