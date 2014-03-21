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
package dom.todo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class ToDoTest_notYetCompleted {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private EventBusService eventBusService;
    
    private ToDoItem toDoItem;

    @Before
    public void setUp() throws Exception {
        toDoItem = new ToDoItem();

        toDoItem.bulkInteractionContext = Bulk.InteractionContext.regularAction(toDoItem);
        toDoItem.eventBusService = eventBusService;

        context.ignoring(eventBusService);
        toDoItem.setComplete(true);
    }
    
    @Test
    public void happyCase() throws Exception {
        // given
        assertThat(toDoItem.disableNotYetCompleted(), is(nullValue()));
        
        // when
        toDoItem.notYetCompleted();
        
        // then
        assertThat(toDoItem.isComplete(), is(false));
        assertThat(toDoItem.disableNotYetCompleted(), is(not(nullValue())));
    }
    
}
