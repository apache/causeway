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
        SUMMARY_PAGE;

        public boolean hideDescription() {
            return this != DESCRIPTION;
        }
        public boolean hideCategories() {
            return this != CATEGORIES;
        }
        public boolean hideDueBy() {
            return this != DUE_BY;
        }
        public boolean hideSummary() {
            return this != SUMMARY_PAGE;
        }

        @Override
        public State next() {
            switch (this) {
                case DESCRIPTION: return CATEGORIES;
                case CATEGORIES: return DUE_BY;
                case DUE_BY: return SUMMARY_PAGE;
                default: return null;
            }
        }
        @Override
        public String disableNext(ToDoItemWizard w) {
            return w.getState().next() == null? "No more pages": null;
        }

        @Override
        public State previous() {
            switch (this) {
                case SUMMARY_PAGE: return DUE_BY;
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

    public String disableCategory() {
        return subcategory != null? "Use the update action to change both category and subcategory": null;
    }
    //endregion

    //region > subcategory (hidden property)
    private ToDoItem.Subcategory subcategory;

    @Disabled(reason = "Use the update action to change both category and subcategory")
    @Optional
    public ToDoItem.Subcategory getSubcategory() {
        return subcategory;
    }
    public void setSubcategory(final ToDoItem.Subcategory subcategory) {
        this.subcategory = subcategory;
    }

    public boolean hideSubcategory() {
        return subcategory == null || getState().hideCategories();
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

    //region > summary propertyies
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getDescriptionOnSummary() {
        return getDescription();
    }
    public boolean hideDescriptionOnSummary() {
        return getState().hideSummary();
    }

    public Category getCategoryOnSummary() {
        return getCategory();
    }
    public boolean hideCategoryOnSummary() {
        return getState().hideSummary();
    }

    public ToDoItem.Subcategory getSubcategoryOnSummary() {
        return getSubcategory();
    }
    public boolean hideSubcategoryOnSummary() {
        return getState().hideSummary() || getSubcategory() == null;
    }

    public LocalDate getDueByOnSummary() {
        return getDueBy();
    }
    public boolean hideDueByOnSummary() {
        return getState().hideSummary();
    }
    //endregion


    //region > finish (action)
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @MemberOrder(sequence = "1")
    public ToDoItem finish() {
        return toDoItems.newToDo(getDescription(), getCategory(), getSubcategory(), getDueBy(), null);
    }

    @Override
    public String disableFinish() {
        // no additional rules
        return null;
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
