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
package integration.tests.events;

import integration.tests.ToDoIntegTest;

import java.util.List;

import dom.todo.ToDoItem;
import dom.todo.ToDoItems;
import fixture.todo.integtests.ToDoItemsIntegTestFixture;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.eventbus.CollectionAddedToEvent;
import org.apache.isis.applib.services.eventbus.EventBusService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.Subscribe;

public class ToDoItemTest_dependencies_addedEvent extends ToDoIntegTest {

    private ToDoItem toDoItem;
    private ToDoItem otherToDoItem;
    private EventBusService eventBusService;
    
    
    public class TestSubscription {
    	
        @Programmatic
        @Subscribe
        public Boolean on(CollectionAddedToEvent<?,?> ev) {
            return true;
        }
    	
    }
    

    @Before
    public void setUp() throws Exception {
        scenarioExecution().install(new ToDoItemsIntegTestFixture());

        final List<ToDoItem> items = wrap(service(ToDoItems.class)).notYetComplete();
        toDoItem = wrap(items.get(0));
        otherToDoItem = items.get(1); // wrapping this seems to trip up cglib :-(
        eventBusService = this.service(EventBusService.class);
        
        // Register new Service.
        
    }

    @After
    public void tearDown() throws Exception {
        unwrap(toDoItem).getDependencies().clear();
    }

    @Test
    public void collectionAddedToEventReceived() throws Exception {

        // given
        
        // when
        toDoItem.add(otherToDoItem);
        
        // then
    }

}