package org.isisaddons.module.excel.fixture.demoapp.demomodule.dom.bulkupdate;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import org.apache.isis.applib.AbstractViewModel;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.InvokeOn;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.actinvoc.ActionInvocationContext;

import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.Category;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.ExcelDemoToDoItemMenu;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.dom.Subcategory;

@DomainObject(
        objectType = "libExcelFixture.BulkUpdateLineItemForDemoToDoItem"
)
@DomainObjectLayout(
        named = "Bulk update line item",
        bookmarking = BookmarkPolicy.AS_ROOT
)
public class BulkUpdateLineItemForDemoToDoItem
        extends AbstractViewModel 
        implements Comparable<BulkUpdateLineItemForDemoToDoItem> {


    public String title() {
        final ExcelDemoToDoItem existingItem = getToDoItem();
        if(existingItem != null) {
            return "EXISTING: " + getContainer().titleOf(existingItem);
        }
        return "NEW: " + getDescription();
    }
    
    
    // //////////////////////////////////////
    // ViewModel implementation
    // //////////////////////////////////////
    

    @Override
    public String viewModelMemento() {
        return toDoItemExportImportService.mementoFor(this);
    }

    @Override
    public void viewModelInit(final String mementoStr) {
        toDoItemExportImportService.init(mementoStr, this);
    }

    
    // //////////////////////////////////////
    // ToDoItem (optional property)
    // //////////////////////////////////////
    
    private ExcelDemoToDoItem toDoItem;

    @MemberOrder(sequence="1")
    public ExcelDemoToDoItem getToDoItem() {
        return toDoItem;
    }
    public void setToDoItem(final ExcelDemoToDoItem toDoItem) {
        this.toDoItem = toDoItem;
    }
    public void modifyToDoItem(final ExcelDemoToDoItem toDoItem) {
        setToDoItem(toDoItem);
        setDescription(toDoItem.getDescription());
        setCategory(toDoItem.getCategory());
        setSubcategory(toDoItem.getSubcategory());
        setComplete(toDoItem.isComplete());
        setCost(toDoItem.getCost());
        setDueBy(toDoItem.getDueBy());
        setNotes(toDoItem.getNotes());
        setOwnedBy(toDoItem.getOwnedBy());
    }

    
    // //////////////////////////////////////
    // Description (property)
    // //////////////////////////////////////
    
    private String description;

    @MemberOrder(sequence="2")
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    // //////////////////////////////////////
    // Category and Subcategory (property)
    // //////////////////////////////////////

    private Category category;

    @MemberOrder(sequence="3")
    public Category getCategory() {
        return category;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }

    // //////////////////////////////////////

    private Subcategory subcategory;

    @MemberOrder(sequence="4")
    public Subcategory getSubcategory() {
        return subcategory;
    }
    public void setSubcategory(final Subcategory subcategory) {
        this.subcategory = subcategory;
    }

    // //////////////////////////////////////
    // OwnedBy (property)
    // //////////////////////////////////////
    
    private String ownedBy;

    @MemberOrder(sequence="5")
    public String getOwnedBy() {
        return ownedBy;
    }

    public void setOwnedBy(final String ownedBy) {
        this.ownedBy = ownedBy;
    }


    // //////////////////////////////////////
    // DueBy (property)
    // //////////////////////////////////////

    private LocalDate dueBy;

    @MemberOrder(sequence="6")
    public LocalDate getDueBy() {
        return dueBy;
    }

    public void setDueBy(final LocalDate dueBy) {
        this.dueBy = dueBy;
    }

    
    // //////////////////////////////////////
    // Complete (property), 
    // Done (action), Undo (action)
    // //////////////////////////////////////

    private boolean complete;

    @MemberOrder(sequence="7")
    public boolean isComplete() {
        return complete;
    }

    public void setComplete(final boolean complete) {
        this.complete = complete;
    }


    // //////////////////////////////////////
    // Cost (property), UpdateCost (action)
    // //////////////////////////////////////

    private BigDecimal cost;

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(final BigDecimal cost) {
        this.cost = cost!=null?cost.setScale(2, BigDecimal.ROUND_HALF_EVEN):null;
    }
    

    // //////////////////////////////////////
    // Notes (property)
    // //////////////////////////////////////

    private String notes;

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }


    // //////////////////////////////////////
    // apply
    // //////////////////////////////////////

    @Action(
            semantics = SemanticsOf.IDEMPOTENT,
            invokeOn = InvokeOn.OBJECT_AND_COLLECTION
    )
    public ExcelDemoToDoItem apply() {
        ExcelDemoToDoItem item = getToDoItem();
        if(item == null) {
            // description must be unique, so check
            item = toDoItems.findToDoItemsByDescription(getDescription());
            if(item != null) {
                getContainer().warnUser("Item already exists with description '" + getDescription() + "'");
            } else {
                // create new item
                // (since this is just a demo, haven't bothered to validate new values)
                item = toDoItems.newToDoItem(getDescription(), getCategory(), getSubcategory(), getDueBy(), getCost());
                item.setNotes(getNotes());
                item.setOwnedBy(getOwnedBy());
                item.setComplete(isComplete());
            }
        } else {
            // copy over new values
            // (since this is just a demo, haven't bothered to validate new values)
            item.setDescription(getDescription());
            item.setCategory(getCategory());
            item.setSubcategory(getSubcategory());
            item.setDueBy(getDueBy());
            item.setCost(getCost());
            item.setNotes(getNotes());
            item.setOwnedBy(getOwnedBy());
            item.setComplete(isComplete());
        }
        return actionInvocationContext.getInvokedOn().isCollection()? null: item;
    }

    
    // //////////////////////////////////////
    // compareTo
    // //////////////////////////////////////

    @Override
    public int compareTo(final BulkUpdateLineItemForDemoToDoItem other) {
        return this.toDoItem.compareTo(other.toDoItem);
    }

    
    // //////////////////////////////////////
    // injected services
    // //////////////////////////////////////
    
    @javax.inject.Inject
    private BulkUpdateMenuForDemoToDoItem toDoItemExportImportService;
    
    @javax.inject.Inject
    private ExcelDemoToDoItemMenu toDoItems;

    @javax.inject.Inject
    private ActionInvocationContext actionInvocationContext;
}
