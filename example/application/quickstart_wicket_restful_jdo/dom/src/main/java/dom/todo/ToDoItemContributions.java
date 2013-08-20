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
package dom.todo;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import dom.todo.ToDoItem.Category;
import dom.todo.ToDoItem.Subcategory;

import org.joda.time.LocalDate;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.DescribedAs;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.NotContributed;
import org.apache.isis.applib.annotation.NotContributed.As;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.query.QueryDefault;

public class ToDoItemContributions extends AbstractFactoryAndRepository {

    
    // //////////////////////////////////////
    // priority (contributed property)
    // //////////////////////////////////////
    
    @DescribedAs("The relative priority of this item compared to others (using 'due by' date)")
    @NotInServiceMenu
    @MemberOrder(sequence="1")
    @ActionSemantics(Of.SAFE)
    @NotContributed(As.ACTION)
    @Hidden(where=Where.ALL_TABLES)
    public Integer priority(final ToDoItem toDoItem) {
        if(toDoItem.isComplete()) {
            return null;
        }

        // sort items ...
        final List<ToDoItem> sortedNotYetComplete = 
                ORDERING_DUE_BY
                .compound(ORDERING_DESCRIPTION)
                .sortedCopy(toDoItems.notYetComplete());
        
        // ... then locate this one
        int i=1;
        for (ToDoItem each : sortedNotYetComplete) {
            if(each == toDoItem) {
                return i;
            }
            i++;
        }
        return null;
    }

    private static Ordering<ToDoItem> ORDERING_DUE_BY = 
        Ordering.natural().nullsLast().onResultOf(new Function<ToDoItem, LocalDate>(){
            @Override
            public LocalDate apply(ToDoItem input) {
                return input.getDueBy();
            }
        });
    
    private static Ordering<ToDoItem> ORDERING_DESCRIPTION = 
        Ordering.natural().nullsLast().onResultOf(new Function<ToDoItem, String>(){
            @Override
            public String apply(ToDoItem input) {
                return input.getDescription();
            }
        });
    
    
    // //////////////////////////////////////
    // SimilarTo (contributed collection)
    // //////////////////////////////////////
    
    @NotInServiceMenu
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence="1")
    @NotContributed(As.ACTION)
    public List<ToDoItem> similarTo(final ToDoItem toDoItem) {
        if(false) {
            // the naive implementation ...
            return allMatches(ToDoItem.class, new Filter<ToDoItem>() {
                @Override
                public boolean accept(ToDoItem t) {
                    return t != toDoItem && Objects.equal(toDoItem.getCategory(), t.getCategory()) && Objects.equal(toDoItem.getOwnedBy(), t.getOwnedBy());
                }
            });
        } else {
            // the JDO implementation ...
            final List<ToDoItem> similarToDoItems = allMatches(
                    new QueryDefault<ToDoItem>(ToDoItem.class, 
                            "todo_similarTo", 
                            "ownedBy", currentUserName(), 
                            "category", toDoItem.getCategory()));
            return Lists.newArrayList(Iterables.filter(similarToDoItems, excluding(toDoItem)));
        }
    }

    private static Predicate<ToDoItem> excluding(final ToDoItem toDoItem) {
        return new Predicate<ToDoItem>() {
            @Override
            public boolean apply(ToDoItem input) {
                return input != toDoItem;
            }
        };
    }

    
    // //////////////////////////////////////
    // UpdateCategory (contributed action)
    // //////////////////////////////////////

    @DescribedAs("Update category and subcategory")
    @NotInServiceMenu
    @ActionSemantics(Of.IDEMPOTENT)
    @MemberOrder(name="Subcategory", sequence="1")
    public ToDoItem updateCategory(
            final ToDoItem item, 
            final @Named("Category") Category category,
            final @Named("Subcategory") Subcategory subcategory) {
        item.setCategory(category);
        item.setSubcategory(subcategory);
        return item;
    }

    public Category default1UpdateCategory(
            final ToDoItem item, final Category category, final Subcategory subcategory) {
        return item.getCategory();
    }
    public Subcategory default2UpdateCategory(
            final ToDoItem item, final Category category, final Subcategory subcategory) {
        return item.getSubcategory();
    }

    public List<Subcategory> choices2UpdateCategory(
            final ToDoItem item, final Category category, final Subcategory subcategory) {
        return Subcategory.listFor(category);
    }
    
    public String validateUpdateCategory(
            final ToDoItem item, final Category category, final Subcategory subcategory) {
        return Subcategory.validate(category, subcategory);
    }

    
    // //////////////////////////////////////
    // helpers
    // //////////////////////////////////////
    
    protected String currentUserName() {
        return getContainer().getUser().getName();
    }

    // //////////////////////////////////////

    private ToDoItems toDoItems;
    public void injectToDoItems(ToDoItems toDoItems) {
        this.toDoItems = toDoItems;
    }
}
