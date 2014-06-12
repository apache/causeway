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

import dom.todo.Categorized;
import dom.todo.ToDoItem;
import dom.todo.ToDoItem.Category;
import dom.todo.ToDoItems;

import com.google.common.base.Strings;
import org.joda.time.LocalDate;
import org.apache.isis.applib.AbstractWizard;
import org.apache.isis.applib.annotation.*;

public class ToDoItemWizard
        extends AbstractWizard<ToDoItemWizard, ToDoItemWizard.State>
        implements Categorized {

    //region > constructor
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public ToDoItemWizard() {
        setState(State.DESCRIPTION);
    }
    //endregion

    //region > identification
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String title() {
        return !Strings.isNullOrEmpty(getDescription()) ? getDescription() : "New item";
    }
    //endregion

    //region > viewModel implementation
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public String viewModelMemento() {
        return toDoItemWizardSupport.mementoFor(this);
    }

    @Override
    public void viewModelInit(String memento) {
        toDoItemWizardSupport.initOf(memento, this);
    }

    @Override
    public ToDoItemWizard clone() {
        return cloneThis();
    }
    //endregion

    //region > state (property)
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public enum State implements AbstractWizard.State<ToDoItemWizard> {
        DESCRIPTION,
        CATEGORIES,
        DUE_BY,
        CONFIRMATION_PAGE;

        public boolean hideDescription() {
            return this != DESCRIPTION && this != CONFIRMATION_PAGE;
        }
        public boolean hideCategories() {
            return this != CATEGORIES && this != CONFIRMATION_PAGE;
        }
        public boolean hideDueBy() {
            return this != DUE_BY && this != CONFIRMATION_PAGE;
        }

        public String disableFinish(ToDoItemWizard toDoItemWizard) {
            return Strings.isNullOrEmpty(toDoItemWizard.getDescription())? "Must enter a description": null;
        }

        @Override
        public State next() {
            switch (this) {
                case DESCRIPTION: return CATEGORIES;
                case CATEGORIES: return DUE_BY;
                case DUE_BY: return CONFIRMATION_PAGE;
                default: return CONFIRMATION_PAGE;
            }
        }
        @Override
        public String disableNext(ToDoItemWizard w) {
            return w.getState().next() == null? "No more pages": null;
        }

        @Override
        public State previous() {
            switch (this) {
                case CONFIRMATION_PAGE: return DUE_BY;
                case DUE_BY: return CATEGORIES;
                case CATEGORIES: return DESCRIPTION;
                default: return null;
            }
        }
        @Override
        public String disablePrevious(ToDoItemWizard w) {
            return w.getState().previous() == null? "No more pages": null;
        }
    }
    //endregion

    //region > description (property)
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private String description;

    @MaxLength(100)
    @RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*")
    @TypicalLength(50)
    public String getDescription() {
        return description;
    }
    public void setDescription(final String description) {
        this.description = description;
    }

    public boolean hideDescription() {
        return getState().hideDescription();
    }
    //endregion

    //region > category (property)
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private Category category;

    /**
     * Used as {@link #viewModelMemento() memento}
     */
    public Category getCategory() {
        return category;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }

    public boolean hideCategory() {
        return getState().hideCategories();
    }
    //endregion

    //region > subcategory (hidden property)
    private ToDoItem.Subcategory subcategory;

    @Hidden
    @Optional
    public ToDoItem.Subcategory getSubcategory() {
        return subcategory;
    }
    public void setSubcategory(final ToDoItem.Subcategory subcategory) {
        this.subcategory = subcategory;
    }
    //endregion

    //region > dueBy (property)
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private LocalDate dueBy;

    @Optional
    public LocalDate getDueBy() {
        return dueBy;
    }

    public void setDueBy(final LocalDate dueBy) {
        this.dueBy = dueBy;
    }
    public void clearDueBy() {
        setDueBy(null);
    }
    public String validateDueBy(final LocalDate dueBy) {
        if (dueBy == null) {
            return null;
        }
        return toDoItems.validateDueBy(dueBy);
    }
    public boolean hideDueBy() {
        return getState().hideDueBy();
    }
    //endregion

    //region > finish (action)
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @MemberOrder(sequence = "1")
    public ToDoItem finish() {
        return toDoItems.newToDo(getDescription(), getCategory(), getSubcategory(), getDueBy(), null);
    }

    public String disableFinish() {
        return getState().disableFinish(this);
    }
    //endregion

    //region > wizard impl
    @Override
    protected ToDoItemWizard cloneThis() {
        return toDoItemWizardSupport.clone(this);
    }
    //endregion

    //region > injected services
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @javax.inject.Inject
    private ToDoItemWizardSupport toDoItemWizardSupport;

    @javax.inject.Inject
    private ToDoItems toDoItems;
    //endregion

}
