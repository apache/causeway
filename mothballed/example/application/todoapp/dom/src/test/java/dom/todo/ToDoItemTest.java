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

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.services.actinvoc.ActionInvocationContext;
import org.apache.isis.applib.security.RoleMemento;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public abstract class ToDoItemTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    EventBusService eventBusService;

    ToDoItem toDoItem;

    @Before
    public void setUp() throws Exception {
        toDoItem = new ToDoItem();

        toDoItem.actionInvocationContext = ActionInvocationContext.onObject(toDoItem);
        toDoItem.eventBusService = eventBusService;

        context.ignoring(eventBusService);
    }

    public static class Properties extends ToDoItemTest {

        @Mock
        DomainObjectContainer mockContainer;

        public static class DueBy extends Properties {

            @Test
            public void hiddenForNoDueByRole() {
                final UserMemento userWithRole = new UserMemento("user", new RoleMemento("realm1:noDueBy_role"));
                context.checking(new Expectations() {{
                    allowing(mockContainer).getUser();
                    will(returnValue(userWithRole));
                }});

                toDoItem.container = mockContainer;

                assertThat(toDoItem.hideDueBy(), is(true));
            }

            @Test
            public void notHiddenWithoutRole() {
                final UserMemento userWithRole = new UserMemento("user", new RoleMemento("realm1:someOtherRole"));
                context.checking(new Expectations() {{
                    allowing(mockContainer).getUser();
                    will(returnValue(userWithRole));
                }});

                toDoItem.container = mockContainer;

                assertThat(toDoItem.hideDueBy(), is(false));
            }
        }

    }

    public static class Actions extends ToDoItemTest {

        public static class Completed extends Actions {

            @Test
            public void happyCase() throws Exception {

                // given
                toDoItem.setComplete(false);
                assertThat(toDoItem.disableCompleted(), is(nullValue()));

                // when
                toDoItem.completed();

                // then
                assertThat(toDoItem.isComplete(), is(true));
                assertThat(toDoItem.disableCompleted(), is(not(nullValue())));
            }
        }

        public static class NotYetCompleted extends Actions {

            @Test
            public void happyCase() throws Exception {

                // given
                toDoItem.setComplete(true);
                assertThat(toDoItem.disableNotYetCompleted(), is(nullValue()));

                // when
                toDoItem.notYetCompleted();

                // then
                assertThat(toDoItem.isComplete(), is(false));
                assertThat(toDoItem.disableNotYetCompleted(), is(not(nullValue())));
            }
        }
    }


}
