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
package org.apache.isis.applib.id;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.isis.applib.SomeDomainClass;
import org.apache.isis.commons.internal.testing._SerializationTester;

import lombok.val;

class LogicalTypeTest {

    @Test
    void eager() {
        
        val original = LogicalType.fqcn(SomeDomainClass.class);
        
        _SerializationTester.assertEqualsOnRoundtrip(original);
        
        assertEquals(
                original.getLogicalTypeName(),
                SomeDomainClass.class.getName());
        
        assertEquals(
                _SerializationTester.roundtrip(original).getLogicalTypeName(), 
                original.getLogicalTypeName());
    }
    
    @Test
    void lazy() {
        
        val original = LogicalType.lazy(SomeDomainClass.class, ()->"hello");
        
        _SerializationTester.assertEqualsOnRoundtrip(original);
        
        assertEquals(
                original.getLogicalTypeName(),
                "hello");
        
        assertEquals(
                _SerializationTester.roundtrip(original).getLogicalTypeName(), 
                original.getLogicalTypeName());
    }
    
    @Test
    void cannotBeEmpty() throws Exception {
        assertThrows(IllegalArgumentException.class, ()->LogicalType.eager(Object.class, ""));
        assertThrows(IllegalArgumentException.class, ()->LogicalType.lazy(Object.class, ()->"").getLogicalTypeName());
    }

    @Test
    void cannotBeNull()  {
        assertThrows(NullPointerException.class, ()->LogicalType.lazy(null, ()->"x"));
        assertThrows(NullPointerException.class, ()->LogicalType.lazy(Object.class, null));
        assertThrows(NullPointerException.class, ()->LogicalType.eager(null, "x"));
        assertThrows(IllegalArgumentException.class, ()->LogicalType.eager(Object.class, null));
    }

}
