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

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.subdomains.excel.applib.dom.ExcelService;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.Category;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.Subcategory;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "libExcelFixture.BulkUpdateMenuForDemoToDoItem"
)
@DomainServiceLayout(named = "Excel")
public class BulkUpdateMenuForDemoToDoItem {

    public BulkUpdateMenuForDemoToDoItem() {
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    @ActionLayout(sequence="90.1")
    public BulkUpdateManagerForDemoToDoItem bulkUpdateManager() {
        BulkUpdateManagerForDemoToDoItem manager = new BulkUpdateManagerForDemoToDoItem();
        manager.setFileName("toDoItems.xlsx");
        manager.setCategory(Category.Domestic);
        manager.setSubcategory(Subcategory.Shopping);
        manager.setComplete(false);
        return manager;
    }


    @XmlTransient @Inject private RepositoryService repositoryService;
    @XmlTransient @Inject private FactoryService factoryService;
    @XmlTransient @Inject private UserService userService;
    @XmlTransient @Inject private ExcelService excelService;
    @XmlTransient @Inject private BookmarkService bookmarkService;
    
}
