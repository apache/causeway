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

import java.util.function.Function;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

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
    
    private Function<String,String> prepend = new Function<String, String>() {
        @Override
        public String apply(String input) {
            return "pre: " + input;
        }
    };
    
    @Test
    public void whenRecognized() {
        ersForType = new ExceptionRecognizerForType(FooException.class);
        assertThat(ersForType.recognize(new FooException()), is("foo"));
    }

    @Test
    public void whenDoesNotRecognize() {
        ersForType = new ExceptionRecognizerForType(FooException.class);
        assertThat(ersForType.recognize(new BarException()), is(nullValue()));
    }

    @Test
    public void whenRecognizedWithMessageParser() {
        ersForType = new ExceptionRecognizerForType(FooException.class, prepend);
        assertThat(ersForType.recognize(new FooException()), is("pre: foo"));
    }

    
}
