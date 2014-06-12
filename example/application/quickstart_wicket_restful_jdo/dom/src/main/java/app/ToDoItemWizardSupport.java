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

import dom.todo.ToDoItem;

import org.joda.time.LocalDate;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.memento.MementoService;

@DomainService(menuOrder = "10")
public class ToDoItemWizardSupport {


    @Named("New To Do (wizard)")
    @MemberOrder(name = "ToDos", sequence = "6")
    public ToDoItemWizard newToDoItemWizard() {
        return clone(new ToDoItemWizard());
    }

    ToDoItemWizard clone(ToDoItemWizard toDoItemWizard) {
        return container.newViewModelInstance(ToDoItemWizard.class, mementoFor(toDoItemWizard));
    }

    //region > view model support
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Programmatic
    String mementoFor(ToDoItemWizard toDoItemWizard) {
        final MementoService.Memento memento = mementoService.create();
        memento.set("state", toDoItemWizard.getState());
        memento.set("description", toDoItemWizard.getDescription());
        memento.set("category", toDoItemWizard.getCategory());
        memento.set("subcategory", toDoItemWizard.getSubcategory());
        memento.set("dueBy", toDoItemWizard.getDueBy());
        return memento.asString();
    }

    @Programmatic
    void initOf(String mementoStr, ToDoItemWizard toDoItemWizard) {
        final MementoService.Memento memento = mementoService.parse(mementoStr);
        toDoItemWizard.setState(memento.get("state", ToDoItemWizard.State.class));
        toDoItemWizard.setDescription(memento.get("description", String.class));
        toDoItemWizard.setCategory(memento.get("category", ToDoItem.Category.class));
        toDoItemWizard.setSubcategory(memento.get("subcategory", ToDoItem.Subcategory.class));
        toDoItemWizard.setDueBy(memento.get("dueBy", LocalDate.class));
    }

    //endregion

    //region > injected services
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    @javax.inject.Inject
    private DomainObjectContainer container;

    @javax.inject.Inject
    private MementoService mementoService;

    @javax.inject.Inject
    private BookmarkService bookmarkService;

    @javax.inject.Inject
    private ClockService clockService;



    //endregion

}
