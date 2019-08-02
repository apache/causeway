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
package org.apache.isis.viewer.restfulobjects.applib.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PathNodeTest_equalsHashcode {

    @Test
    public void simple() throws Exception {
        final PathNode node = PathNode.parse("foo");
        final PathNode node2 = PathNode.parse("foo");
        assertEquals(node, node2);
    }

    @Test
    public void sameCriterium() throws Exception {
        final PathNode node = PathNode.parse("a[b=c]");
        final PathNode node2 = PathNode.parse("a[b=c]");
        assertEquals(node, node2);
    }

    @Test
    public void moreThenOneSameCriterium() throws Exception {
        final PathNode node = PathNode.parse("a[b=c d=e]");
        final PathNode node2 = PathNode.parse("a[d=e b=c]");
        assertEquals(node, node2);
    }
    
    @Test
    public void notEqualKey() throws Exception {
        final PathNode node = PathNode.parse("a[b=c d=e]");
        final PathNode node2 = PathNode.parse("b");
        assertFalse(node.equals(node2));
    }

    @Test
    public void criteriumIgnored() throws Exception {
        final PathNode node = PathNode.parse("a[b=c]");
        final PathNode node2 = PathNode.parse("a[b=d]");
        assertEquals(node, node2);
    }

    @Test
    public void criteriumIgnoredHereAlso() throws Exception {
        final PathNode node = PathNode.parse("a[b=c d=e]");
        final PathNode node2 = PathNode.parse("a");
        assertEquals(node, node2);
    }

}
