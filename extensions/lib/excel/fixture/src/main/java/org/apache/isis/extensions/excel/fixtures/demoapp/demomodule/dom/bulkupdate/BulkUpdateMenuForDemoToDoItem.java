package org.apache.isis.extensions.excel.fixtures.demoapp.demomodule.dom.bulkupdate;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.extensions.excel.dom.ExcelService;
import org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.dom.Category;
import org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.dom.Subcategory;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "libExcelFixture.BulkUpdateMenuForDemoToDoItem"
)
@DomainServiceLayout(named = "Excel")
public class BulkUpdateMenuForDemoToDoItem {

    public BulkUpdateMenuForDemoToDoItem() {
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    @MemberOrder(sequence="90.1")
    public BulkUpdateManagerForDemoToDoItem bulkUpdateManager() {
        BulkUpdateManagerForDemoToDoItem manager = new BulkUpdateManagerForDemoToDoItem();
        manager.setFileName("toDoItems.xlsx");
        manager.setCategory(Category.Domestic);
        manager.setSubcategory(Subcategory.Shopping);
        manager.setComplete(false);
        return manager;
    }


    @Inject private RepositoryService repositoryService;
    @Inject private FactoryService factoryService;
    @Inject private UserService userService;
    @Inject private ExcelService excelService;
    @Inject private BookmarkService bookmarkService;
    
}
