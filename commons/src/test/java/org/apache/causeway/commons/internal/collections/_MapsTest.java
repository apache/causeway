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
package org.apache.causeway.commons.internal.collections;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.collections.Can;

import lombok.val;

class _MapsTest {
    
    @Test
    void aliasMap_shouldAllowLookupByAliasKey() {
        
        val aliasMap = _Maps.<String, String>newAliasMap(HashMap::new);
        
        aliasMap.put("key1", "value1");
        aliasMap.put("key2", Can.ofArray(new String[] {"alias2a", "alias2b"}), "value2");
        aliasMap.put("key3", Can.empty(), "value3");
        
        assertTrue(aliasMap.containsKey("key1"));
        assertEquals("value1", aliasMap.get("key1"));

        assertTrue(aliasMap.containsKey("key2"));
        assertEquals("value2", aliasMap.get("key2"));
        assertEquals("value2", aliasMap.get("alias2a"));
        assertEquals("value2", aliasMap.get("alias2b"));
        
        assertTrue(aliasMap.containsKey("key3"));
        assertEquals("value3", aliasMap.get("key3"));
        
        
    }
    
    @Test
    void aliasMap_shouldAlsoClearAliases() {
        
        val aliasMap = _Maps.<String, String>newAliasMap(HashMap::new);
        
        aliasMap.put("key1", "value1");
        aliasMap.put("key2", Can.ofArray(new String[] {"alias2a", "alias2b"}), "value2");
        aliasMap.put("key3", Can.empty(), "value3");
        
        aliasMap.clear();
        
        assertFalse(aliasMap.containsKey("key1"));
        assertNull(aliasMap.get("key1"));
        
        assertFalse(aliasMap.containsKey("key2"));
        assertNull(aliasMap.get("key2"));
        assertNull(aliasMap.get("alias2a"));
        assertNull(aliasMap.get("alias2b"));
        
        assertFalse(aliasMap.containsKey("key3"));
        assertNull(aliasMap.get("key3"));
        
    }
    
    @Test
    void aliasMap_shouldHonorRemoval() {
        
        val aliasMap = _Maps.<String, String>newAliasMap(HashMap::new);
        
        aliasMap.put("key1", "value1");
        aliasMap.put("key2", Can.ofArray(new String[] {"alias2a", "alias2b"}), "value2");
        aliasMap.put("key3", Can.empty(), "value3");
        
        aliasMap.remove("key1");
        aliasMap.remove("key2");
        aliasMap.remove("key3");
        
        assertFalse(aliasMap.containsKey("key1"));
        assertNull(aliasMap.get("key1"));
        
        assertFalse(aliasMap.containsKey("key2"));
        assertNull(aliasMap.get("key2"));
        assertNull(aliasMap.get("alias2a"));
        assertNull(aliasMap.get("alias2b"));
        
        assertFalse(aliasMap.containsKey("key3"));
        assertNull(aliasMap.get("key3"));
        
        // re-adding previously removed should not throw
        aliasMap.put("key2", Can.ofArray(new String[] {"alias2a", "alias2b"}), "value2");
        
    }
    
    @Test
    void aliasMap_shouldThrowOnAliasKeyCollision() {
        
        val aliasMap = _Maps.<String, String>newAliasMap(HashMap::new);
        
        aliasMap.put("key1", Can.ofArray(new String[] {"alias1a", "alias1b"}), "value1");
        
        assertThrows(IllegalArgumentException.class, ()->{
            aliasMap.put("key2", Can.ofArray(new String[] {"alias1a"}), "value2");
        });
        
    }
    

}
