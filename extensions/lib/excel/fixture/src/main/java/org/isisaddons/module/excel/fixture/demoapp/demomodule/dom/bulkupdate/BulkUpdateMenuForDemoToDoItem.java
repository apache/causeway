package org.isisaddons.module.excel.fixture.demoapp.demomodule.dom.bulkupdate;

import java.math.BigDecimal;

import javax.annotation.PostConstruct;

import org.joda.time.LocalDate;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.memento.MementoService;
import org.apache.isis.applib.services.memento.MementoService.Memento;

import org.isisaddons.module.excel.dom.ExcelService;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.Category;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.Subcategory;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY,
        objectType = "libExcelFixture.BulkUpdateMenuForDemoToDoItem"
)
@DomainServiceLayout(
        named = "Excel",
        menuOrder = "60.1.1"
)
public class BulkUpdateMenuForDemoToDoItem {

    public BulkUpdateMenuForDemoToDoItem() {
    }

    @PostConstruct
    public void init() {
        if(excelService == null) {
            throw new IllegalStateException("Require ExcelService to be configured");
        }
    }

    // //////////////////////////////////////
    // bulk update manager (action)
    // //////////////////////////////////////

    @Action(
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(sequence="90.1")
    public BulkUpdateManagerForDemoToDoItem bulkUpdateManager() {
        BulkUpdateManagerForDemoToDoItem template = new BulkUpdateManagerForDemoToDoItem();
        template.setFileName("toDoItems.xlsx");
        template.setCategory(Category.Domestic);
        template.setSubcategory(Subcategory.Shopping);
        template.setComplete(false);
        return newBulkUpdateManager(template);
    }



    // //////////////////////////////////////
    // memento for manager
    // //////////////////////////////////////
    
    String mementoFor(final BulkUpdateManagerForDemoToDoItem tdieim) {
        final Memento memento = mementoService.create();
        memento.set("fileName", tdieim.getFileName());
        memento.set("category", tdieim.getCategory());
        memento.set("subcategory", tdieim.getSubcategory());
        memento.set("completed", tdieim.isComplete());
        return memento.asString();
    }
    
    void initOf(final String mementoStr, final BulkUpdateManagerForDemoToDoItem manager) {
        final Memento memento = mementoService.parse(mementoStr);
        manager.setFileName(memento.get("fileName", String.class));
        manager.setCategory(memento.get("category", Category.class));
        manager.setSubcategory(memento.get("subcategory", Subcategory.class));
        manager.setComplete(memento.get("completed", boolean.class));
    }

    BulkUpdateManagerForDemoToDoItem newBulkUpdateManager(BulkUpdateManagerForDemoToDoItem manager) {
        return container.newViewModelInstance(BulkUpdateManagerForDemoToDoItem.class, mementoFor(manager));
    }
    
    // //////////////////////////////////////
    // memento for line item
    // //////////////////////////////////////
    
    String mementoFor(BulkUpdateLineItemForDemoToDoItem lineItem) {
        final Memento memento = mementoService.create();
        memento.set("toDoItem", bookmarkService.bookmarkFor(lineItem.getToDoItem()));
        memento.set("description", lineItem.getDescription());
        memento.set("category", lineItem.getCategory());
        memento.set("subcategory", lineItem.getSubcategory());
        memento.set("cost", lineItem.getCost());
        memento.set("complete", lineItem.isComplete());
        memento.set("dueBy", lineItem.getDueBy());
        memento.set("notes", lineItem.getNotes());
        memento.set("ownedBy", lineItem.getOwnedBy());
        return memento.asString();
    }

    void init(String mementoStr, BulkUpdateLineItemForDemoToDoItem lineItem) {
        final Memento memento = mementoService.parse(mementoStr);
        lineItem.setToDoItem(bookmarkService.lookup(memento.get("toDoItem", Bookmark.class), ExcelDemoToDoItem.class));
        lineItem.setDescription(memento.get("description", String.class));
        lineItem.setCategory(memento.get("category", Category.class));
        lineItem.setSubcategory(memento.get("subcategory", Subcategory.class));
        lineItem.setCost(memento.get("cost", BigDecimal.class));
        lineItem.setComplete(memento.get("complete", boolean.class));
        lineItem.setDueBy(memento.get("dueBy", LocalDate.class));
        lineItem.setNotes(memento.get("notes", String.class));
        lineItem.setOwnedBy(memento.get("ownedBy", String.class));
    }
    
    BulkUpdateLineItemForDemoToDoItem newLineItem(BulkUpdateLineItemForDemoToDoItem lineItem) {
        return container.newViewModelInstance(BulkUpdateLineItemForDemoToDoItem.class, mementoFor(lineItem));
    }


    // //////////////////////////////////////
    // Injected Services
    // //////////////////////////////////////

    @javax.inject.Inject
    private DomainObjectContainer container;

    @javax.inject.Inject
    private ExcelService excelService;

    @javax.inject.Inject
    private BookmarkService bookmarkService;
    
    @javax.inject.Inject
    private MementoService mementoService;

}
