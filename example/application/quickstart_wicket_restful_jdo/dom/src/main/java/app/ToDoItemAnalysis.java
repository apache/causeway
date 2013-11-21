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
package app;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import dom.todo.ToDoItem.Category;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Programmatic;

@Named("Analysis")
public class ToDoItemAnalysis {

    // //////////////////////////////////////
    // Identification in the UI
    // //////////////////////////////////////

    public String getId() {
        return "analysis";
    }

    public String iconName() {
        return "ToDoItem";
    }

    // //////////////////////////////////////
    // ByCategory (action)
    // //////////////////////////////////////
    
    @Named("By Category")
    @Bookmarkable
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public List<ToDoItemsByCategoryViewModel> toDoItemsByCategory() {
        final List<Category> categories = Arrays.asList(Category.values());
        return Lists.newArrayList(Iterables.transform(categories, byCategory()));
    }

    private Function<Category, ToDoItemsByCategoryViewModel> byCategory() {
        return new Function<Category, ToDoItemsByCategoryViewModel>(){
             @Override
             public ToDoItemsByCategoryViewModel apply(final Category category) {
                 final ToDoItemsByCategoryViewModel byCategory = 
                     container.newViewModelInstance(ToDoItemsByCategoryViewModel.class, category.name());
                 byCategory.setCategory(category);
                 return byCategory;
             }
         };
    }

    // //////////////////////////////////////
    // ByDateRange (action)
    // //////////////////////////////////////
    
    public enum DateRange {
        OverDue,
        Today,
        Tomorrow,
        ThisWeek,
        Later,
        Unknown,
    }
    
    @Named("By Date Range")
    @Bookmarkable
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public List<ToDoItemsByDateRangeViewModel> toDoItemsByDateRange() {
        final List<DateRange> dateRanges = Arrays.asList(DateRange.values());
        return Lists.newArrayList(Iterables.transform(dateRanges, byDateRange()));
    }

    private Function<DateRange, ToDoItemsByDateRangeViewModel> byDateRange() {
        return new Function<DateRange, ToDoItemsByDateRangeViewModel>(){
             @Override
             public ToDoItemsByDateRangeViewModel apply(final DateRange dateRange) {
                 final ToDoItemsByDateRangeViewModel byDateRange = 
                     container.newViewModelInstance(ToDoItemsByDateRangeViewModel.class, dateRange.name());
                 byDateRange.setDateRange(dateRange);
                 return byDateRange;
             }
         };
    }
    
    
    // //////////////////////////////////////
    // ForCategory (programmatic)
    // //////////////////////////////////////

    @Programmatic
    public ToDoItemsByCategoryViewModel toDoItemsForCategory(Category category) {
        return byCategory().apply(category);
    }
    

    // //////////////////////////////////////
    // Injected services
    // //////////////////////////////////////

    @javax.inject.Inject
    private DomainObjectContainer container;

}
