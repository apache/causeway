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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

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
        @Override
        public void init(Map<String, String> properties) {
        }

        @Override
        public void shutdown() {
        }
    }
    
    @Before
    public void setUp() throws Exception {
        composite = new ExceptionRecognizerComposite();
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
