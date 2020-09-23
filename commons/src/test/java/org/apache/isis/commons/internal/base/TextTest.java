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
package org.apache.isis.commons.internal.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.commons.collections.Can;

class TextTest {

    @Test
    void lineParsingWin() {
        assertEquals(Can.of("Hallo", "", "World"), _Text.getLines("Hallo\r\n\r\nWorld"));
    }

    @Test
    void lineParsingUx() {
        assertEquals(Can.of("Hallo", "", "World"), _Text.getLines("Hallo\n\nWorld"));
    }

    @Test
    void repeatedEmptyLineRemoval() {
        
        assertEquals(Can.of("Hallo", "", "World"), _Text.removeRepeatedEmptyLines(Can.of("Hallo", "", "World")));
        assertEquals(Can.of("Hallo", "", "World"), _Text.removeRepeatedEmptyLines(Can.of("Hallo", "", "", "World")));
        assertEquals(Can.of("Hallo", "", "World"), _Text.removeRepeatedEmptyLines(Can.of("Hallo", "", " \t ", "World")));
        
        assertEquals(Can.of("", "Hallo", "", "World"), _Text.removeRepeatedEmptyLines(Can.of("", "Hallo", "", "World")));
        assertEquals(Can.of("", "Hallo", "", "World"), _Text.removeRepeatedEmptyLines(Can.of("", "", "Hallo", "", "World")));
        
        assertEquals(Can.of("Hallo", "", "World", ""), _Text.removeRepeatedEmptyLines(Can.of("Hallo", "", "World", "")));
        assertEquals(Can.of("Hallo", "", "World", ""), _Text.removeRepeatedEmptyLines(Can.of("Hallo", "", "World", "", "")));
        
    }
    
    @Test
    void leadingEmptyLineRemoval() {
        
        assertEquals(Can.<String>empty(), _Text.removeLeadingEmptyLines(Can.of("", "", "")));
        
        assertEquals(Can.of("Hallo", "", "World"), _Text.removeLeadingEmptyLines(Can.of("Hallo", "", "World")));
        assertEquals(Can.of("Hallo", "", "World"), _Text.removeLeadingEmptyLines(Can.of("", "Hallo", "", "World")));
        assertEquals(Can.of("Hallo", "", "World"), _Text.removeLeadingEmptyLines(Can.of("", "", "Hallo", "", "World")));
        
    }
    
    @Test
    void trailingEmptyLineRemoval() {
        
        assertEquals(Can.<String>empty(), _Text.removeTrailingEmptyLines(Can.of("", "", "")));
        
        assertEquals(Can.of("Hallo", "", "World"), _Text.removeTrailingEmptyLines(Can.of("Hallo", "", "World")));
        assertEquals(Can.of("Hallo", "", "World"), _Text.removeTrailingEmptyLines(Can.of("Hallo", "", "World", "")));
        assertEquals(Can.of("Hallo", "", "World"), _Text.removeTrailingEmptyLines(Can.of("Hallo", "", "World", "", "")));
    }
    
}
