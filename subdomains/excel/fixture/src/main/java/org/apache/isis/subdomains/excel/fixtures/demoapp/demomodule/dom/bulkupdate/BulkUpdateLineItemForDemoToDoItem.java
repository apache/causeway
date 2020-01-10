package org.apache.isis.subdomains.excel.fixtures.demoapp.demomodule.dom.bulkupdate;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.inject.Inject;
import javax.jdo.annotations.Column;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.Category;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItemMenu;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.Subcategory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@DomainObject(
        nature = Nature.VIEW_MODEL,
        objectType = "libExcelFixture.BulkUpdateLineItemForDemoToDoItem"
)
@DomainObjectLayout(
        named = "Bulk update line item",
        bookmarking = BookmarkPolicy.AS_ROOT
)
@XmlRootElement(name = "BulkUpdateLineItemForDemoToDoItem")
@XmlType(
        propOrder = {
                "description",
                "category",
                "subcategory",
                "ownedBy",
                "dueBy",
                "complete",
                "cost",
                "notes",
        }
)
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
public class BulkUpdateLineItemForDemoToDoItem
        implements Comparable<BulkUpdateLineItemForDemoToDoItem> {

    public BulkUpdateLineItemForDemoToDoItem(ExcelDemoToDoItem toDoItem) {
        modifyToDoItem(toDoItem);
    }

    public String title() {
        final ExcelDemoToDoItem existingItem = getToDoItem();
        if(existingItem != null) {
            return "EXISTING: " + titleService.titleOf(existingItem);
        }
        return "NEW: " + getDescription();
    }
    
    @Getter @Setter
    private ExcelDemoToDoItem toDoItem;

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

    
    @Getter @Setter
    private String description;

    @Getter @Setter
    private Category category;

    @Getter @Setter
    private Subcategory subcategory;

    @Getter @Setter
    private String ownedBy;

    @Getter @Setter
    private LocalDate dueBy;

    @Getter @Setter
    private boolean complete;

    @Getter @Setter
    @Column(length = 8, scale = 2)
    private BigDecimal cost;

    @Getter @Setter
    private String notes;


    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public ExcelDemoToDoItem apply() {
        ExcelDemoToDoItem item = getToDoItem();
        if(item == null) {
            // description must be unique, so check
            item = toDoItems.findToDoItemsByDescription(getDescription());
            if(item != null) {
                messageService.warnUser("Item already exists with description '" + getDescription() + "'");
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
        return item;
    }

    @Override
    public int compareTo(final BulkUpdateLineItemForDemoToDoItem other) {
        return this.toDoItem.compareTo(other.toDoItem);
    }


    @XmlTransient @Inject BulkUpdateMenuForDemoToDoItem toDoItemExportImportService;
    @XmlTransient @Inject ExcelDemoToDoItemMenu toDoItems;
    @XmlTransient @Inject UserService userService;
    @XmlTransient @Inject MessageService messageService;
    @XmlTransient @Inject TitleService titleService;

}
