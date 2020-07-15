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
package org.apache.isis.core.commons.internal.binding;

import java.util.concurrent.atomic.LongAdder;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.core.commons.collections.Can;

import lombok.val;

class BindableTest {

    @Test
    void unidirectionalBinding() {

        val primaryChangeCounter = new LongAdder();
        val secondaryChangeCounter = new LongAdder();
        
        val primary = _Bindables.forValue("hello"); 
        val secondary = _Bindables.forValue("");
        
        primary.addListener((e,o,n)->primaryChangeCounter.increment());
        secondary.addListener((e,o,n)->secondaryChangeCounter.increment());

        assertFalse(primary.isBound());
        assertFalse(secondary.isBound());
        
        // setup unidirectional binding, implicitly propagates value PRIMARY -> SECONDARY
        
        secondary.bind(primary);
        
        assertFalse(primary.isBound()); // not bound
        assertTrue(secondary.isBound()); // bound
    
        assertEquals("hello", primary.getValue());
        assertEquals("hello", secondary.getValue());
        
        assertEquals(0, primaryChangeCounter.intValue());
        assertEquals(1, secondaryChangeCounter.intValue()); // expected 1 after bidirectional binding established
        
        // propagate value PRIMARY -> SECONDARY
        
        primary.setValue("there");
        
        assertEquals("there", primary.getValue());
        assertEquals("there", secondary.getValue());
        
        assertEquals(1, primaryChangeCounter.intValue());
        assertEquals(2, secondaryChangeCounter.intValue());
        
        // propagate value SECONDARY -> PRIMARY  
        
        assertThrows(Exception.class, ()->secondary.setValue("world")); // a bound value cannot be set
        
        assertEquals("there", primary.getValue());
        assertEquals("there", secondary.getValue());
        
        assertEquals(1, primaryChangeCounter.intValue());
        assertEquals(2, secondaryChangeCounter.intValue());
        
    }
    
    @Test
    void bidirectionalBinding() {
        
        val primaryChangeCounter = new LongAdder();
        val secondaryChangeCounter = new LongAdder();
        
        val primary = _Bindables.forValue("hello"); 
        val secondary = _Bindables.forValue("");
        
        primary.addListener((e,o,n)->primaryChangeCounter.increment());
        secondary.addListener((e,o,n)->secondaryChangeCounter.increment());
        
        assertFalse(primary.isBound());
        assertFalse(secondary.isBound());
        
        // setup bidirectional binding, implicitly propagates value PRIMARY -> SECONDARY
        
        secondary.bindBidirectional(primary);
        
        assertFalse(primary.isBound()); // bidirectional binding is separated from unidirectional binding
        assertFalse(secondary.isBound()); // bidirectional binding is separated from unidirectional binding
        
        assertEquals("hello", primary.getValue());
        assertEquals("hello", secondary.getValue());
        
        assertEquals(0, primaryChangeCounter.intValue());
        assertEquals(1, secondaryChangeCounter.intValue()); // expected 1 after bidirectional binding established
        
        // propagate value PRIMARY -> SECONDARY
        
        primary.setValue("there");
        
        assertEquals("there", primary.getValue());
        assertEquals("there", secondary.getValue());
        
        assertEquals(1, primaryChangeCounter.intValue());
        assertEquals(2, secondaryChangeCounter.intValue());
        
        // propagate value SECONDARY -> PRIMARY  
        
        secondary.setValue("world");
        
        assertEquals("world", primary.getValue());
        assertEquals("world", secondary.getValue());
        
        assertEquals(2, primaryChangeCounter.intValue());
        assertEquals(3, secondaryChangeCounter.intValue());
        
    }

    @Test
    void unidirectionalBinding_onCan() {

        val primaryChangeCounter = new LongAdder();
        val secondaryChangeCounter = new LongAdder();
        
        val primary = _Bindables.forValue(Can.<String>of("hello")); 
        val secondary = _Bindables.forValue(Can.<String>empty());
        
        primary.addListener((e,o,n)->primaryChangeCounter.increment());
        secondary.addListener((e,o,n)->secondaryChangeCounter.increment());

        assertFalse(primary.isBound());
        assertFalse(secondary.isBound());
        
        // setup unidirectional binding, implicitly propagates value PRIMARY -> SECONDARY
        
        secondary.bind(primary);
        
        assertFalse(primary.isBound()); // not bound
        assertTrue(secondary.isBound()); // bound
    
        assertEquals(Can.<String>of("hello"), primary.getValue());
        assertEquals(Can.<String>of("hello"), secondary.getValue());
        
        assertEquals(0, primaryChangeCounter.intValue());
        assertEquals(1, secondaryChangeCounter.intValue()); // expected 1 after bidirectional binding established
        
        // propagate value PRIMARY -> SECONDARY
        
        primary.setValue(primary.getValue().add("there"));
        
        assertEquals(Can.<String>of("hello", "there"), primary.getValue());
        assertEquals(Can.<String>of("hello", "there"), secondary.getValue());
        
        assertEquals(1, primaryChangeCounter.intValue());
        assertEquals(2, secondaryChangeCounter.intValue());
        
        // propagate value SECONDARY -> PRIMARY  
        
        assertThrows(Exception.class, ()->secondary.setValue(Can.empty())); // a bound value cannot be set
        
        assertEquals(Can.<String>of("hello", "there"), primary.getValue());
        assertEquals(Can.<String>of("hello", "there"), secondary.getValue());
        
        assertEquals(1, primaryChangeCounter.intValue());
        assertEquals(2, secondaryChangeCounter.intValue());
        
    }
    
    @Test
    void bidirectionalBinding_onCan() {
        
        val primaryChangeCounter = new LongAdder();
        val secondaryChangeCounter = new LongAdder();
        
        val primary = _Bindables.forValue(Can.<String>of("hello")); 
        val secondary = _Bindables.forValue(Can.<String>empty());
        
        primary.addListener((e,o,n)->primaryChangeCounter.increment());
        secondary.addListener((e,o,n)->secondaryChangeCounter.increment());
        
        assertFalse(primary.isBound());
        assertFalse(secondary.isBound());
        
        // setup bidirectional binding, implicitly propagates value PRIMARY -> SECONDARY
        
        secondary.bindBidirectional(primary);
        
        assertFalse(primary.isBound()); // bidirectional binding is separated from unidirectional binding
        assertFalse(secondary.isBound()); // bidirectional binding is separated from unidirectional binding
        
        assertEquals(Can.<String>of("hello"), primary.getValue());
        assertEquals(Can.<String>of("hello"), secondary.getValue());
        
        assertEquals(0, primaryChangeCounter.intValue());
        assertEquals(1, secondaryChangeCounter.intValue()); // expected 1 after bidirectional binding established
        
        // propagate value PRIMARY -> SECONDARY
        
        primary.setValue(primary.getValue().add("there"));
        
        assertEquals(Can.<String>of("hello", "there"), primary.getValue());
        assertEquals(Can.<String>of("hello", "there"), secondary.getValue());
        
        assertEquals(1, primaryChangeCounter.intValue());
        assertEquals(2, secondaryChangeCounter.intValue());
        
        // propagate value SECONDARY -> PRIMARY  
        
        primary.setValue(primary.getValue().add("world"));
        
        assertEquals(Can.<String>of("hello", "there", "world"), primary.getValue());
        assertEquals(Can.<String>of("hello", "there", "world"), secondary.getValue());
        
        assertEquals(2, primaryChangeCounter.intValue());
        assertEquals(3, secondaryChangeCounter.intValue());
        
    }
    
    
}
