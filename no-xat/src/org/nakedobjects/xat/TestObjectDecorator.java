package org.nakedobjects.xat;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.reflect.ActionSpecification;


public abstract class TestObjectDecorator implements TestObject {
    private final TestObject wrappedObject;

    public TestObjectDecorator(TestObject wrappedObject) {
        this.wrappedObject = wrappedObject;
    }

    public void assertActionInvisible(String name) {
        wrappedObject.assertActionInvisible(name);
    }

    public void assertActionInvisible(String name, TestObject parameter) {
        wrappedObject.assertActionInvisible(name, parameter);
    }

    public void assertActionUnusable(String name) {
        wrappedObject.assertActionUnusable(name);
    }

    public void assertActionUnusable(String name, TestObject parameter) {
        wrappedObject.assertActionUnusable(name, parameter);
    }

    public void assertActionUsable(String name) {
        wrappedObject.assertActionUsable(name);
    }

    public void assertActionUsable(String name, TestObject parameter) {
        wrappedObject.assertActionUsable(name, parameter);
    }

    public void assertActionVisible(String name) {
        wrappedObject.assertActionVisible(name);
    }

    public void assertActionVisible(String name, TestObject parameter) {
        wrappedObject.assertActionVisible(name, parameter);
    }

    /** @deprecated */
    public void assertCantInvokeAction(String name) {
        wrappedObject.assertCantInvokeAction(name);
    }

    /** @deprecated */
    public void assertCantInvokeAction(String name, TestObject parameter) {
        wrappedObject.assertCantInvokeAction(name, parameter);
    }

    public void assertEmpty(String fieldName) {
        wrappedObject.assertEmpty(fieldName);
    }

    public void assertEmpty(String message, String fieldName) {
        wrappedObject.assertEmpty(message, fieldName);
    }

    public void assertFieldContains(String fieldName, NakedValue expectedValue) {
        wrappedObject.assertFieldContains(fieldName, expectedValue);
    }

    public void assertFieldContains(String fieldName, String expectedValue) {
        wrappedObject.assertFieldContains(fieldName, expectedValue);
    }

    public void assertFieldContains(String message, String fieldName, NakedValue expectedValue) {
        wrappedObject.assertFieldContains(message, fieldName, expectedValue);
    }

    public void assertFieldContains(String message, String fieldName, String expectedValue) {
        wrappedObject.assertFieldContains(message, fieldName, expectedValue);
    }

    public void assertFieldContains(String message, String fieldName, TestObject expectedView) {
        wrappedObject.assertFieldContains(message, fieldName, expectedView);
    }

    public void assertFieldContains(String fieldName, TestObject expectedView) {
        wrappedObject.assertFieldContains(fieldName, expectedView);
    }

    public void assertFieldContainsType(String fieldName, String expectedType) {
        wrappedObject.assertFieldContainsType(fieldName, expectedType);
    }

    public void assertFieldContainsType(String message, String fieldName, String expectedType) {
        wrappedObject.assertFieldContainsType(fieldName, expectedType);
    }

    public void assertFieldContainsType(String message, String fieldName, String title, String expectedType) {
        wrappedObject.assertFieldContainsType(message, fieldName, title, expectedType);
    }

    public void assertFieldInvisible(String fieldName) {
        wrappedObject.assertFieldInvisible(fieldName);
    }

    public void assertFieldModifiable(String fieldName) {
        wrappedObject.assertFieldModifiable(fieldName);
    }

    /** @deprecated */
    public void assertFieldReadOnly(String fieldName) {
        wrappedObject.assertFieldReadOnly(fieldName);
    }

    public void assertFieldUnmodifiable(String fieldName) {
        wrappedObject.assertFieldUnmodifiable(fieldName);
    }

    public void assertFieldVisible(String fieldName) {
        wrappedObject.assertFieldVisible(fieldName);
    }

    public void assertNotEmpty(String fieldName) {
        wrappedObject.assertNotEmpty(fieldName);
    }

    public void assertNotEmpty(String message, String fieldName) {
        wrappedObject.assertNotEmpty(message, fieldName);
    }

    public void assertTitleEquals(String expectedTitle) {
        wrappedObject.assertTitleEquals(expectedTitle);
    }

    public void assertTitleEquals(String message, String expectedTitle) {
        wrappedObject.assertTitleEquals(message, expectedTitle);
    }

    public void assertType(String expectedType) {
        wrappedObject.assertType(expectedType);
    }

    public void assertType(String message, String expectedType) {
        wrappedObject.assertType(message, expectedType);
    }

    public void associate(String fieldName, TestObject draggedView) {
        wrappedObject.associate(fieldName, draggedView);
    }

    public void clearAssociation(String fieldName) {
        wrappedObject.clearAssociation(fieldName);
    }

    public void clearAssociation(String fieldName, String title) {
        wrappedObject.clearAssociation(fieldName, title);
    }

    public void fieldEntry(String name, String value) {
        wrappedObject.fieldEntry(name, value);
    }

    public ActionSpecification getAction(String name) {
        return wrappedObject.getAction(name);
    }

    public TestObject getAssociation(String title) {
        return wrappedObject.getAssociation(title);
    }

    public TestNaked getField(String fieldName) {
        return wrappedObject.getField(fieldName);
    }

    public TestObject getField(String fieldName, String title) {
        return wrappedObject.getField(fieldName, title);
    }

    public String getFieldTitle(String field) {
        return wrappedObject.getFieldTitle(field);
    }

    public Naked getForObject() {
        return wrappedObject.getForObject();
    }

    public String getTitle() {
        return wrappedObject.getTitle();
    }

    public TestObject invokeAction(String name) {
        return wrappedObject.invokeAction(name);
    }

    public TestObject invokeAction(String name, TestObject parameter) {
        return wrappedObject.invokeAction(name, parameter);
    }

    public void setForObject(Naked object) {
        wrappedObject.setForObject(object);
    }

    public void testField(String fieldName, String expectedValue) {
        wrappedObject.testField(fieldName, expectedValue);
    }

    public void testField(String fieldName, String setValue, String expectedValue) {
        wrappedObject.testField(fieldName, setValue, expectedValue);
    }

    public void testField(String fieldName, TestObject expectedObject) {
        wrappedObject.testField(fieldName, expectedObject);
    }
    
    public void assertActionExists(String name) {
        wrappedObject.assertActionExists(name);  
        }

    public void assertActionExists(String name, TestObject parameter) {
        wrappedObject.assertActionExists(name, parameter);
        }


    public void assertActionInvisible(String name, TestNaked[] parameters) {
        wrappedObject.assertActionInvisible(name, parameters);
    }
    public void assertActionUnusable(String name, TestNaked[] parameters) {
        wrappedObject.assertActionUnusable(name, parameters);
    }
    public void assertActionUsable(String name, TestNaked[] parameters) {
        wrappedObject.assertActionUsable(name, parameters);
    }
    public void assertActionVisible(String name, TestNaked[] parameters) {
        wrappedObject.assertActionVisible(name, parameters);
    }
    
    public TestObject invokeAction(String name, TestNaked[] parameters) {
        return wrappedObject.invokeAction(name, parameters);
    }
    
    public void assertActionExists(String name, TestNaked[] parameters) {
        wrappedObject.assertActionExists(name, parameters);
    }
    
    public void assertFieldExists(String fieldName) {
        wrappedObject.assertFieldExists(fieldName);
    }    
}    

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */