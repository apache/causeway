#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
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

import app.ToDoItemAnalysis.DateRange;
import dom.todo.ToDoItem;
import dom.todo.ToDoItems;

import java.util.List;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.ViewModel;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.util.ObjectContracts;

@DomainObjectLayout(
    named="By Date Range"
)
@Bookmarkable
@ViewModel
public class ToDoItemsByDateRangeViewModel
        implements Comparable<ToDoItemsByDateRangeViewModel> {

    //region > constructors
    public ToDoItemsByDateRangeViewModel() {
    }
    public ToDoItemsByDateRangeViewModel(DateRange dateRange) {
        setDateRange(dateRange);
    }
    //endregion

    //region > dateRange (property)
    private DateRange dateRange;

    @Title
    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(final DateRange dateRange) {
        this.dateRange = dateRange;
    }
    //endregion

    //region > count (derived property)
    public int getCount() {
        return getItemsNotYetComplete().size();
    }
    //endregion

    //region > getItemsNotYetComplete (collection)
    /**
     * All those items {@link ToDoItems${symbol_pound}notYetComplete() not yet complete}, for this {@link ${symbol_pound}getCategory() category}.
     */
    @Render(Type.EAGERLY)
    public List<ToDoItem> getItemsNotYetComplete() {
        final List<ToDoItem> notYetComplete = toDoItems.notYetCompleteNoUi();
        return Lists.newArrayList(Iterables.filter(notYetComplete, thoseInDateRange()));
    }

    private Predicate<ToDoItem> thoseInDateRange() {
        return new Predicate<ToDoItem>() {
            @Override
            public boolean apply(ToDoItem t) {
                return Objects.equal(dateRangeFor(t), getDateRange());
            }
        };
    }
    
    private DateRange dateRangeFor(final ToDoItem t) {
        if(t.getDueBy() == null) {
            return DateRange.Unknown;
        }
        final DateTime dueBy = t.getDueBy().toDateTimeAtStartOfDay();
        final DateTime today = clockService.now().toDateTimeAtStartOfDay();
        
        if(dueBy.isBefore(today)) {
            return DateRange.OverDue;
        }
        if(dueBy.isBefore(today.plusDays(1))) {
            return DateRange.Today;
        }
        if(dueBy.isBefore(today.plusDays(2))) {
            return DateRange.Tomorrow;
        }
        if(dueBy.isBefore(today.plusDays(7))) {
            return DateRange.ThisWeek;
        }
        return DateRange.Later;
    }
    //endregion

    //region > compareTo
    @Override
    public int compareTo(ToDoItemsByDateRangeViewModel other) {
        return ObjectContracts.compare(this, other, "dateRange");
    }
    //endregion

    //region > injected services
    @javax.inject.Inject
    private ToDoItems toDoItems;

    @javax.inject.Inject
    private ClockService clockService;
    //endregion

}
