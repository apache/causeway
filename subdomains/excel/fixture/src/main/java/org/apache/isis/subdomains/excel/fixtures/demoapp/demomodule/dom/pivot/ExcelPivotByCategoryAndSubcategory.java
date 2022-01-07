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

import java.math.BigDecimal;

import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Nature;
import org.apache.isis.subdomains.excel.applib.dom.AggregationType;
import org.apache.isis.subdomains.excel.applib.dom.PivotColumn;
import org.apache.isis.subdomains.excel.applib.dom.PivotRow;
import org.apache.isis.subdomains.excel.applib.dom.PivotValue;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.Category;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.Subcategory;

import lombok.Getter;
import lombok.Setter;

@DomainObject(
        logicalTypeName = "libExcelFixture.ExcelPivotByCategoryAndSubcategory",
        nature = Nature.VIEW_MODEL
)
public class ExcelPivotByCategoryAndSubcategory {

    public ExcelPivotByCategoryAndSubcategory(
            final Category category,
            final Subcategory subcategory,
            final BigDecimal cost){
        this.category = category;
        this.subcategory = subcategory;
        this.cost = cost;
    }

    @Getter @Setter
    @PivotRow
    private Subcategory subcategory;

    @Getter @Setter
    @PivotColumn(order = 1)
    private Category category;

    @Getter @Setter
    @PivotValue(order = 1, type = AggregationType.SUM)
    private BigDecimal cost;

}
