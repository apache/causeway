package org.nakedobjects.xat;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.Action;

public class MockTestClass implements TestObject {

    public void assertActionExists(String name) {}

    public void assertActionExists(String name, TestNaked parameter) {}

    public void assertActionExists(String name, TestNaked[] parameters) {}

    public void assertActionInvisible(String name) {}

    public void assertActionInvisible(String name, TestNaked parameter) {}

    public void assertActionInvisible(String name, TestNaked[] parameters) {}

    public void assertActionUnusable(String name) {}

    public void assertActionUnusable(String name, TestNaked parameter) {}

    public void assertActionUnusable(String name, TestNaked[] parameters) {}

    public void assertActionUsable(String name) {}

    public void assertActionUsable(String name, TestNaked parameter) {}

    public void assertActionUsable(String name, TestNaked[] parameter) {}

    public void assertActionVisible(String name) {}

    public void assertActionVisible(String name, TestNaked parameter) {}

    public void assertActionVisible(String name, TestNaked[] parameters) {}

    public void assertEmpty(String fieldName) {}

    public void assertEmpty(String message, String fieldName) {}

    public void assertFieldContains(String fieldName, Object expected) {}

    public void assertFieldContains(String fieldName, String expected) {}

    public void assertFieldContains(String message, String fieldName, Object expected) {}

    public void assertFieldContains(String message, String fieldName, String expected) {}

    public void assertFieldContains(String message, String fieldName, TestObject expected) {}

    public void assertFieldContains(String fieldName, TestObject expected) {}

    public void assertFieldContainsType(String fieldName, String expected) {}

    public void assertFieldContainsType(String message, String fieldName, String expected) {}

    public void assertFieldContainsType(String message, String fieldName, String title, String expected) {}

    public void assertFieldDoesNotContain(String fieldName, NakedObject expected) {}

    public void assertFieldDoesNotContain(String fieldName, String expected) {}

    public void assertFieldDoesNotContain(String message, String fieldName, NakedObject expected) {}

    public void assertFieldDoesNotContain(String message, String fieldName, String expected) {}

    public void assertFieldDoesNotContain(String message, String fieldName, TestObject expected) {}

    public void assertFieldDoesNotContain(String fieldName, TestObject expected) {}

    public void assertFieldEntryCantParse(String fieldName, String value) {}

    public void assertFieldEntryInvalid(String fieldName, String value) {}

    public void assertFieldExists(String fieldName) {}

    public void assertFieldInvisible(String fieldName) {}

    public void assertFieldModifiable(String fieldName) {}

    public void assertFieldUnmodifiable(String fieldName) {}

    public void assertFieldVisible(String fieldName) {}

    public void assertFirstElementInField(String fieldName, String expected) {}

    public void assertFirstElementInField(String message, String fieldName, String expected) {}

    public void assertFirstElementInField(String message, String fieldName, TestObject expected) {}

    public void assertFirstElementInField(String fieldName, TestObject expected) {}

    public void assertLastElementInField(String fieldName, String expected) {}

    public void assertLastElementInField(String message, String fieldName, String expected) {}

    public void assertLastElementInField(String message, String fieldName, TestObject expected) {}

    public void assertLastElementInField(String fieldName, TestObject expected) {}

    public void assertNoOfElements(String collectionName, int noOfElements) {}

    public void assertNoOfElementsNotEqual(String collectionName, int noOfElements) {}

    public void assertNotEmpty(String fieldName) {}

    public void assertNotEmpty(String message, String fieldName) {}

    public void assertTitleEquals(String expectedTitle) {}

    public void assertTitleEquals(String message, String expectedTitle) {}

    public void assertType(String expected) {}

    public void assertType(String message, String expected) {}

    public void associate(String fieldName, TestObject draggedView) {}

    public void clearAssociation(String fieldName) {}

    public void clearAssociation(String fieldName, String title) {}

    public void fieldEntry(String name, String value) {}

    public Action getAction(String name) {
        return null;
    }

    public TestObject getAssociation(String title) {
        return null;
    }

    public TestNaked getField(String fieldName) {
        return null;
    }

    public TestObject getField(String fieldName, String title) {
        return null;
    }

    public TestObject getFieldAsObject(String fieldName) {
        return null;
    }

    public TestValue getFieldAsValue(String fieldName) {
        return null;
    }

    public String getFieldTitle(String field) {
        return null;
    }

    public Object getForObject() {
        return null;
    }

    public TestObject invokeAction(String name) {
        return null;
    }

    public TestObject invokeAction(String name, TestNaked parameter) {
        return null;
    }

    public TestObject invokeAction(String name, TestNaked[] parameter) {
        return null;
    }

    public TestObject invokeActionReturnObject(String string, TestNaked[] parameter) {
        return null;
    }

    public TestCollection invokeActionReturnCollection(String string, TestNaked[] parameter) {
        return null;
    }

    public void testField(String fieldName, String expected) {}

    public void testField(String fieldName, String setValue, String expected) {}

    public void testField(String fieldName, TestObject expected) {}

    public Naked getForNaked() {
        return null;
    }

    public void setForNaked(Naked object) {}

    public String getTitle() {
        return null;
    }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/