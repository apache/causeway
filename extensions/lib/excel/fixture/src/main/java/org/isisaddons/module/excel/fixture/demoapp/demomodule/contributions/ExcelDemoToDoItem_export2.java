package org.isisaddons.module.excel.fixture.demoapp.demomodule.contributions;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.value.Blob;
import org.isisaddons.module.excel.dom.ExcelService;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.ExcelDemoToDoItemMenu;

@Mixin(method = "act")
public class ExcelDemoToDoItem_export2 {

    private final ExcelDemoToDoItem toDoItem;

    public ExcelDemoToDoItem_export2(final ExcelDemoToDoItem toDoItem) {
        this.toDoItem = toDoItem;
    }

    @Action()
    @ActionLayout(
            contributed = Contributed.AS_ACTION
    )
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Blob act() {
        throw new UnsupportedOperationException();
//        if(actionInvocationContext.isLast()) {
//            // ie current object only
//            final List toDoItems = actionInvocationContext.getDomainObjects();
//            final List<ExcelDemoToDoItem> allItems = this.excelModuleDemoToDoItems.allInstances();
//            final List<WorksheetContent> worksheetContents = Lists.newArrayList(
//                    new WorksheetContent(toDoItems, new WorksheetSpec(ExcelDemoToDoItem.class, "current")),
//                    new WorksheetContent(allItems, new WorksheetSpec(ExcelDemoToDoItem.class, "all")));
//            return excelService.toExcel(worksheetContents, "toDoItems.xlsx");
//        } else {
//            return null;
//        }
    }

    
    @Inject ExcelService excelService;
    @Inject ExcelDemoToDoItemMenu excelModuleDemoToDoItems;

}
