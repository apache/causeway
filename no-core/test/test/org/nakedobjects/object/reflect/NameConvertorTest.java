package test.org.nakedobjects.object.reflect;

import org.nakedobjects.object.reflect.NameConvertor;

import junit.framework.TestCase;


public class NameConvertorTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(NameConvertorTest.class);
    }

    public void testNaturalNameAddsSpacesToCamelCaseWords() {
        assertEquals("Camel Case Word", NameConvertor.naturalName("CamelCaseWord"));
    }

    public void testNaturalNameAddsSpacesBeforeNumbers() {
        assertEquals("One 2 One", NameConvertor.naturalName("One2One"));
        assertEquals("Type 123", NameConvertor.naturalName("Type123"));
        assertEquals("4321 Go", NameConvertor.naturalName("4321Go"));
    }


    public void testNaturalNameRecognisesAcronymns() {
        assertEquals("TNT Power", NameConvertor.naturalName("TNTPower"));
        assertEquals("Spam RAM Can", NameConvertor.naturalName("SpamRAMCan"));
        assertEquals("DOB", NameConvertor.naturalName("DOB"));
    }

    public void testNaturalNameWithShortNames() {
        assertEquals("At", NameConvertor.naturalName("At"));
        assertEquals("I", NameConvertor.naturalName("I"));
    }

    public void testNaturalNameNoChange() {
        assertEquals("Camel Case Word", NameConvertor.naturalName("CamelCaseWord"));
        assertEquals("Almost Normal english sentence", NameConvertor.naturalName("Almost Normal english sentence"));
    }

    public void testPluralNameAdd_S() {
        assertEquals("Cans", NameConvertor.pluralName("Can"));
        assertEquals("Spaces", NameConvertor.pluralName("Space"));
        assertEquals("Noses", NameConvertor.pluralName("Nose"));
    }

    public void testPluralNameReplace_Y_With_IES() {
        assertEquals("Babies", NameConvertor.pluralName("Baby"));
        assertEquals("Cities", NameConvertor.pluralName("City"));
    }

    public void testPluralNameReplaceAdd_ES() {
        assertEquals("Foxes", NameConvertor.pluralName("Fox"));
        assertEquals("Bosses", NameConvertor.pluralName("Boss"));
    }

    public void testSimpleNameAllToLowerCase() {
        assertEquals("abcde", NameConvertor.simpleName("ABCDE"));
        assertEquals("camelcaseword", NameConvertor.simpleName("CamelCaseWord"));
    }

    public void testSimpleNameNoChanges() {
        assertEquals("nochanges", NameConvertor.simpleName("nochanges"));
    }

    public void testSimpleNameRemoveSpaces() {
        assertEquals("abcde", NameConvertor.simpleName("a bc  de "));
        assertEquals("twoparts", NameConvertor.simpleName("two parts"));
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */