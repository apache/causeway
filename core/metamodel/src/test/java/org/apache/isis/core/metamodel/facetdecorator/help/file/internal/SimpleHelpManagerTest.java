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

package org.apache.isis.core.metamodel.facetdecorator.help.file.internal;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.StringReader;
import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;

public class SimpleHelpManagerTest extends TestCase {

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(SimpleHelpManagerTest.class);
    }

    private TestHelpManager manager;

    @Override
    protected void setUp() throws Exception {
        manager = new TestHelpManager(new IsisConfigurationDefault());
    }

    public void testNoLines() {
        final Identifier identifier = Identifier.propertyOrCollectionIdentifier("cls", "mth");
        final String s = manager.getHelpText(identifier);
        assertEquals("", s);
    }

    public void testClass() {
        manager.addLine("C:cls");
        manager.addLine("Help about class");

        final Identifier identifier = Identifier.classIdentifier("cls");
        final String s = manager.getHelpText(identifier);
        assertEquals("Help about class\n", s);
    }

    public void testClassWithNoText() {
        manager.addLine("C:cls");
        manager.addLine("C:cls2");
        manager.addLine("Help about class");

        final Identifier identifier = Identifier.classIdentifier("cls");
        final String s = manager.getHelpText(identifier);
        assertEquals("", s);
    }

    public void testClassTextStopsAtNextClass() {
        manager.addLine("C:cls");
        manager.addLine("Help about class");
        manager.addLine("C:cls2");
        manager.addLine("Different text");

        final Identifier identifier = Identifier.classIdentifier("cls");
        final String s = manager.getHelpText(identifier);
        assertEquals("Help about class\n", s);
    }

    public void testMethodTextStopsAtNextClass() {
        manager.addLine("C:cls");
        manager.addLine("M:fld");
        manager.addLine("Help about method");
        manager.addLine("C:cls2");
        manager.addLine("Different text");

        final Identifier identifier = Identifier.propertyOrCollectionIdentifier("cls", "fld");
        final String s = manager.getHelpText(identifier);
        assertEquals("Help about method\n", s);
    }

    public void testMethodTextStopsAtNextMethod() {
        manager.addLine("C:cls");
        manager.addLine("M:fld");
        manager.addLine("Help about method");
        manager.addLine("M:fld2");
        manager.addLine("Different text");

        final Identifier identifier = Identifier.propertyOrCollectionIdentifier("cls", "fld");
        final String s = manager.getHelpText(identifier);
        assertEquals("Help about method\n", s);
    }

    public void testClassTextStopsAtFirstMethod() {
        manager.addLine("C:cls");
        manager.addLine("Help about class");
        manager.addLine("M:method");
        manager.addLine("Different text");

        final Identifier identifier = Identifier.classIdentifier("cls");
        final String s = manager.getHelpText(identifier);
        assertEquals("Help about class\n", s);
    }

    public void testEntryWithMultipleLines() {
        manager.addLine("C:cls");
        manager.addLine("Help about class");
        manager.addLine("line 2");
        manager.addLine("line 3");

        final Identifier identifier = Identifier.classIdentifier("cls");
        final String s = manager.getHelpText(identifier);
        assertEquals("Help about class\nline 2\nline 3\n", s);
    }

    public void testFieldWithNoEntry() {
        manager.addLine("C:cls");
        manager.addLine("Help about class");

        final Identifier identifier = Identifier.propertyOrCollectionIdentifier("cls", "fld2");
        final String s = manager.getHelpText(identifier);
        assertEquals("", s);
    }

    public void testMessageForFileError() {
        final Identifier identifier = Identifier.propertyOrCollectionIdentifier("cls", "fld2");
        final HelpManagerUsingFiles manager = new HelpManagerUsingFiles(new IsisConfigurationDefault()) {
            @Override
            protected BufferedReader getReader() throws FileNotFoundException {
                throw new FileNotFoundException("not found");
            }
        };
        final String s = manager.getHelpText(identifier);
        assertEquals("Failure opening help file: not found", s);
    }

    public void testField() {
        manager.addLine("C:cls");
        manager.addLine("Help about class");
        manager.addLine("M:fld1");
        manager.addLine("Help about field");
        manager.addLine("M:fld2");
        manager.addLine("Help about second field");

        final Identifier identifier = Identifier.propertyOrCollectionIdentifier("cls", "fld2");
        final String s = manager.getHelpText(identifier);
        assertEquals("Help about second field\n", s);
    }

    public void testReadBlankLines() {
        manager.addLine("C:cls");
        manager.addLine("");
        manager.addLine("");
        manager.addLine("Help about class");

        final Identifier identifier = Identifier.classIdentifier("cls");
        final String s = manager.getHelpText(identifier);
        assertEquals("\n\nHelp about class\n", s);

    }

    public void testSkipComments() {
        manager.addLine("C:cls");
        manager.addLine("# comment");
        manager.addLine("Help about class");

        final Identifier identifier = Identifier.classIdentifier("cls");
        final String s = manager.getHelpText(identifier);
        assertEquals("Help about class\n", s);

    }
}

class TestHelpManager extends HelpManagerUsingFiles {
    public TestHelpManager(final IsisConfiguration configuration) {
        super(configuration);
    }

    private final StringBuffer file = new StringBuffer();

    public void addLine(final String string) {
        file.append(string);
        file.append('\n');
    }

    @Override
    protected BufferedReader getReader() throws FileNotFoundException {
        return new BufferedReader(new StringReader(file.toString()));
    }
}
