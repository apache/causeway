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

import java.util.Optional;
import java.util.function.Predicate;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ExceptionRecognizerGeneralTest {

    private ExceptionRecognizer ersGeneral;

    static class FooException extends Exception {
        private static final long serialVersionUID = 1L;
        public FooException() {
            super("foo");
        }
    }

    private final static Predicate<Throwable> ALWAYS_TRUE = __->true;
    private final static Predicate<Throwable> ALWAYS_FALSE = __->false;

    @Test
    public void whenRecognized() {
        ersGeneral = new ExceptionRecognizerAbstract(ALWAYS_TRUE){};
        assertThat(ersGeneral.recognize(new FooException()).get().getReason(), is("foo"));
    }

    @Test
    public void whenDoesNotRecognize() {
        ersGeneral = new ExceptionRecognizerAbstract(ALWAYS_FALSE){};
        assertThat(ersGeneral.recognize(new FooException()), is(Optional.empty()));
    }

    @Test
    public void whenRecognizedWithMessageParser() {
        ersGeneral = new ExceptionRecognizerAbstract(ALWAYS_TRUE, s->"pre: " + s){};
        assertThat(ersGeneral.recognize(new FooException()).get().getReason(), is("pre: foo"));
    }

}
