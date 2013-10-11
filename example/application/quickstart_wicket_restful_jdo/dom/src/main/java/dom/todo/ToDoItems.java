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
import java.util.Collections;
import java.util.List;

import com.google.common.base.Predicates;

import dom.todo.ToDoItem.Category;
import dom.todo.ToDoItem.Subcategory;

import org.joda.time.LocalDate;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.RegEx;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.query.QueryDefault;

import services.ClockService;

@Named("ToDos")
public class ToDoItems {

    // //////////////////////////////////////
    // Identification in the UI
    // //////////////////////////////////////

    public String getId() {
        return "toDoItems";
    }

    public String iconName() {
        return "ToDoItem";
    }

    // //////////////////////////////////////
    // NotYetComplete (action)
    // //////////////////////////////////////
    
    @Bookmarkable
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "1")
    public List<ToDoItem> notYetComplete() {
        final List<ToDoItem> items = notYetCompleteNoUi();
        if(items.isEmpty()) {
            container.informUser("All to-do items have been completed :-)");
        }
        return items;
    }

    @Programmatic
    public List<ToDoItem> notYetCompleteNoUi() {
        final List<ToDoItem> items;
        if(false) {
            // the naive implementation ...
            items = container.allMatches(ToDoItem.class, 
                    Predicates.and(
                        ToDoItem.Predicates.thoseOwnedBy(currentUserName()), 
                        ToDoItem.Predicates.thoseNotYetComplete()));
        } else {
            // the JDO implementation ...
            items = container.allMatches(
                    new QueryDefault<ToDoItem>(ToDoItem.class, 
                            "todo_notYetComplete", 
                            "ownedBy", currentUserName()));
        }
        return items;
    }

    
    // //////////////////////////////////////
    // Complete (action)
    // //////////////////////////////////////
    
    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "2")
    public List<ToDoItem> complete() {
        final List<ToDoItem> items = completeNoUi();
        if(items.isEmpty()) {
            container.informUser("No to-do items have yet been completed :-(");
        }
        return items;
    }

    @Programmatic
    public List<ToDoItem> completeNoUi() {
        final List<ToDoItem> items;
        if(false) {
            // the naive implementation ...
            items = container.allMatches(ToDoItem.class, 
                    Predicates.and(
                        ToDoItem.Predicates.thoseOwnedBy(currentUserName()), 
                        ToDoItem.Predicates.thoseComplete()));
        } else {
            // the JDO implementation ...
            items = container.allMatches(
                    new QueryDefault<ToDoItem>(ToDoItem.class, 
                            "todo_complete", 
                            "ownedBy", currentUserName()));
        }
        return items;
    }


    // //////////////////////////////////////
    // NewToDo (action)
    // //////////////////////////////////////

    @MemberOrder(sequence = "3")
    public ToDoItem newToDo(
            final @RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*") @Named("Description") String description, 
            final @Named("Category") Category category,
            final @Named("Subcategory") Subcategory subcategory,
            final @Optional @Named("Due by") LocalDate dueBy,
            final @Optional @Named("Cost") BigDecimal cost) {
        final String ownedBy = currentUserName();
        return newToDo(description, category, subcategory, ownedBy, dueBy, cost);
    }
    public Category default1NewToDo() {
        return Category.Professional;
    }
    public Subcategory default2NewToDo() {
        return Category.Professional.subcategories().get(0);
    }
    public LocalDate default3NewToDo() {
        return clockService.now().plusDays(14);
    }
    public List<Subcategory> choices2NewToDo(
            final String description, final Category category) {
        return Subcategory.listFor(category);
    }
    public String validateNewToDo(
            final String description, 
            final Category category, final Subcategory subcategory, 
            final LocalDate dueBy, final BigDecimal cost) {
        return Subcategory.validate(category, subcategory);
    }

    // //////////////////////////////////////
    // AllToDos (action)
    // //////////////////////////////////////

    @ActionSemantics(Of.SAFE)
    @MemberOrder(sequence = "4")
    public List<ToDoItem> allToDos() {
        final String currentUser = currentUserName();
        final List<ToDoItem> items = container.allMatches(ToDoItem.class, ToDoItem.Predicates.thoseOwnedBy(currentUser));
        Collections.sort(items);
        if(items.isEmpty()) {
            container.warnUser("No to-do items found.");
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
            return container.allMatches(ToDoItem.class, 
                    Predicates.and(
                        ToDoItem.Predicates.thoseOwnedBy(currentUserName()), 
                        ToDoItem.Predicates.thoseWithSimilarDescription(description)));
        } else {
            // the JDO implementation ...
            return container.allMatches(
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
        final ToDoItem toDoItem = container.newTransientInstance(ToDoItem.class);
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
        
        container.persist(toDoItem);
        return toDoItem;
    }
    
    private static double random(double from, double to) {
        return Math.random() * (to-from) + from;
    }

    private String currentUserName() {
        return container.getUser().getName();
    }

    
    // //////////////////////////////////////
    // Injected Services
    // //////////////////////////////////////

    
    private DomainObjectContainer container;

    public void injectDomainObjectContainer(final DomainObjectContainer container) {
        this.container = container;
    }

    private ClockService clockService;
    public void injectClockService(ClockService clockService) {
        this.clockService = clockService;
    }


}
