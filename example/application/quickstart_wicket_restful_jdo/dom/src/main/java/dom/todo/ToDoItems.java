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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
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
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.query.QueryDefault;

@Named("ToDos")
public class ToDoItems extends AbstractFactoryAndRepository {

    // //////////////////////////////////////
    // Identification in the UI
    // //////////////////////////////////////

    @Override
    public String getId() {
        return "toDoItems";
    }

    public String iconName() {
        return "ToDoItem";
    }

    // //////////////////////////////////////
    // NotYetComplete (action)
    // //////////////////////////////////////
    
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public List<ToDoItem> notYetComplete() {
        final List<ToDoItem> items;
        if(false) {
            // the naive implementation ...
            items = allMatches(ToDoItem.class, new Filter<ToDoItem>() {
                @Override
                public boolean accept(final ToDoItem t) {
                    return ownedByCurrentUser(t) && !t.isComplete();
                }
            });
        } else {
            // the JDO implementation ...
            items = allMatches(
                    new QueryDefault<ToDoItem>(ToDoItem.class, 
                            "todo_notYetComplete", 
                            "ownedBy", currentUserName()));
        }
        if(items.isEmpty()) {
            getContainer().informUser("All to-do items have been completed :-)");
        }
        return items;
    }
    
    // //////////////////////////////////////
    // Complete (action)
    // //////////////////////////////////////
    
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "2")
    public List<ToDoItem> complete() {
        final List<ToDoItem> items;
        if(false) {
            // the naive implementation ...
            items = allMatches(ToDoItem.class, new Filter<ToDoItem>() {
                @Override
                public boolean accept(final ToDoItem t) {
                    return ownedByCurrentUser(t) && t.isComplete();
                }
            });
        } else {
            // the JDO implementation ...
            items = allMatches(
                    new QueryDefault<ToDoItem>(ToDoItem.class, 
                            "todo_complete", 
                            "ownedBy", currentUserName()));
        }
        if(items.isEmpty()) {
            getContainer().informUser("No to-do items have yet been completed :-(");
        }
        return items;
    }


    // //////////////////////////////////////
    // NewToDo (action)
    // //////////////////////////////////////

    @MemberOrder(sequence = "3")
    public ToDoItem newToDo(
            @RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*") // words, spaces and selected punctuation
            @Named("Description") String description, 
            @Named("Category") Category category,
            @Named("Subcategory") Subcategory subcategory,
            @Optional
            @Named("Due by") LocalDate dueBy,
            @Optional
            @Named("Cost") BigDecimal cost) {
        final String ownedBy = currentUserName();
        return newToDo(description, category, subcategory, ownedBy, dueBy, cost);
    }
    public LocalDate default3NewToDo() {
        return new LocalDate(Clock.getTime()).plusDays(14);
    }

    // //////////////////////////////////////
    // AllToDos (action)
    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "4")
    public List<ToDoItem> allToDos() {
        return allToDos(NotifyUserIfNone.YES);
    }

    public enum NotifyUserIfNone { YES, NO }
    
    @Programmatic
    public List<ToDoItem> allToDos(NotifyUserIfNone notifyUser) {
        final String currentUser = currentUserName();
        final List<ToDoItem> items = allMatches(ToDoItem.class, ToDoItem.thoseOwnedBy(currentUser));
        Collections.sort(items);
        if(notifyUser == NotifyUserIfNone.YES && items.isEmpty()) {
            getContainer().warnUser("No to-do items found.");
        }
        return items;
    }


    // //////////////////////////////////////
    // AutoComplete
    // //////////////////////////////////////

    @Programmatic // not part of metamodel
    public List<ToDoItem> autoComplete(final String description) {
        if(false) {
            // the naive implementation ...
            return allMatches(ToDoItem.class, new Filter<ToDoItem>() {
                @Override
                public boolean accept(final ToDoItem t) {
                    return ownedByCurrentUser(t) && t.getDescription().contains(description);
                }
                
            });
        } else {
            // the JDO implementation ...
            return allMatches(
                    new QueryDefault<ToDoItem>(ToDoItem.class, 
                            "todo_autoComplete", 
                            "ownedBy", currentUserName(), 
                            "description", description));
        }
    }

    // //////////////////////////////////////
    // Programmatic Helpers
    // //////////////////////////////////////

    @Programmatic // for use by fixtures
    public ToDoItem newToDo(
            final String description, 
            final Category category, 
            final Subcategory subcategory,
            final String userName, 
            final LocalDate dueBy, final BigDecimal cost) {
        final ToDoItem toDoItem = newTransientInstance(ToDoItem.class);
        toDoItem.setDescription(description);
        toDoItem.setCategory(category);
        toDoItem.setSubcategory(subcategory);
        toDoItem.setOwnedBy(userName);
        toDoItem.setDueBy(dueBy);
        toDoItem.setCost(cost);

        // 
        // GMAP3: uncomment to use https://github.com/danhaywood/isis-wicket-gmap3        
        // toDoItem.setLocation(
        //    new Location(51.5172+random(-0.05, +0.05), 0.1182 + random(-0.05, +0.05)));
        //
        
        persist(toDoItem);
        return toDoItem;
    }
    
    private static double random(double from, double to) {
        return Math.random() * (to-from) + from;
    }

    
    protected boolean ownedByCurrentUser(final ToDoItem t) {
        return Objects.equal(t.getOwnedBy(), currentUserName());
    }
    protected String currentUserName() {
        return getContainer().getUser().getName();
    }

}
