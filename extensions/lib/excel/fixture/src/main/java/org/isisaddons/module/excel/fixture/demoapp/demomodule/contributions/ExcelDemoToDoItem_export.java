package org.isisaddons.module.excel.fixture.demoapp.demomodule.contributions;

import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.InvokeOn;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.services.actinvoc.ActionInvocationContext;
import org.apache.isis.applib.value.Blob;

import org.isisaddons.module.excel.dom.ExcelService;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.ExcelDemoToDoItemMenu;

@Mixin(method = "act")
public class ExcelDemoToDoItem_export {

    private final ExcelDemoToDoItem toDoItem;

    public ExcelDemoToDoItem_export(final ExcelDemoToDoItem toDoItem) {
        this.toDoItem = toDoItem;
    }

    @Action(
            invokeOn = InvokeOn.OBJECT_ONLY // ISIS-705 ... bulk actions returning Blobs are not yet supported
    )
    @ActionLayout(contributed = Contributed.AS_ACTION)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Blob act() {
        if(actionInvocationContext.isLast()) {
            // ie current object only
            final List toDoItems = actionInvocationContext.getDomainObjects();
            return excelService.toExcel(toDoItems, ExcelDemoToDoItem.class, ExcelDemoToDoItem.class.getSimpleName(), "toDoItems.xlsx");
        } else {
            return null;
        }
    }

    

    @javax.inject.Inject
    private ExcelService excelService;

    @javax.inject.Inject
    private ExcelDemoToDoItemMenu excelModuleDemoToDoItems;

    @javax.inject.Inject
    private ActionInvocationContext actionInvocationContext;

}
