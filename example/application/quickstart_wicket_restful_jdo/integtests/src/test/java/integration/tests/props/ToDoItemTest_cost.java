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
package integration.tests.props;

import dom.todo.ToDoItem;
import dom.todo.ToDoItems;
import fixture.todo.integtests.ToDoItemsIntegTestFixture;
import integration.tests.ToDoIntegTest;

import java.math.BigDecimal;
import java.util.List;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ToDoItemTest_cost extends ToDoIntegTest {

    @Before
    public void setUpData() throws Exception {
        scenarioExecution().install(new ToDoItemsIntegTestFixture());
    }

    @Inject
    private ToDoItems toDoItems;

    private ToDoItem toDoItem;
    private BigDecimal cost;

    @Before
    public void setUp() throws Exception {
        final List<ToDoItem> all = wrap(toDoItems).notYetComplete();
        toDoItem = wrap(all.get(0));
        cost = toDoItem.getCost();
    }

    @Test
    public void happyCaseUsingProperty() throws Exception {
        
        final BigDecimal newCost = new BigDecimal("123.45");
        
        // when
        toDoItem.updateCost(newCost);
        
        // then
        assertThat(toDoItem.getCost(), is(newCost));
    }

    @Test
    public void happyCaseUsingAction() throws Exception {
        
        final BigDecimal newCost = new BigDecimal("123.45");
        
        // when
        toDoItem.updateCost(newCost);
        
        // then
        assertThat(toDoItem.getCost(), is(newCost));
    }
    
    @Test
    public void canBeNull() throws Exception {
        
        // when
        toDoItem.updateCost((BigDecimal)null);
        
        // then
        assertThat(toDoItem.getCost(), is((BigDecimal)null));
    }

    @Test
    public void defaultForAction() throws Exception {
        
        // then
        assertThat(unwrap(toDoItem).default0UpdateCost(), is(cost));
    }
    
    
}