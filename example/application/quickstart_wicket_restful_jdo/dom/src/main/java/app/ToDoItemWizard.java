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

@Named("By Category")
@Bookmarkable
public class ToDoItemWizard
        extends AbstractWizard<ToDoItemWizard, ToDoItemWizard.State>
        implements Categorized {

    public ToDoItemWizard() {
        setState(State.ENTER_DESCRIPTION_PAGE);
    }


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

    public ToDoItemWizard clone() {
        return cloneThis();
    }
    //endregion

    //region > state (property)
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public enum State implements AbstractWizard.State<ToDoItemWizard> {
        ENTER_DESCRIPTION_PAGE,
        ENTER_CATEGORY_PAGE,
        ENTER_DUE_BY_PAGE,
        CONFIRMATION_PAGE;

        public boolean hideDescription() {
            return this != ENTER_DESCRIPTION_PAGE && this != CONFIRMATION_PAGE;
        }
        public boolean hideCategory() {
            return this != ENTER_CATEGORY_PAGE && this != CONFIRMATION_PAGE;
        }
        public boolean hideDueBy() {
            return this != ENTER_DUE_BY_PAGE && this != CONFIRMATION_PAGE;
        }

        public String disableFinish(ToDoItemWizard toDoItemWizard) {
            return Strings.isNullOrEmpty(toDoItemWizard.getDescription())? "Must enter a description": null;
        }

        @Override
        public State next() {
            switch (this) {
                case ENTER_DESCRIPTION_PAGE: return ENTER_CATEGORY_PAGE;
                case ENTER_CATEGORY_PAGE: return ENTER_DUE_BY_PAGE;
                case ENTER_DUE_BY_PAGE: return CONFIRMATION_PAGE;
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
                case CONFIRMATION_PAGE: return ENTER_DUE_BY_PAGE;
                case ENTER_DUE_BY_PAGE: return ENTER_CATEGORY_PAGE;
                case ENTER_CATEGORY_PAGE: return ENTER_DESCRIPTION_PAGE;
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
        return getState().hideCategory();
    }
    //endregion

    private ToDoItem.Subcategory subcategory;

    public ToDoItem.Subcategory getSubcategory() {
        return subcategory;
    }
    public void setSubcategory(final ToDoItem.Subcategory subcategory) {
        this.subcategory = subcategory;
    }

    /**
     * Only show subcategory if has been populated.
     */
    public boolean hideSubcategory() {
        return subcategory == null;
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
        return toDoItems.newToDo(getDescription(), getCategory(), null, getDueBy(), null);
    }

    public String disableFinish() {
        return getState().disableFinish(this);
    }
    //endregion

    //region > wizard impl
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected ToDoItemWizard cloneThis() {
        return toDoItemWizardSupport.clone(this);
    }
    //endregion



    //region > injected services
    // //////////////////////////////////////

    @javax.inject.Inject
    private ToDoItemWizardSupport toDoItemWizardSupport;

    @javax.inject.Inject
    private ToDoItems toDoItems;
    //endregion

}
