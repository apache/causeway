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
package org.apache.causeway.applib.services.exceprecog;

import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExceptionRecognizerForTypeTest {

    private ExceptionRecognizer ersForType;

    static class FooException extends Exception {
        private static final long serialVersionUID = 1L;
        public FooException() {
            super("foo");
        }

    }
    static class BarException extends Exception {
        private static final long serialVersionUID = 1L;
        public BarException() {
            super("bar");
        }
    }

    private Function<Throwable, String> rootCauseMessageFormatter = ex -> "pre: " + ex.getMessage();

    @Test
    public void whenRecognized() {
        ersForType = new ExceptionRecognizerForType(FooException.class);
        assertThat(ersForType.recognize(new FooException()).get().getReason(), is("foo"));
    }

    @Test
    public void whenDoesNotRecognize() {
        ersForType = new ExceptionRecognizerForType(FooException.class);
        assertThat(ersForType.recognize(new BarException()), is(Optional.empty()));
    }

    @Test
    public void whenRecognizedWithMessageParser() {
        ersForType = new ExceptionRecognizerForType(FooException.class, rootCauseMessageFormatter);
        assertThat(ersForType.recognize(new FooException()).get().getReason(), is("pre: foo"));
    }


}
