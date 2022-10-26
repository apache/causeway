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
package org.apache.causeway.applib.services.iactnlayer;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.commons.functional.Try;

import lombok.val;

class TryTest {

    // -- TEST DUMMIES

    String hello_happy() {
        return "hello";
    }

    String hello_nullable() {
        return null;
    }

    String hello_throwing_uncatched() {
        throw new RuntimeException("hello failed");
    }

    String hello_throwing_catched() throws Exception {
        throw new Exception("hello failed");
    }

    void void_happy() {

    }

    void void_throwing_uncatched() {
        throw new RuntimeException("void failed");
    }

    void void_throwing_catched() throws Exception {
        throw new Exception("void failed");
    }


    // -- TESTS

    @Test
    void hello_happy_case() {

        val result = Try.<String>call(this::hello_happy);
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals("hello", result.getValue().orElse(""));
        assertEquals("hello", result.getValue().orElse(null));
        assertEquals("hello", result.ifAbsentFail().getValue().get());

        // non-evaluated code-path
        result.getValue().orElseGet(()->fail("unexpected code reach"));

        val mandatory = result.mapEmptyToFailure();
        assertTrue(mandatory.isSuccess());
        assertEquals("hello", mandatory.getValue().orElse(""));

    }

    @Test
    void hello_nullable_case() {

        val result = Try.<String>call(this::hello_nullable);
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals("no value", result.getValue().orElse("no value"));
        assertThrows(NoSuchElementException.class, ()->result.ifAbsentFail());
        assertEquals(Optional.empty(), result.ifFailureFail().getValue());

        val mandatory = result.mapEmptyToFailure();
        assertTrue(mandatory.isFailure());
        assertThrows(NoSuchElementException.class, ()->mandatory.ifFailureFail());

    }

    @Test
    void hello_throwing_uncatched_case() {

        val result = Try.<String>call(this::hello_throwing_uncatched);
        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertEquals("hello failed", result.getFailure().get().getMessage());
        assertEquals("it failed", result.getValue().orElse("it failed"));
        assertEquals("it failed", result.getValue().orElseGet(()->"it failed"));
        assertThrows(RuntimeException.class, ()->result.ifAbsentFail());
        assertEquals(Optional.empty(), result.getValue());

        val mandatory = result.mapEmptyToFailure();
        assertTrue(mandatory.isFailure());

    }

    @Test
    void hello_throwing_catched_case() {

        val result = Try.<String>call(this::hello_throwing_catched);
        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertEquals("hello failed", result.getFailure().get().getMessage());
        assertEquals("it failed", result.getValue().orElse("it failed"));
        assertEquals("it failed", result.getValue().orElseGet(()->"it failed"));
        assertThrows(Exception.class, ()->result.ifAbsentFail());
        assertEquals(Optional.empty(), result.getValue());

        val mandatory = result.mapEmptyToFailure();
        assertTrue(mandatory.isFailure());

    }

    @Test
    void void_happy_case() {

        val result = ThrowingRunnable.resultOf(this::void_happy);
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals(null, result.getValue().orElse(null));

        assertThrows(NoSuchElementException.class, ()->result.ifAbsentFail());
    }

    @Test
    void void_throwing_uncatched_case() {

        val result = ThrowingRunnable.resultOf(this::void_throwing_uncatched);
        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertEquals("void failed", result.getFailure().get().getMessage());

        assertThrows(RuntimeException.class, ()->result.ifFailureFail()); // throw contained exception
        assertThrows(NoSuchElementException.class, ()->result.ifAbsentFail()); // throw NoSuchElementException always
        assertEquals(Optional.empty(), result.getValue());

        val mandatory = result.mapEmptyToFailure();
        assertTrue(mandatory.isFailure());
    }

    @Test
    void void_throwing_catched_case() {

        val result = ThrowingRunnable.resultOf(this::void_throwing_catched);
        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        assertEquals("void failed", result.getFailure().get().getMessage());

        assertThrows(Exception.class, ()->result.ifFailureFail()); // throw contained exception
        assertThrows(NoSuchElementException.class, ()->result.ifAbsentFail()); // throw NoSuchElementException always
        assertEquals(Optional.empty(), result.getValue());

        val mandatory = result.mapEmptyToFailure();
        assertTrue(mandatory.isFailure());
    }


}
