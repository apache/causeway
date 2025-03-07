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
package org.apache.causeway.applib.id;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.causeway.applib.SomeDomainClass;
import org.apache.causeway.commons.internal.testing._SerializationTester;

class LogicalTypeTest {

    @Test
    void eager() {
        var original = LogicalType.fqcn(SomeDomainClass.class);
        
        _SerializationTester.assertEqualsOnRoundtrip(original);
        
        assertEquals(
                original.logicalName(),
                SomeDomainClass.class.getName());
        
        assertEquals(
                _SerializationTester.roundtrip(original).logicalName(), 
                original.logicalName());
    }
    
    @Test
    void cannotBeEmpty() throws Exception {
        assertThrows(IllegalArgumentException.class, ()->LogicalType.eager(Object.class, ""));
    }

    @Test
    void cannotBeNull()  {
        assertThrows(NullPointerException.class, ()->LogicalType.eager(null, "x"));
        assertThrows(IllegalArgumentException.class, ()->LogicalType.eager(Object.class, null));
    }

}
