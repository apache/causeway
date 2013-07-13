/*
 *  Copyright 2012-2013 Eurocommercial Properties NV
 *
 *  Licensed under the Apache License, Version 2.0 (the
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
package integration.specs.todoitem;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import dom.todo.ToDoItem;
import dom.todo.ToDoItems;
import fixture.todo.ToDoItemsFixture;

import org.jmock.Expectations;
import org.jmock.Mockery;

import org.apache.isis.core.specsupport.scenarios.InMemoryDB;
import org.apache.isis.core.specsupport.scenarios.ScenarioExecutionScope;
import org.apache.isis.core.specsupport.specs.CukeStepDefsAbstract;

public class ToDoItemStepDefs extends CukeStepDefsAbstract {


    // //////////////////////////////////////
    
    private List<ToDoItem> items;

    @Before({"@unit"})
    public void beforeScenarioUnitScope() {
        before(ScenarioExecutionScope.UNIT);
    }

    @Before({"@integration"})
    public void beforeScenarioIntegrationScope() {
        before(ScenarioExecutionScope.INTEGRATION);
    }

    @After
    public void afterScenario(cucumber.api.Scenario sc) {
        after(sc);
    }

    // //////////////////////////////////////
    
    

    @Before(value={"@unit"}, order=20000)
    public void unitFixtures() throws Throwable {
        final InMemoryDB inMemoryDB = new InMemoryDBForToDoApp(this.scenarioExecution());
        final ToDoItem t1 = inMemoryDB.getElseCreate(ToDoItem.class, "Write blog post");
        final ToDoItem t2 = inMemoryDB.getElseCreate(ToDoItem.class, "Pick up bread");
        items = Arrays.asList(t1, t2);
    }

    private static ArrayList<ToDoItem> notYetComplete(List<ToDoItem> items) {
        return Lists.newArrayList(Iterables.filter(items, new Predicate<ToDoItem>(){

            @Override
            public boolean apply(ToDoItem input) {
                return !input.isComplete();
            }
        }));
    }

    @Before(value={"@integration"}, order=20000)
    public void integrationFixtures() throws Throwable {
        scenarioExecution().install(new ToDoItemsFixture());
    }
    
    // //////////////////////////////////////
    

    @Given("^there are a number of incomplete ToDo items$")
    public void there_are_a_number_of_incomplete_ToDo_items() throws Throwable {
        checking(new Expectations() {
            {
                allowing(service(ToDoItems.class)).notYetComplete();
                will(returnValue(notYetComplete(items)));
            }
        });

        final List<ToDoItem> notYetComplete = service(ToDoItems.class).notYetComplete();
        assertThat(notYetComplete.isEmpty(), is(false));
        put("list", "notYetComplete", notYetComplete);
    }
    
    @When("^I choose the first one$")
    public void I_choose_the_first_one() throws Throwable {
        @SuppressWarnings("unchecked")
        List<ToDoItem> notYetComplete = get(null, "notYetComplete", List.class);
        assertThat(notYetComplete.isEmpty(), is(false));
        
        put("todo", "firstToDo", notYetComplete.get(0));
    }
    
    @When("^mark it as complete$")
    public void mark_it_as_complete() throws Throwable {
        ToDoItem toDoItem = get(null, "firstToDo", ToDoItem.class);
        wrap(toDoItem).completed();
    }
    
    @Then("^the item is no longer listed as incomplete$")
    public void the_item_is_no_longer_listed_as_incomplete() throws Throwable {
        assertIsSatisfied();
        Mockery m;
        
        final ArrayList<ToDoItem> notYetCompleteItems = notYetComplete(items);
        checking(new Expectations() {
            {
                oneOf(service(ToDoItems.class)).notYetComplete();
                will(returnValue(notYetCompleteItems));
            }
        });

        final List<ToDoItem> notYetComplete = service(ToDoItems.class).notYetComplete();
        ToDoItem toDoItem = get(null, "firstToDo", ToDoItem.class);
        
        assertThat(notYetComplete.contains(toDoItem), is(false));
    }
    
    
}
