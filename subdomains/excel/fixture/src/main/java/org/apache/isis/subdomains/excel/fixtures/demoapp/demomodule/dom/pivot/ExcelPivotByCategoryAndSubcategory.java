package org.apache.isis.subdomains.excel.fixtures.demoapp.demomodule.dom.pivot;

import java.math.BigDecimal;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.extensions.excel.dom.AggregationType;
import org.apache.isis.extensions.excel.dom.PivotColumn;
import org.apache.isis.extensions.excel.dom.PivotRow;
import org.apache.isis.extensions.excel.dom.PivotValue;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.Category;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.Subcategory;

import lombok.Getter;
import lombok.Setter;

@DomainObject(
        objectType = "libExcelFixture.ExcelPivotByCategoryAndSubcategory",
        nature = Nature.VIEW_MODEL
)
public class ExcelPivotByCategoryAndSubcategory {

    public ExcelPivotByCategoryAndSubcategory(
            final Category category,
            final Subcategory subcategory,
            final BigDecimal cost){
        this.category = category;
        this.subcategory = subcategory;
        this.cost = cost;
    }

    @Getter @Setter
    @PivotRow
    private Subcategory subcategory;

    @Getter @Setter
    @PivotColumn(order = 1)
    private Category category;

    @Getter @Setter
    @PivotValue(order = 1, type = AggregationType.SUM)
    private BigDecimal cost;

}
