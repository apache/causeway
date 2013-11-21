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
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import dom.todo.ToDoItem;
import dom.todo.ToDoItem.Category;
import dom.todo.ToDoItem.Subcategory;
import dom.todo.ToDoItems;

import org.apache.isis.applib.AbstractViewModel;
import org.apache.isis.applib.annotation.Bookmarkable;
import org.apache.isis.applib.annotation.MultiLine;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.applib.util.ObjectContracts;

@Named("By Category")
@Bookmarkable
public class ToDoItemsByCategoryViewModel 
        extends AbstractViewModel 
        implements Comparable<ToDoItemsByCategoryViewModel> {

    
    // //////////////////////////////////////
    // ViewModel implementation
    // //////////////////////////////////////

    @Override
    public String viewModelMemento() {
        return getCategory().name();
    }

    @Override
    public void viewModelInit(String memento) {
        setCategory(Category.valueOf(memento));
    }

    // //////////////////////////////////////
    // Category
    // //////////////////////////////////////

    private Category category;

    /**
     * Used as {@link #viewModelMemento() memento}
     */
    @Title
    public Category getCategory() {
        return category;
    }

    public void setCategory(final Category category) {
        this.category = category;
    }


    // //////////////////////////////////////
    // NotYetComplete, Complete
    // //////////////////////////////////////

    @MultiLine(numberOfLines=5)
    public String getNotYetComplete() {
        final List<ToDoItem> notYetComplete = getItemsNotYetComplete();
        return Joiner.on(", ").join(
            Iterables.transform(subcategories(), summarizeBySubcategory(notYetComplete)));
    }

    // //////////////////////////////////////

    @MultiLine(numberOfLines=5)
    public String getComplete() {
        final List<ToDoItem> completeInCategory = getItemsComplete();
        return Joiner.on(", ").join(
            Iterables.transform(subcategories(), summarizeBySubcategory(completeInCategory)));
    }

    // //////////////////////////////////////

    private Iterable<Subcategory> subcategories() {
        return Iterables.filter(Arrays.asList(Subcategory.values()), Subcategory.thoseFor(getCategory()));
    }

    private Function<Subcategory, String> summarizeBySubcategory(final Iterable<ToDoItem> itemsInCategory) {
        return new Function<Subcategory, String>() {
            @Override
            public String apply(final Subcategory subcategory) {
                return subcategory + ": " + countIn(itemsInCategory, subcategory);
            }
        };
    }

    private static int countIn(final Iterable<ToDoItem> items, final Subcategory subcategory) {
        return Iterables.size(Iterables.filter(items, 
                ToDoItem.Predicates.thoseSubcategorised(subcategory)));
    }

    
    // //////////////////////////////////////
    // getItemsNotYetComplete, getItemsComplete
    // //////////////////////////////////////

    /**
     * All those items {@link ToDoItems#notYetComplete() not yet complete}, for this {@link #getCategory() category}.
     */
    @Render(Type.EAGERLY)
    public List<ToDoItem> getItemsNotYetComplete() {
        final List<ToDoItem> notYetComplete = toDoItems.notYetCompleteNoUi();
        return Lists.newArrayList(Iterables.filter(notYetComplete, ToDoItem.Predicates.thoseCategorised(getCategory())));
    }

    // //////////////////////////////////////

    /**
     * All those items {@link ToDoItems#complete() complete}, for this {@link #getCategory() category}.
     */
    @Render(Type.EAGERLY)
    public List<ToDoItem> getItemsComplete() {
        final List<ToDoItem> complete = toDoItems.completeNoUi();
        return Lists.newArrayList(Iterables.filter(complete, ToDoItem.Predicates.thoseCategorised(getCategory())));
    }

    
    // //////////////////////////////////////
    // DeleteCompleted (action)
    // //////////////////////////////////////

    @Named("Delete")
    public ToDoItemsByCategoryViewModel deleteCompleted() {
        for (ToDoItem item : getItemsComplete()) {
            removeIfNotAlready(item);
        }
        // force reload of page
        return this;
    }
    
    

    // //////////////////////////////////////
    // compareTo
    // //////////////////////////////////////

    @Override
    public int compareTo(ToDoItemsByCategoryViewModel other) {
        return ObjectContracts.compare(this, other, "category");
    }

    
    // //////////////////////////////////////
    // injected services
    // //////////////////////////////////////
    
    @javax.inject.Inject
    private ToDoItems toDoItems;

}
