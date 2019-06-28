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

package org.apache.isis.core.commons.lang;

import org.apache.isis.metamodel.commons.StringExtensions;

import junit.framework.TestCase;

public class NameUtilTest extends TestCase {

    public void testNaturalNameAddsSpacesToCamelCaseWords() {
        assertEquals("Camel Case Word", StringExtensions.asNaturalName2("CamelCaseWord"));
    }

    public void testNaturalNameAddsSpacesBeforeNumbers() {
        assertEquals("One 2 One", StringExtensions.asNaturalName2("One2One"));
        assertEquals("Type 123", StringExtensions.asNaturalName2("Type123"));
        assertEquals("4321 Go", StringExtensions.asNaturalName2("4321Go"));
    }

    public void testNaturalNameRecognisesAcronymns() {
        assertEquals("TNT Power", StringExtensions.asNaturalName2("TNTPower"));
        assertEquals("Spam RAM Can", StringExtensions.asNaturalName2("SpamRAMCan"));
        assertEquals("DOB", StringExtensions.asNaturalName2("DOB"));
    }

    public void testNaturalNameWithShortNames() {
        assertEquals("At", StringExtensions.asNaturalName2("At"));
        assertEquals("I", StringExtensions.asNaturalName2("I"));
    }

    public void testNaturalNameNoChange() {
        assertEquals("Camel Case Word", StringExtensions.asNaturalName2("CamelCaseWord"));
        assertEquals("Almost Normal english sentence", StringExtensions.asNaturalName2("Almost Normal english sentence"));
    }

    public void testPluralNameAdd_S() {
        assertEquals("Cans", StringExtensions.asPluralName("Can"));
        assertEquals("Spaces", StringExtensions.asPluralName("Space"));
        assertEquals("Noses", StringExtensions.asPluralName("Nose"));
    }

    public void testPluralNameReplace_Y_With_IES() {
        assertEquals("Babies", StringExtensions.asPluralName("Baby"));
        assertEquals("Cities", StringExtensions.asPluralName("City"));
    }

    public void testPluralNameReplaceAdd_ES() {
        assertEquals("Foxes", StringExtensions.asPluralName("Fox"));
        assertEquals("Bosses", StringExtensions.asPluralName("Boss"));
    }

}
