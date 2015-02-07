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

import dom.todo.ToDoItem.Category;
import dom.todo.ToDoItem.Subcategory;

import java.util.List;
import java.util.concurrent.Callable;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import org.joda.time.LocalDate;
import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.applib.value.Clob;

@DomainService(nature = NatureOfService.VIEW_CONTRIBUTIONS_ONLY)
public class ToDoItemContributions extends AbstractFactoryAndRepository {

    //region > priority (contributed property)
    @Action(
            semantics = SemanticsOf.SAFE,
            hidden = Where.ALL_TABLES
    )
    @ActionLayout(
            describedAs = "The relative priority of this item compared to others not yet complete (using 'due by' date)",
            contributed = Contributed.AS_ASSOCIATION
    )
    @Property(
            editing = Editing.DISABLED,
            editingDisabledReason = "Relative priority, derived from due date"
    )
    public Integer relativePriority(final ToDoItem toDoItem) {
        return queryResultsCache.execute(new Callable<Integer>(){
            @Override
            public Integer call() throws Exception {
                if(toDoItem.isComplete()) {
                    return null;
                }

                // sort items, then locate this one
                int i=1;
                for (final ToDoItem each : sortedNotYetComplete()) {
                    if(each == toDoItem) {
                        return i;
                    }
                    i++;
                }
                return null;
            }}, ToDoItemContributions.class, "relativePriority", toDoItem);
    }

    private List<ToDoItem> sortedNotYetComplete() {
        return ORDERING_DUE_BY
        .compound(ORDERING_DESCRIPTION)
        .sortedCopy(toDoItems.notYetComplete());
    }

    private static final Ordering<ToDoItem> ORDERING_DUE_BY =
        Ordering.natural().nullsLast().onResultOf(new Function<ToDoItem, LocalDate>(){
            @Override
            public LocalDate apply(final ToDoItem input) {
                return input.getDueBy();
            }
        });
    
    private static final Ordering<ToDoItem> ORDERING_DESCRIPTION =
        Ordering.natural().nullsLast().onResultOf(new Function<ToDoItem, String>(){
            @Override
            public String apply(final ToDoItem input) {
                return input.getDescription();
            }
        });


    //endregion

    //region >  next, previous (contributed actions)
    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            describedAs = "The next item not yet completed",
            contributed = Contributed.AS_ACTION
    )
    public ToDoItem next(final ToDoItem item) {
        final Integer priority = relativePriority(item);
        if(priority == null) {
            return item;
        }
        int priorityOfNext = priority != null ? priority + 1 : 0;
        return itemWithPriorityElse(priorityOfNext, item);
    }
    public String disableNext(final ToDoItem toDoItem) {
        if (toDoItem.isComplete()) {
            return "Completed";
        }
        if(next(toDoItem) == null) {
            return "No next item";
        }
        return null;
    }

    // //////////////////////////////////////

    @ActionLayout(
            describedAs = "The previous item not yet completed",
            contributed = Contributed.AS_ACTION
    )
    @Action(semantics = SemanticsOf.SAFE)
    public ToDoItem previous(final ToDoItem item) {
        final Integer priority = relativePriority(item);
        if(priority == null) {
            return item;
        }
        int priorityOfPrevious = priority != null? priority - 1 : 0;
        return itemWithPriorityElse(priorityOfPrevious, item);
    }
    public String disablePrevious(final ToDoItem toDoItem) {
        if (toDoItem.isComplete()) {
            return "Completed";
        }
        if(previous(toDoItem) == null) {
            return "No previous item";
        }
        return null;
    }

    // //////////////////////////////////////

    /**
     * @param priority : 1-based priority
     */
    private ToDoItem itemWithPriorityElse(int priority, final ToDoItem itemElse) {
        if(priority < 1) {
            return null;
        }
        final List<ToDoItem> items = sortedNotYetComplete();
        if(priority > items.size()) {
            return null;
        }
        return priority>=0 && items.size()>=priority? items.get(priority-1): itemElse;
    }
    //endregion

    //region > similarTo (contributed collection)
    @ActionLayout(
            contributed = Contributed.AS_ASSOCIATION
    )
    @Action(semantics = SemanticsOf.SAFE)
    public List<ToDoItem> similarTo(final ToDoItem toDoItem) {
        final List<ToDoItem> similarToDoItems = allMatches(
                new QueryDefault<ToDoItem>(ToDoItem.class,
                        "findByOwnedByAndCategory", 
                        "ownedBy", currentUserName(), 
                        "category", toDoItem.getCategory()));
        return Lists.newArrayList(Iterables.filter(similarToDoItems, excluding(toDoItem)));
    }


    private static Predicate<ToDoItem> excluding(final ToDoItem toDoItem) {
        return new Predicate<ToDoItem>() {
            @Override
            public boolean apply(ToDoItem input) {
                return input != toDoItem;
            }
        };
    }
    //endregion

    //region > updateCategory (contributed action)

    @ActionLayout(
            describedAs = "Update category and subcategory"
    )
    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public Categorized updateCategory(
            final Categorized item,
            final @ParameterLayout(named="Category") Category category,
            final @Parameter(optionality = Optionality.OPTIONAL) @ParameterLayout(named="Subcategory") Subcategory subcategory) {
        item.setCategory(category);
        item.setSubcategory(subcategory);
        return item;
    }
    public Category default1UpdateCategory(
            final Categorized item) {
        return item != null? item.getCategory(): null;
    }
    public Subcategory default2UpdateCategory(
            final Categorized item) {
        return item != null? item.getSubcategory(): null;
    }

    public List<Subcategory> choices2UpdateCategory(
            final Categorized item, final Category category) {
        return Subcategory.listFor(category);
    }
    
    public String validateUpdateCategory(
            final Categorized item, final Category category, final Subcategory subcategory) {
        return Subcategory.validate(category, subcategory);
    }
    //endregion

    // region > exportAsJson (action)
    /**
     * Demonstrates functionality of streaming back Clob/Blob result within an action with a prompt, i.e. Ajax request
     */
    @Action(semantics = SemanticsOf.SAFE)
    public Clob exportAsJson(
            final ToDoItem toDoItem,
            @ParameterLayout(named = "File name") String fileName
    ) {
        if(!fileName.endsWith(".json")) {
            fileName += ".json";
        }
        return new Clob(
                fileName,
                "application/json",
                "{" +
                "\"description\": \"" + toDoItem.getDescription()+"\"" +
                ",\"complete\": " + ""+toDoItem.isComplete() +
                "}");
    }

    public String default1ExportAsJson() {
        return "todo";
    }
    //endregion


    //region > helpers
    protected String currentUserName() {
        return getContainer().getUser().getName();
    }
    //endregion

    //region > injected services
    @javax.inject.Inject
    private ToDoItems toDoItems;

    @javax.inject.Inject
    private QueryResultsCache queryResultsCache;
    //endregion

}
