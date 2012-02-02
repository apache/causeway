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

package org.apache.isis.runtimes.embedded;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.runtimes.embedded.dom.claim.ClaimRepositoryImpl;
import org.apache.isis.runtimes.embedded.dom.employee.EmployeeRepositoryImpl;

@RunWith(JMock.class)
public class IsisMetaModelTest_constructWithServices {

    private final Mockery mockery = new JUnit4Mockery();

    private EmbeddedContext mockContext;

    private IsisMetaModel metaModel;

    @Before
    public void setUp() {
        mockContext = mockery.mock(EmbeddedContext.class);
    }

    @Test
    public void shouldSucceedWithoutThrowingAnyExceptions() {
        metaModel = new IsisMetaModel(mockContext);
    }

    @Test
    public void shouldBeAbleToRegisterServices() {
        metaModel = new IsisMetaModel(mockContext, new EmployeeRepositoryImpl(), new ClaimRepositoryImpl());
        final List<Object> services = metaModel.getServices();
        assertThat(services.size(), is(2));
        assertThat(services, contains(EmployeeRepositoryImpl.class));
        assertThat(services, contains(ClaimRepositoryImpl.class));
    }

    private Matcher<List<Object>> contains(final Class<?> cls) {
        return new TypeSafeMatcher<List<Object>>() {

            @Override
            public void describeTo(final Description desc) {
                desc.appendText("contains instance of type " + cls.getName());
            }

            @Override
            public boolean matchesSafely(final List<Object> items) {
                for (final Object object : items) {
                    if (cls.isAssignableFrom(object.getClass())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

}
