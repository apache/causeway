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
package org.apache.isis.subdomains.excel.fixtures.demoapp.demomodule.dom.bulkupdate;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.BookmarkPolicy;
import org.apache.isis.applib.annotations.Collection;
import org.apache.isis.applib.annotations.CollectionLayout;
import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.DomainObjectLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.applib.annotations.Parameter;
import org.apache.isis.applib.annotations.ParameterLayout;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.subdomains.excel.applib.dom.ExcelService;
import org.apache.isis.subdomains.excel.applib.dom.WorksheetContent;
import org.apache.isis.subdomains.excel.applib.dom.WorksheetSpec;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.Category;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.Subcategory;

import static org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem.Predicates.thoseCategorised;
import static org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem.Predicates.thoseCompleted;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

@DomainObject(
        nature = Nature.VIEW_MODEL,
        logicalTypeName = "libExcelFixture.BulkUpdateManagerForDemoToDoItem"
)
@DomainObjectLayout(
        named ="Import/export manager",
        bookmarking = BookmarkPolicy.AS_ROOT
)
@XmlRootElement(name = "BulkUpdateManagerForDemoToDoItem")
@XmlType(
        propOrder = {
                "fileName",
                "category",
                "subcategory",
                "complete",
        }
)
@XmlAccessorType(XmlAccessType.FIELD)
public class BulkUpdateManagerForDemoToDoItem {

    public static final WorksheetSpec WORKSHEET_SPEC =
            new WorksheetSpec(BulkUpdateLineItemForDemoToDoItem.class, "line-items");

    public BulkUpdateManagerForDemoToDoItem(){
    }

    public String title() {
        return "Import/export manager";
    }

    @Getter @Setter @Nullable
    private String fileName;

    @Getter @Setter @Nullable
    private Category category;

    @Getter @Setter @Nullable
    private Subcategory subcategory;

    @Getter @Setter @Nullable
    private boolean complete;


    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public BulkUpdateManagerForDemoToDoItem changeFileName(final String fileName) {
        setFileName(fileName);
        return this;
    }
    @MemberSupport public String default0ChangeFileName() {
        return getFileName();
    }


    @Action
    public BulkUpdateManagerForDemoToDoItem select(
            final Category category,
            @Nullable
            final Subcategory subcategory,
            @ParameterLayout(named="Completed?")
            final boolean completed) {
        setCategory(category);
        setSubcategory(subcategory);
        setComplete(completed);
        return this;
    }
    @MemberSupport public Category default0Select() {
        return getCategory();
    }
    @MemberSupport public Subcategory default1Select() {
        return getSubcategory();
    }
    @MemberSupport public boolean default2Select() {
        return isComplete();
    }
    @MemberSupport public List<Subcategory> choices1Select(
            final Category category) {
        return Subcategory.listFor(category);
    }
    @MemberSupport public String validateSelect(
            final Category category,
            final Subcategory subcategory,
            final boolean completed) {
        return Subcategory.validate(category, subcategory);
    }

    private String currentUserName() {
        return userService.currentUserNameElseNobody();
    }

    @Collection
    @CollectionLayout(defaultView = "table")
    public List<ExcelDemoToDoItem> getToDoItems() {
        return repositoryService.allMatches(ExcelDemoToDoItem.class,
                thoseCompleted(isComplete()).and(thoseCategorised(getCategory(), getSubcategory())));
    }


    @Action(semantics = SemanticsOf.SAFE)
    public Blob export() {
        final String fileName = withExtension(getFileName(), ".xlsx");
        final List<ExcelDemoToDoItem> items = getToDoItems();
        return toExcel(fileName, items);
    }
    @MemberSupport public String disableExport() {
        return getFileName() == null? "file name is required": null;
    }

    private static String withExtension(final String fileName, final String fileExtension) {
        return fileName.endsWith(fileExtension) ? fileName : fileName + fileExtension;
    }

    private Blob toExcel(final String fileName, final List<ExcelDemoToDoItem> items) {
        val toDoItemViewModels = items.stream()
                .map(BulkUpdateLineItemForDemoToDoItem::new)
                .collect(Collectors.toList());
        return excelService.toExcel(new WorksheetContent(toDoItemViewModels, WORKSHEET_SPEC), fileName);
    }

    @Action(choicesFrom = "toDoItems")
    @ActionLayout(named = "Import", sequence = "2")
    public List<BulkUpdateLineItemForDemoToDoItem> importBlob(
            @Parameter(fileAccept = ".xlsx")
            @ParameterLayout(named="Excel spreadsheet")
            final Blob spreadsheet) {
        final List<BulkUpdateLineItemForDemoToDoItem> lineItems =
                excelService.fromExcel(spreadsheet, WORKSHEET_SPEC);
        messageService.informUser(lineItems.size() + " items imported");
        return lineItems;
    }

    @XmlTransient @Inject MessageService messageService;
    @XmlTransient @Inject RepositoryService repositoryService;
    @XmlTransient @Inject UserService userService;
    @XmlTransient @Inject ExcelService excelService;
    @XmlTransient @Inject BulkUpdateMenuForDemoToDoItem bulkUpdateMenuForDemoToDoItem;

}
