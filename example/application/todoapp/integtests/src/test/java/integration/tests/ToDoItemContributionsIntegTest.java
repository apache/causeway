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
package integration.tests;

import dom.todo.ToDoItem;
import dom.todo.ToDoItemContributions;
import dom.todo.ToDoItems;
import fixture.todo.scenarios.ToDoItemsRecreateAndCompleteSeveral;

import java.util.List;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.applib.fixturescripts.FixtureScripts;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public abstract class ToDoItemContributionsIntegTest extends AbstractToDoIntegTest {

    ToDoItemsRecreateAndCompleteSeveral fixtureScript;

    @Before
    public void setUpData() throws Exception {
        fixtureScript = new ToDoItemsRecreateAndCompleteSeveral();
        fixtureScripts.runFixtureScript(fixtureScript, null);
    }

    @Inject
    FixtureScripts fixtureScripts;
    @Inject
    ToDoItems toDoItems;
    @Inject
    ToDoItemContributions toDoItemContributions;

    ToDoItemContributions toDoItemContributionsWrapped;
    ToDoItem toDoItem;

    @Before
    public void setUp() throws Exception {

        toDoItem = wrap(fixtureScript.lookup("to-do-items-recreate-and-complete-several/to-do-item-complete-for-buy-stamps/item-1", ToDoItem.class));
        assertThat(toDoItem, is(not(nullValue())));

        toDoItemContributionsWrapped = wrap(toDoItemContributions);
    }

    public static class Actions {
        public static class UpdateCategory extends ToDoItemContributionsIntegTest {

            @Test
            public void happyCase() throws Exception {

                // when
                toDoItemContributionsWrapped.updateCategory(toDoItem, ToDoItem.Category.Professional, ToDoItem.Subcategory.Consulting);

                // then
                assertThat(toDoItem.getCategory(), is(ToDoItem.Category.Professional));
                assertThat(toDoItem.getSubcategory(), is(ToDoItem.Subcategory.Consulting));

                // when
                toDoItemContributionsWrapped.updateCategory(toDoItem, ToDoItem.Category.Domestic, ToDoItem.Subcategory.Chores);

                // then
                assertThat(toDoItem.getCategory(), is(ToDoItem.Category.Domestic));
                assertThat(toDoItem.getSubcategory(), is(ToDoItem.Subcategory.Chores));
            }


            @Test
            public void categoryCannotBeNull() throws Exception {

                // when, then
                expectedExceptions.expectMessage("'Category' is mandatory");
                toDoItemContributionsWrapped.updateCategory(toDoItem, null, ToDoItem.Subcategory.Chores);
            }

            @Test
            public void subcategoryCanBeNull() throws Exception {

                // when, then
                toDoItemContributionsWrapped.updateCategory(toDoItem, ToDoItem.Category.Professional, null);
            }

            @Test
            public void subcategoryMustBelongToCategory() throws Exception {

                // when, then
                expectedExceptions.expectMessage(containsString("Invalid subcategory"));
                toDoItemContributionsWrapped.updateCategory(toDoItem, ToDoItem.Category.Professional, ToDoItem.Subcategory.Chores);
            }
        }

        public static class SimilarTo extends ToDoItemContributionsIntegTest {

            @Test
            public void happyCase() throws Exception {

                // when
                List<ToDoItem> similarItems = toDoItemContributionsWrapped.similarTo(toDoItem);

                // then
                assertThat(similarItems.size(), is(6));
            }

        }
    }

    public static class Properties {
        public static class Priority extends ToDoItemContributionsIntegTest {

            private List<ToDoItem> notYetComplete;

            @Before
            public void setUp() throws Exception {
                notYetComplete = wrap(toDoItems).notYetComplete();
            }

            @Test
            public void happyCase() throws Exception {
                assertPriority(0, 1);
                assertPriority(1, 2);
                assertPriority(2, 4);
                assertPriority(3, 6);
                assertPriority(4, 5);
                assertPriority(5, 7);
                assertPriority(6, 9);
                assertPriority(7, 8);
                assertPriority(8, 3);
                assertPriority(9, 10);
            }

            private void assertPriority(final int n, final int priority) {
                assertThat(toDoItemContributions.relativePriority(notYetComplete.get(n)), is(Integer.valueOf(priority)));
            }
        }
    }

}