/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.extensions.excel.fixtures.demoapp.demomodule.dom.bulkupdate;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.Column;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.jaxb.JavaTimeJaxbAdapters;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.services.title.TitleService;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.extensions.excel.fixtures.demoapp.todomodule.dom.Category;
import org.apache.causeway.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.apache.causeway.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItemMenu;
import org.apache.causeway.extensions.excel.fixtures.demoapp.todomodule.dom.Subcategory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Named("libExcelFixture.BulkUpdateLineItemForDemoToDoItem")
@DomainObject(
        nature = Nature.VIEW_MODEL)
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

    public BulkUpdateLineItemForDemoToDoItem(final ExcelDemoToDoItem toDoItem) {
        modifyToDoItem(toDoItem);
    }

    @ObjectSupport public String title() {
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

    @Getter @Setter @XmlJavaTypeAdapter(JavaTimeJaxbAdapters.LocalDateToStringAdapter.class)
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
