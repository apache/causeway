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

import junit.framework.TestCase;

public class NameUtilTest extends TestCase {

    public void testNaturalNameAddsSpacesToCamelCaseWords() {
        assertEquals("Camel Case Word", NameUtils.naturalName("CamelCaseWord"));
    }

    public void testNaturalNameAddsSpacesBeforeNumbers() {
        assertEquals("One 2 One", NameUtils.naturalName("One2One"));
        assertEquals("Type 123", NameUtils.naturalName("Type123"));
        assertEquals("4321 Go", NameUtils.naturalName("4321Go"));
    }

    public void testNaturalNameRecognisesAcronymns() {
        assertEquals("TNT Power", NameUtils.naturalName("TNTPower"));
        assertEquals("Spam RAM Can", NameUtils.naturalName("SpamRAMCan"));
        assertEquals("DOB", NameUtils.naturalName("DOB"));
    }

    public void testNaturalNameWithShortNames() {
        assertEquals("At", NameUtils.naturalName("At"));
        assertEquals("I", NameUtils.naturalName("I"));
    }

    public void testNaturalNameNoChange() {
        assertEquals("Camel Case Word", NameUtils.naturalName("CamelCaseWord"));
        assertEquals("Almost Normal english sentence", NameUtils.naturalName("Almost Normal english sentence"));
    }

    public void testPluralNameAdd_S() {
        assertEquals("Cans", NameUtils.pluralName("Can"));
        assertEquals("Spaces", NameUtils.pluralName("Space"));
        assertEquals("Noses", NameUtils.pluralName("Nose"));
    }

    public void testPluralNameReplace_Y_With_IES() {
        assertEquals("Babies", NameUtils.pluralName("Baby"));
        assertEquals("Cities", NameUtils.pluralName("City"));
    }

    public void testPluralNameReplaceAdd_ES() {
        assertEquals("Foxes", NameUtils.pluralName("Fox"));
        assertEquals("Bosses", NameUtils.pluralName("Boss"));
    }

    public void testSimpleNameAllToLowerCase() {
        assertEquals("abcde", NameUtils.simpleName("ABCDE"));
        assertEquals("camelcaseword", NameUtils.simpleName("CamelCaseWord"));
    }

    public void testSimpleNameNoChanges() {
        assertEquals("nochanges", NameUtils.simpleName("nochanges"));
    }

    public void testSimpleNameRemoveSpaces() {
        assertEquals("abcde", NameUtils.simpleName("a bc  de "));
        assertEquals("twoparts", NameUtils.simpleName("two parts"));
    }

}
