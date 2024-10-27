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
package org.apache.causeway.commons.handlers;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.causeway.commons.handler.ChainOfResponsibility;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

class ChainOfResponsibilityTest {

    @Test
    void threeHandlers_shouldProperlyTakeResponsibilityInOrder() {

        var aToUpperCase = new ChainOfResponsibility.Handler<String, String>() {

            @Override
            public boolean isHandling(final String request) {
                return request.startsWith("a");
            }

            @Override
            public String handle(final String request) {
                return request.toUpperCase();
            }

        };

        var bToUpperCase = new ChainOfResponsibility.Handler<String, String>() {

            @Override
            public boolean isHandling(final String request) {
                return request.startsWith("b");
            }

            @Override
            public String handle(final String request) {
                return request.toUpperCase();
            }

        };

        var finallyNoop = new ChainOfResponsibility.Handler<String, String>() {

            @Override
            public boolean isHandling(final String request) {
                return true;
            }

            @Override
            public String handle(final String request) {
                return request;
            }

        };

        var chainOfResponsibility = ChainOfResponsibility.of(
                Arrays.asList(aToUpperCase, bToUpperCase, finallyNoop));

        assertEquals("ASTRING", chainOfResponsibility.handle("aString")); // handled by first handler
        assertEquals("BSTRING", chainOfResponsibility.handle("bString")); // handled by second handler
        assertEquals("cString", chainOfResponsibility.handle("cString")); // handled by third handler

    }

    // sample extension
    static interface StringHandler extends ChainOfResponsibility.Handler<String, String>{

    }

    @Test
    void whenExtended_shouldWorkAsWell() {

        var aToUpperCase = new StringHandler() {

            @Override
            public boolean isHandling(final String request) {
                return request.startsWith("a");
            }

            @Override
            public String handle(final String request) {
                return request.toUpperCase();
            }

        };

        var handlers = Arrays.asList(aToUpperCase);

        var chainOfResponsibility = ChainOfResponsibility.of(handlers);

        assertEquals("ASTRING", chainOfResponsibility.handle("aString")); // handled by first handler
        assertThrows(NoSuchElementException.class, ()->chainOfResponsibility.handle("xxx")); // not handled
    }

    @Test
    void handlerExceptions_shouldNoBeSwallowed() {

        var throwingHandler = new ChainOfResponsibility.Handler<String, String>() {

            @Override
            public boolean isHandling(final String request) {
                return request.startsWith("throw");
            }

            @Override
            public String handle(final String request) {
                throw _Exceptions.unrecoverable("for testing purposes");
            }

        };

        var chainOfResponsibility = ChainOfResponsibility.of(
                Arrays.asList(throwingHandler));

        assertThrows(RuntimeException.class, ()->chainOfResponsibility.handle("throw"));
    }

}
