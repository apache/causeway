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
package org.apache.causeway.extensions.excel.fixtures.demoapp.demomodule.dom.pivot;

import java.math.BigDecimal;

import javax.inject.Named;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.extensions.excel.applib.AggregationType;
import org.apache.causeway.extensions.excel.applib.annotation.PivotColumn;
import org.apache.causeway.extensions.excel.applib.annotation.PivotRow;
import org.apache.causeway.extensions.excel.applib.annotation.PivotValue;
import org.apache.causeway.extensions.excel.fixtures.demoapp.todomodule.dom.Category;
import org.apache.causeway.extensions.excel.fixtures.demoapp.todomodule.dom.Subcategory;

import lombok.Getter;
import lombok.Setter;

@Named("libExcelFixture.ExcelPivotByCategoryAndSubcategory")
@DomainObject(
        nature = Nature.VIEW_MODEL)
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
