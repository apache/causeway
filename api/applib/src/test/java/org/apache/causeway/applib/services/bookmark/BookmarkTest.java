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
package org.apache.causeway.applib.services.bookmark;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookmarkTest {

    @Test
    void shouldParse_whenExactly2Token() {
        var bookmark = Bookmark.parse("a:b").get();

        assertEquals("a", bookmark.logicalTypeName());
        assertEquals("b", bookmark.identifier());
    }

    @Test
    void shouldNotParse_whenNotAtLeast1TokenOrInvalid() {
        assertEquals(Optional.empty(), Bookmark.parse(null));
        assertEquals(Optional.empty(), Bookmark.parse(""));
        //assertEquals(Optional.empty(), Bookmark.parse("a"));
        assertEquals(Optional.empty(), Bookmark.parse("a:"));
        assertEquals(Optional.empty(), Bookmark.parse(":"));
        assertEquals(Optional.empty(), Bookmark.parse(":b"));
    }

    @Test
    void shouldParse_when1Token() {
        var bookmark = Bookmark.parse("a").get();

        assertEquals("a", bookmark.logicalTypeName());
        assertTrue(bookmark.isEmpty());
        assertEquals(null, bookmark.identifier());
    }

    @Test
    void shouldParse_whenMoreThan2Token() {
        var bookmark = Bookmark.parse("a:b:c").get();

        assertEquals("a", bookmark.logicalTypeName());
        assertEquals("b:c", bookmark.identifier());
    }

}
