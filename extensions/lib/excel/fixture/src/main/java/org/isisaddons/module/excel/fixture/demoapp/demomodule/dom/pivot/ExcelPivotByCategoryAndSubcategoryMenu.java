package org.isisaddons.module.excel.fixture.demoapp.demomodule.dom.pivot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.value.Blob;

import org.isisaddons.module.excel.dom.ExcelService;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.ExcelDemoToDoItem;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY,
        objectType = "libExcelFixture.ExcelPivotByCategoryAndSubcategoryMenu"
)
@DomainServiceLayout(
        named = "Excel",
        menuOrder = "60.1.2"
)
public class ExcelPivotByCategoryAndSubcategoryMenu {

    public ExcelPivotByCategoryAndSubcategoryMenu() {
    }

    @PostConstruct
    public void init() {
        if(excelService == null) {
            throw new IllegalStateException("Require ExcelService to be configured");
        }
    }

    @Action(
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(sequence="90.2")
    public Blob downloadDemoPivotsheet(){
        return excelService.toExcelPivot(vm1list(), ExcelPivotByCategoryAndSubcategory.class, "pivot-example", "demo-pivots.xlsx");
    }

    private List<ExcelPivotByCategoryAndSubcategory> vm1list(){
        List<ExcelPivotByCategoryAndSubcategory> result = new ArrayList<>();
        for (ExcelDemoToDoItem todo : getToDoItems()){
            result.add(
                    new ExcelPivotByCategoryAndSubcategory(
                            todo.getCategory(),
                            todo.getSubcategory(),
                            todo.getCost()
                    )
            );
        }
        return result;
    }

    private List<ExcelDemoToDoItem> getToDoItems() {
        return container.allInstances(ExcelDemoToDoItem.class);
    }

    @javax.inject.Inject
    private ExcelService excelService;

    @javax.inject.Inject
    private DomainObjectContainer container;

}
