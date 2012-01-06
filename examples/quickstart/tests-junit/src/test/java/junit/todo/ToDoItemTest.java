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

package junit.todo;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import junit.AbstractTest;

import org.junit.Before;
import org.junit.Test;

import dom.todo.ToDoItem;
import fixture.todo.ToDoItemsFixture;

import org.apache.isis.progmodel.wrapper.applib.DisabledException;
import org.apache.isis.viewer.junit.Fixture;
import org.apache.isis.viewer.junit.Fixtures;

@Fixtures({ @Fixture(ToDoItemsFixture.class) })
public class ToDoItemTest extends AbstractTest {

    private ToDoItem firstItem;

    @Override
    @Before
    public void setUp() {
        firstItem = wrapped(toDoItems.notYetDone().get(0));
    }
    
    @Test
    public void canMarkAsComplete() throws Exception {
        firstItem.markAsDone();
        assertThat(firstItem.isComplete(), is(true));
    }

    @Test
    public void cannotMarkAsCompleteTwice() throws Exception {
        firstItem.markAsDone();
        try {
            firstItem.markAsDone();
            fail("Should have been disabled");
        } catch (DisabledException e) {
            assertThat(e.getMessage(), is("Already done"));
        }
    }


}
