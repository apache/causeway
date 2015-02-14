/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package integration.glue.todoitem;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import dom.todo.ToDoItem;
import dom.todo.ToDoItems;

import java.util.List;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.jmock.Expectations;
import org.junit.Assert;
import org.apache.isis.applib.services.actinvoc.ActionInvocationContext;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.specsupport.scenarios.InMemoryDB;
import org.apache.isis.core.specsupport.specs.CukeGlueAbstract;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ToDoItemGlue extends CukeGlueAbstract {

    @Given("^there are a number of incomplete ToDo items$")
    public void there_are_a_number_of_incomplete_ToDo_items() throws Throwable {
        if(supportsMocks()) {
            checking(new Expectations() {
                {
                    allowing(service(ToDoItems.class)).notYetComplete();
                    will(returnValue(notYetCompleteItems()));
                }
            });
        }
        try {
            final List<ToDoItem> notYetComplete = service(ToDoItems.class).notYetComplete();
            assertThat(notYetComplete.isEmpty(), is(false));
            putVar("list", "notYetCompleteItems", notYetComplete);
            
        } finally {
            assertMocksSatisfied();
        }
    }
    
    @When("^I choose the first of the incomplete items$")
    public void I_choose_the_first_one() throws Throwable {
        @SuppressWarnings("unchecked")
        List<ToDoItem> notYetComplete = getVar(null, "notYetCompleteItems", List.class);
        assertThat(notYetComplete.isEmpty(), is(false));
        
        putVar("todo", "toDoItem", notYetComplete.get(0));
    }
    
    @When("^mark the item as complete$")
    public void mark_it_as_complete() throws Throwable {
        final ToDoItem toDoItem = getVar(null, "toDoItem", ToDoItem.class);
        if(supportsMocks()) {
            final ActionInvocationContext actionInvocationContext = service(ActionInvocationContext.class);
            final EventBusService eventBusService = service(EventBusService.class);
            checking(new Expectations() {
                {
                    allowing(actionInvocationContext);
                    allowing(eventBusService);
                }
            });
            toDoItem.actionInvocationContext = actionInvocationContext;
            toDoItem.eventBusService = eventBusService;
        }
        wrap(toDoItem).completed();
    }
    
    @Then("^the item is no longer listed as incomplete$")
    public void the_item_is_no_longer_listed_as_incomplete() throws Throwable {
        ToDoItem toDoItem = getVar(null, "toDoItem", ToDoItem.class);
        whetherNotYetCompletedContains(toDoItem, false);
    }

    @Given("^.*completed .*item$")
    public void a_completed_ToDo_item() throws Throwable {
        if(supportsMocks()) {
            checking(new Expectations(){{
                allowing(service(ToDoItems.class)).allToDos();
                will(returnValue(findItems(Predicates.<ToDoItem>alwaysTrue()) ));
            }});
        }
        try {
            final List<ToDoItem> allToDos = service(ToDoItems.class).allToDos();
            for (ToDoItem toDoItem : allToDos) {
                if(toDoItem.isComplete()) {
                    putVar("todo", "toDoItem", toDoItem);
                    return;
                }
            }
            Assert.fail("could not locate any completed ToDo items");
        } finally {
            assertMocksSatisfied();
        }
    }

    @When("^I mark the .*item as not yet complete$")
    public void I_mark_it_as_not_yet_complete() throws Throwable {
        ToDoItem toDoItem = getVar(null, "toDoItem", ToDoItem.class);
        assertThat(toDoItem.isComplete(), is(true));
        
        toDoItem.setComplete(false);
    }

    @Then("^the .*item is listed as incomplete$")
    public void the_item_is_listed_as_incomplete() throws Throwable {
        ToDoItem toDoItem = getVar(null, "toDoItem", ToDoItem.class);
        whetherNotYetCompletedContains(toDoItem, true);
    }

    private void whetherNotYetCompletedContains(ToDoItem toDoItem, final boolean whetherContained) {
        if(supportsMocks()) {
            final List<ToDoItem> notYetCompleteItems = notYetCompleteItems();
            checking(new Expectations() {
                {
                    oneOf(service(ToDoItems.class)).notYetComplete();
                    will(returnValue(notYetCompleteItems));
                }
            });
        }
        try {
            final List<ToDoItem> notYetComplete = service(ToDoItems.class).notYetComplete();
            assertThat(notYetComplete.contains(toDoItem), is(whetherContained));
        } finally {
            assertMocksSatisfied();
        }
    }


    // helper
    private List<ToDoItem> notYetCompleteItems() {
        return findItems(new Predicate<ToDoItem>(){
            @Override
            public boolean apply(ToDoItem input) {
                return !input.isComplete();
            }
        });
    }

    private List<ToDoItem> findItems(final Predicate<ToDoItem> predicate) {
        final InMemoryDB inMemoryDB = getVar("isis", "in-memory-db", InMemoryDB.class);
        final List<ToDoItem> items = inMemoryDB.findAll(ToDoItem.class);
        return Lists.newArrayList(Iterables.filter(items, predicate));
    }
}
