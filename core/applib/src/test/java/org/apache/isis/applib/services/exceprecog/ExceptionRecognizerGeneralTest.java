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

import java.util.function.Predicate;

import org.junit.Test;

public class ExceptionRecognizerGeneralTest {

    private ExceptionRecognizerAbstract ersGeneral;

    static class FooException extends Exception {
        private static final long serialVersionUID = 1L;
        public FooException() {
            super("foo");
        }
    }
    
    private com.google.common.base.Function<String,String> prepend = new com.google.common.base.Function<String, String>() {
        @Override
        public String apply(String input) {
            return "pre: " + input;
        }
    };
    
    
    @Test
    public void whenRecognized_guava() {
        ersGeneral = new ExceptionRecognizerAbstract(com.google.common.base.Predicates.<Throwable>alwaysTrue()){};
        assertThat(ersGeneral.recognize(new FooException()), is("foo"));
    }

    @Test
    public void whenDoesNotRecognize_guava() {
        ersGeneral = new ExceptionRecognizerAbstract(com.google.common.base.Predicates.<Throwable>alwaysFalse()){};
        assertThat(ersGeneral.recognize(new FooException()), is(nullValue()));
    }

    @Test
    public void whenRecognizedWithMessageParser_guava() {
        ersGeneral = new ExceptionRecognizerAbstract(com.google.common.base.Predicates.<Throwable>alwaysTrue(), prepend){};
        assertThat(ersGeneral.recognize(new FooException()), is("pre: foo"));
    }
    
    private final static Predicate<Throwable> ALWAYS_TRUE = __->true;
    private final static Predicate<Throwable> ALWAYS_FALSE = __->false;
    
    @Test
    public void whenRecognized() {
        ersGeneral = new ExceptionRecognizerAbstract(ALWAYS_TRUE){};
        assertThat(ersGeneral.recognize(new FooException()), is("foo"));
    }

    @Test
    public void whenDoesNotRecognize() {
        ersGeneral = new ExceptionRecognizerAbstract(ALWAYS_FALSE){};
        assertThat(ersGeneral.recognize(new FooException()), is(nullValue()));
    }

    @Test
    public void whenRecognizedWithMessageParser() {
        ersGeneral = new ExceptionRecognizerAbstract(ALWAYS_TRUE, s->"pre: " + s){};
        assertThat(ersGeneral.recognize(new FooException()), is("pre: foo"));
    }

}
