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

package org.apache.isis.profilestore.xml.internal;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.runtime.userprofile.Options;
import org.apache.isis.core.runtime.userprofile.PerspectiveEntry;
import org.apache.isis.core.runtime.userprofile.UserProfile;

public class UserProfileContentWriterTest {

    private UserProfileContentWriter writer1;
    private UserProfile profile;
    private StringWriter writer;

    @Before
    public void setup() throws Exception {
        profile = new UserProfile();
        writer1 = new UserProfileContentWriter(profile);
        writer = new StringWriter();

    }

    @Test
    public void emptyStructure() throws Exception {
        writeContent();
        assertEquals("<profile>\n  <options>\n  </options>\n  <perspectives>\n  </perspectives>\n</profile>\n", writer.toString());
    }

    @Test
    public void singleOption() throws Exception {
        profile.addToOptions("option1", "value1");
        profile.addToOptions("option2", "value2");
        assertLine("    <option id=\"option2\">value2</option>", 2);
        assertLine("    <option id=\"option1\">value1</option>", 3);
    }

    @Test
    public void recursiveOptions() throws Exception {
        final Options options = new Options();
        options.addOption("option2", "value2");
        profile.getOptions().addOptions("option1", options);
        assertLine("    <options id=\"option1\">", 2);
        assertLine("      <option id=\"option2\">value2</option>", 3);
        assertLine("    </options>", 4);
    }

    @Test
    public void emptyOptionsAreIgnored() throws Exception {
        final Options options = new Options();
        profile.getOptions().addOptions("option1", options);
        debug();
        assertLine("  </options>", 2);
    }

    @Test
    public void perspective() throws Exception {
        final PerspectiveEntry perspective = new PerspectiveEntry();
        perspective.setName("test");
        profile.addToPerspectives(perspective);
        // debug();
        writeContent();
        assertEquals("<profile>\n  <options>\n  </options>\n  <perspectives>\n" + "    <perspective name=\"test\">\n      <services>\n      </services>\n      <objects>\n      </objects>\n    </perspective>\n" + "  </perspectives>\n</profile>\n", writer.toString());
    }

    // // Helpers

    private void assertLine(final String expected, final int line) throws IOException {
        writeContent();
        assertEquals(expected, writer.toString().split("\n")[line]);
    }

    private void debug() throws IOException {
        writeContent();
        System.out.println(writer.toString());
    }

    private void writeContent() throws IOException {
        writer1.write(writer);
    }
}
