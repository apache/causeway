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
package org.apache.isis.commons.internal.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.commons.internal.context._Context;

@Disabled("some tests fail to run on eclipse -> "
        + "java.lang.ClassNotFoundException: org.junit.platform.commons.logging.LogRecordListener")
class ContextTest {

    public static class AClass {
        String name;
    }

    @BeforeEach
    void beforeEach() {
        _Context.clear();
    }

    @AfterEach
    void afterEach() {
        _Context.clear();
    }

    @Test
    void precondition() {
        final Class<AClass> type = AClass.class;

        final Object actual = _Context.getIfAny(type); 
        assertNull(actual, "pre-condition: context is expected to be empty");
    }


    @Test
    void testPutSingleton() {

        final Class<AClass> type = AClass.class;
        final AClass singleton = new AClass();

        _Context.putSingleton(type, singleton);

        final Object actual = _Context.getIfAny(type);
        assertTrue(singleton==actual, "singleton on context is expected to be the same as the 'local' instance");

        assertThrows(IllegalStateException.class, 
                ()->{_Context.putSingleton(type, singleton);},
                "expected to throw: you cannot override a singleton that is already on the context");


        assertThrows(NullPointerException.class, 
                ()->{_Context.putSingleton(type, null);},
                "expected to throw: you cannot put null references onto the context");
    }

    @Test
    void testPutWithOverride() {

        final Class<AClass> type = AClass.class;
        final AClass instance1 = new AClass();
        final AClass instance2 = new AClass();

        {
            final boolean success = _Context.put(type, instance1, true);
            assertTrue(success, "expected: successful put");

            final Object actual = _Context.getIfAny(type);
            assertTrue(instance1==actual, "singleton on context is expected to be the same as the 'local' instance1");
        }

        {
            final boolean success = _Context.put(type, instance2, true);
            assertTrue(success, "expected: successful put");

            final Object actual = _Context.getIfAny(type);
            assertTrue(instance2==actual, "singleton on context is expected to be the same as the 'local' instance2");
        }

        assertThrows(NullPointerException.class, 
                ()->{_Context.put(type, null, true);},
                "expected to throw: you cannot put null references onto the context");

    }

    @Test
    void testPutWithoutOverride() {

        final Class<AClass> type = AClass.class;
        final AClass instance1 = new AClass();
        final AClass instance2 = new AClass();

        {
            final boolean success = _Context.put(type, instance1, false);
            assertTrue(success, "expected: successful put");

            final Object actual = _Context.getIfAny(type);
            assertTrue(instance1==actual, "singleton on context is expected to be the same as the 'local' instance1");
        }

        {
            final boolean success = _Context.put(type, instance2, false);
            assertFalse(success, "expected: failed put");

            final Object actual = _Context.getIfAny(type);
            assertTrue(instance1==actual, "singleton on context is expected to be the same as the 'local' instance1");
        }

        assertThrows(NullPointerException.class, 
                ()->{_Context.put(type, null, false);},
                "expected to throw: you cannot put null references onto the context");

    }

    @Test
    void testComputeIfAbsent() {

        final Class<AClass> type = AClass.class;
        final AClass instance1 = new AClass();
        final AClass instance2 = new AClass();

        _Context.computeIfAbsent(type, ()->instance1);

        {
            final Object actual = _Context.getIfAny(type);
            assertTrue(instance1==actual, "singleton on context is expected to be the same as the 'local' instance1");
        }

        _Context.computeIfAbsent(type, ()->instance2); // expected: this call does nothing

        { 
            final Object actual = _Context.getIfAny(type);
            assertTrue(instance1==actual, "singleton on context is expected to be the same as the 'local' instance1");
        }

    }

    @Test
    void testGetOrElse() {

        final Class<AClass> type = AClass.class;
        final AClass singleton = new AClass();
        final AClass fallback = new AClass();

        { 
            final Object actual = _Context.getOrElse(type, ()->fallback);
            assertTrue(fallback==actual, "singleton on context is expected to be the same as the 'local' fallback");
        }

        _Context.putSingleton(type, singleton);

        { 
            final Object actual = _Context.getOrElse(type, ()->fallback);
            assertTrue(singleton==actual, "singleton on context is expected to be the same as the 'local' singleton");
        }
    }

    @Test
    void testGetOrThrow() {

        final Class<AClass> type = AClass.class;
        final AClass singleton = new AClass();

        assertThrows(IllegalStateException.class, 
                ()->{_Context.getElseThrow(type, IllegalStateException::new);},
                "expected to throw: no singleton on context that matches the given type");

        _Context.putSingleton(type, singleton);

        final Object actual = _Context.getElseThrow(type, IllegalStateException::new);
        assertTrue(singleton==actual, "singleton on context is expected to be the same as the 'local' singleton");
    }

    @Test
    void testGetDefaultClassLoader() {
        assertNotNull(_Context.getDefaultClassLoader(), "expected: even an empty context should provide a non-empty default");
    }

    @Test
    void testSetDefaultClassLoaderWithOverride() {
        assertThrows(NullPointerException.class, 
                ()->{_Context.setDefaultClassLoader(null, true);},
                "expected to throw: you cannot put null references onto the context");

        _Context.setDefaultClassLoader(AClass.class.getClassLoader(), true);

        final Object actual = _Context.getDefaultClassLoader();
        assertTrue(AClass.class.getClassLoader()==actual, "singleton on context is expected to be the same as the 'local' singleton");
    }

    @Test
    void testSetDefaultClassLoaderWithoutOverride() {

        assertThrows(NullPointerException.class, 
                ()->{_Context.setDefaultClassLoader(null, false);},
                "expected to throw: you cannot put null references onto the context");

        _Context.setDefaultClassLoader(AClass.class.getClassLoader(), false);

        final Object actual = _Context.getDefaultClassLoader();
        assertTrue(AClass.class.getClassLoader()==actual, "singleton on context is expected to be the same as the 'local' singleton");
    }

    @Test
    void testLoadClass() {
        assertThrows(ClassNotFoundException.class, 
                ()->_Context.loadClass(AClass.class.getName()));
    }

    @Test
    void testLoadClassAndInitialize() {
        assertThrows(ClassNotFoundException.class, 
                ()->_Context.loadClassAndInitialize(AClass.class.getName()));
    }

}
