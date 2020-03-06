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
package org.apache.isis.subdomains.excel.fixtures.demoapp.demomodule.dom.pivot;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.subdomains.excel.applib.dom.ExcelService;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem;

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
