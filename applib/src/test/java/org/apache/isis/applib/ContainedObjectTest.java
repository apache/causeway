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

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.security.UserMemento;

public class ContainedObjectTest {

    private DomainObjectContainer container;
    private AbstractContainedObject object;
    private Mockery context;

    @Before
    public void setUp() throws Exception {
        context = new Mockery();
        container = context.mock(DomainObjectContainer.class);
        object = new AbstractContainedObject() {
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
                one(container).informUser("message");
            }
        });

        object.informUser("message");

        context.assertIsSatisfied();
    }

    @Test
    public void testWarnUser() throws Exception {
        context.checking(new Expectations() {
            {
                one(container).warnUser("message");
            }
        });

        object.warnUser("message");

        context.assertIsSatisfied();
    }

    @Test
    public void testRaiseError() throws Exception {
        context.checking(new Expectations() {
            {
                one(container).raiseError("message");
            }
        });

        object.raiseError("message");

        context.assertIsSatisfied();
    }

    @Test
    public void testGetUser() throws Exception {
        final UserMemento memento = new UserMemento("Harry");
        context.checking(new Expectations() {
            {
                one(container).getUser();
                will(returnValue(memento));

            }
        });

        final UserMemento user = object.getUser();
        assertEquals(memento, user);

        context.assertIsSatisfied();
    }

}
