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

package org.apache.isis.applib;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class FactoryAndRepositoryTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);


    public static class TestDomainObject {
    }

    @Mock
    private DomainObjectContainer container;
    
    private AbstractFactoryAndRepository object;

    @Before
    public void setUp() throws Exception {
        container = context.mock(DomainObjectContainer.class);
        object = new AbstractFactoryAndRepository() {
        };
        object.setContainer(container);
    }

    @Test
    public void testContainer() throws Exception {
        assertEquals(container, object.getContainer());
    }

    @Test
    public void testInformUser() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(container).informUser("message");
            }
        });

        object.informUser("message");
    }

    @Test
    public void testWarnUser() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(container).warnUser("message");
            }
        });

        object.warnUser("message");
    }

    @Test
    public void testRaiseError() throws Exception {
        context.checking(new Expectations() {
            {
                oneOf(container).raiseError("message");
            }
        });

        object.raiseError("message");
    }

    @Test
    public void testAllInstances() throws Exception {
        final List<Object> list = new ArrayList<Object>();
        list.add(new TestDomainObject());
        list.add(new TestDomainObject());
        list.add(new TestDomainObject());

        context.checking(new Expectations() {
            {
                oneOf(container).allInstances(TestDomainObject.class);
                will(returnValue(list));
            }
        });

        final List<TestDomainObject> allInstances = object.allInstances(TestDomainObject.class);
        assertThat(allInstances.size(), is(3));
        assertThat(allInstances.get(0), notNullValue());
        assertThat(allInstances.get(0), equalTo(list.get(0)));
        assertThat(allInstances.get(1), equalTo(list.get(1)));
        assertThat(allInstances.get(2), equalTo(list.get(2)));
    }

}
