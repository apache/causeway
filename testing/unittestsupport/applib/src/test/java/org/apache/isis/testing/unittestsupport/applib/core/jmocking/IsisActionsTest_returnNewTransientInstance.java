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
package org.apache.isis.testing.unittestsupport.applib.core.jmocking;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.testing.unittestsupport.applib.jmocking.IsisActions;
import org.apache.isis.testing.unittestsupport.applib.jmocking.JUnitRuleMockery2;

public class IsisActionsTest_returnNewTransientInstance {

    // we can't use the 'real' DomainObjectConainter because applib depends on this module, not vice versa
    // but it doesn't matter; we are just testing the action (of the expectation), not the object on which
    // we add the expectation
    public static interface MyDomainObjectContainer {
        <T> T newTransientInstance(Class<T> t);

        void persistIfNotAlready(Object o);
    }

    public static class MyCustomer  {
    }

    @Mock
    private MyDomainObjectContainer mockContainer;

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);


    @Test
    public void testIt() {

        context.checking(new Expectations() {
            {
                allowing(mockContainer).newTransientInstance(with(anySubclassOf(Object.class)));
                will(IsisActions.returnNewTransientInstance());
                ignoring(mockContainer);
            }
        });

        // is allowed (and executed)
        MyCustomer o = mockContainer.newTransientInstance(MyCustomer.class);
        assertThat(o, is(not(nullValue())));

        // is ignored
        mockContainer.persistIfNotAlready(o);
    }


    private static <X> Matcher<Class<X>> anySubclassOf(final Class<X> cls) {
        return new TypeSafeMatcher<Class<X>>() {

            @Override
            public void describeTo(final Description arg0) {
                arg0.appendText("is subclass of ").appendText(cls.getName());
            }

            @Override
            public boolean matchesSafely(final Class<X> item) {
                return cls.isAssignableFrom(item);
            }
        };
    }


}
