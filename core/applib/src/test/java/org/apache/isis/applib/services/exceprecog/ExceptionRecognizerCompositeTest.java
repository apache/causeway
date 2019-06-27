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
package org.apache.isis.applib.services.exceprecog;

import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ExceptionRecognizerCompositeTest {

    private ExceptionRecognizerComposite composite;

    static class FakeERS implements ExceptionRecognizer {
        private String message;
        public FakeERS(String message) {
            this.message = message;
        }
        @Override
        public String recognize(Throwable ex) {
            return message;
        }

        @Override public Recognition recognize2(final Throwable ex) {
            return Recognition.of(Category.OTHER, message);
        }

        @Override
        public void init() {
        }

        @Override
        public void shutdown() {
        }
    }

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    private ServiceRegistry mockServiceRegistry;

    @Mock
    private TranslationService mockTranslationService;

    @Before
    public void setUp() throws Exception {
        composite = new ExceptionRecognizerComposite();
        composite.serviceRegistry = mockServiceRegistry;
        composite.translationService = mockTranslationService;

        context.ignoring(mockServiceRegistry);
        context.ignoring(mockTranslationService);
    }
    
    @Test
    public void whenEmpty() {
        assertThat(composite.recognize(new RuntimeException()), is(nullValue()));
    }

    @Test
    public void whenOne() {
        composite.add(new FakeERS("one"));
        assertThat(composite.recognize(new RuntimeException()), is("one"));
    }

    @Test
    public void whenNullThenOne() {
        composite.add(new FakeERS(null));
        composite.add(new FakeERS("one"));
        assertThat(composite.recognize(new RuntimeException()), is("one"));
    }

    @Test
    public void whenOneThenTwo() {
        composite.add(new FakeERS("one"));
        composite.add(new FakeERS("two"));
        assertThat(composite.recognize(new RuntimeException()), is("one"));
    }
}
