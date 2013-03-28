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

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberGroups;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Resolve;
import org.apache.isis.applib.annotation.Resolve.Type;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.annotation.Where;
import org.joda.time.LocalDate;

@ObjectType("TODO")
@MemberGroups({"General", "Detail"})
public class ToDoItem {

    public static enum Category {
        Professional, Domestic, Other;
    }

    // {{ Description
    private String description;

    @Title
    @MemberOrder(sequence = "1")
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
    // }}

    // {{ Category
    private Category category;

    @MemberOrder(sequence = "2")
    public Category getCategory() {
        return category;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }
    // }}

    // {{ DueBy (property)
    private LocalDate dueBy;

    @MemberOrder(name="Detail", sequence = "3")
    @Optional
    public LocalDate getDueBy() {
        return dueBy;
    }

    public void setDueBy(final LocalDate dueBy) {
        this.dueBy = dueBy;
    }
    // }}

    // {{ Done
    private boolean done;

    @Disabled
    @MemberOrder(sequence = "4")
    public boolean getDone() {
        return done;
    }

    public void setDone(final boolean done) {
        this.done = done;
    }
    // }}

    // {{ Notes (property)
    private String notes;

    @Hidden(where=Where.ALL_TABLES)
    @Optional
    @MultiLine(numberOfLines=5)
    @MemberOrder(name="Detail", sequence = "6")
    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }
    // }}

    // {{ OwnedBy (property, hidden)
    private String ownedBy;

    @Hidden
    public String getOwnedBy() {
        return ownedBy;
    }

    public void setOwnedBy(final String ownedBy) {
        this.ownedBy = ownedBy;
    }
    // }}

    // {{ markAsDone (action)
    @MemberOrder(sequence = "1")
    public ToDoItem markAsDone() {
        setDone(true);
        return this;
    }

    public String disableMarkAsDone() {
        return done ? "Already done" : null;
    }
    // }}

    // {{ markAsNotDone (action)
    @MemberOrder(sequence = "2")
    public ToDoItem markAsNotDone() {
        setDone(false);
        return this;
    }

    public String disableMarkAsNotDone() {
        return !done ? "Not yet done" : null;
    }
    // }}

    // {{ dependencies (Collection)
    private List<ToDoItem> dependencies = new ArrayList<ToDoItem>();

    @Disabled
    @MemberOrder(sequence = "1")
    @Resolve(Type.EAGERLY)
    public List<ToDoItem> getDependencies() {
        return dependencies;
    }

    public void setDependencies(final List<ToDoItem> dependencies) {
        this.dependencies = dependencies;
    }
    // }}

    // {{ add (action)
    @MemberOrder(name="dependencies", sequence = "3")
    public ToDoItem add(final ToDoItem toDoItem) {
        getDependencies().add(toDoItem);
        return this;
    }
    public String validateAdd(final ToDoItem toDoItem) {
        if(getDependencies().contains(toDoItem)) {
            return "Already a dependency";
        }
        if(toDoItem == this) {
            return "Can't set up a dependency to self";
        }
        return null;
    }
    // }}

    // {{ remove (action)
    @MemberOrder(name="dependencies", sequence = "4")
    public ToDoItem remove(final ToDoItem toDoItem) {
        getDependencies().remove(toDoItem);
        return this;
    }
    public String disableRemove(final ToDoItem toDoItem) {
        return getDependencies().isEmpty()? "No dependencies to remove": null;
    }
    public String validateRemove(final ToDoItem toDoItem) {
        if(!getDependencies().contains(toDoItem)) {
            return "Not a dependency";
        }
        return null;
    }
    public List<ToDoItem> choices0Remove() {
        return getDependencies();
    }
    // }}

    // {{ injected: DomainObjectContainer
    @SuppressWarnings("unused")
    private DomainObjectContainer container;

    public void setDomainObjectContainer(final DomainObjectContainer container) {
        this.container = container;
    }
    // }}

    // {{ injected: ToDoItems
    @SuppressWarnings("unused")
    private ToDoItems toDoItems;

    public void setToDoItems(final ToDoItems toDoItems) {
        this.toDoItems = toDoItems;
    }
    // }}

}
