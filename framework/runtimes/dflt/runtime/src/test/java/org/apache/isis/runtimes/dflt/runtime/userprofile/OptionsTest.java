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

package org.apache.isis.runtimes.dflt.runtime.userprofile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.runtime.userprofile.Options;

public class OptionsTest {

    private Options options;
    private Options suboptions;

    @Before
    public void setup() throws Exception {
        suboptions = new Options();
        suboptions.addOption("name-3", "value-2");

        options = new Options();
        options.addOption("test", "value");
        options.addOption("anInt", "23");
        options.addOptions("suboptions", suboptions);
    }

    @Test
    public void savedValueIsRetrieved() throws Exception {
        assertEquals("value", options.getString("test"));
    }

    @Test
    public void unknownNameIsNull() throws Exception {
        assertNull(options.getString("unknown"));
    }

    @Test
    public void intValue() throws Exception {
        assertEquals(23, options.getInteger("anInt", 0));
    }

    @Test
    public void intDefault() throws Exception {
        assertEquals(10, options.getInteger("unknown", 10));
    }

    @Test
    public void stringDefault() throws Exception {
        assertEquals("def", options.getString("unknown", "def"));
    }

    @Test
    public void debug() throws Exception {
        final DebugString debug = new DebugString();
        options.debugData(debug);
        assertNotNull(debug.toString());
    }

    @Test
    public void names() throws Exception {
        final Iterator<String> names = options.names();
        assertTrue(names.hasNext());
    }

    @Test
    public void copy() throws Exception {
        final Options copy = new Options();
        copy.copy(options);
        assertEquals("value", copy.getString("test"));
    }

    @Test
    public void addOptions() throws Exception {
        final Options suboptions = options.getOptions("suboptions");
        assertEquals("value-2", suboptions.getString("name-3"));
    }

    @Test
    public void emptyWhenOptionsWhenNotFound() throws Exception {
        final Options suboptions = options.getOptions("unkown");
        assertFalse(suboptions.names().hasNext());
    }

    @Test
    public void newEmptyOptionsAdded() throws Exception {
        final Options suboptions = options.getOptions("unknown");
        suboptions.addOption("test", "value");
        assertSame(suboptions, options.getOptions("unknown"));
    }
}
