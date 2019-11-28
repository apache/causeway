package org.isisaddons.module.excel.fixture.demoapp.demomodule.dom.pivot;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Blob;
import org.isisaddons.module.excel.dom.ExcelService;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.ExcelDemoToDoItem;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "libExcelFixture.ExcelPivotByCategoryAndSubcategoryMenu"
)
@DomainServiceLayout(
        named = "Excel"
)
public class ExcelPivotByCategoryAndSubcategoryMenu {

    @Action(semantics = SemanticsOf.IDEMPOTENT)
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
        return repositoryService.allInstances(ExcelDemoToDoItem.class);
    }

    @Inject ExcelService excelService;
    @Inject RepositoryService repositoryService;

}
