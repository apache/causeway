package org.nakedobjects.xat;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.Action;


public interface TestObject extends TestNaked {

    public void assertActionExists(String name);

    public void assertActionExists(String name, TestNaked parameter);

    public void assertActionExists(String name, TestNaked[] parameters);

    /**
     * Check that the specified object menu item is currently invisible. If it
     * is visible the test fails.
     */
    void assertActionInvisible(String name);

    /**
     * Check that the specified object menu item is currently invisible. If it
     * is visible the test fails.
     */
    void assertActionInvisible(String name, TestNaked parameter);

    void assertActionInvisible(String name, TestNaked[] parameters);

    /**
     * Check that the specified object menu item is currently disabled. If it is
     * enabled the test fails.
     */
    void assertActionUnusable(String name);

    /**
     * Check that dragged object cannot be dropped on this object. If it can be
     * dropped the test fails.
     */
    void assertActionUnusable(String name, TestNaked parameter);

    void assertActionUnusable(String name, TestNaked[] parameters);

    /**
     * Check that the specified object menu item is currently available. If it
     * is disabled the test fails.
     */
    void assertActionUsable(String name);

    /**
     * Check that dragged object can be dropped on this object. If it cannot be
     * dropped the test fails.
     */
    void assertActionUsable(String name, TestNaked parameter);

    void assertActionUsable(String name, TestNaked[] parameter);

    /**
     * Check that the specified object menu item is currently visible. If it is
     * invisible the test fails.
     */
    void assertActionVisible(String name);

    /**
     * Check that the specified object menu item is currently visible. If it is
     * invisible the test fails.
     */
    void assertActionVisible(String name, TestNaked parameter);

    void assertActionVisible(String name, TestNaked[] parameters);

    void assertEmpty(String fieldName);

    void assertEmpty(String message, String fieldName);

    void assertFieldContains(String fieldName, Object expected);

    /**
     * Check that the specified field contains the expected value. If it does
     * not contain the expected value the test fails.
     */
    void assertFieldContains(String fieldName, String expected);
    
    
    
    void assertFieldContains(String message, String fieldName, Object expected);

    /**
     * Check that the specified field contains the expected value. If it does
     * not contain the expected value the test fails.
     * 
     * @param message
     *                       text to add to the failure message, which is displayed after a
     *                       failure.
     */
    void assertFieldContains(String message, String fieldName, String expected);

    /**
     * Check that the specified field contains the expected object (as
     * represented by the specifed view. If it does not contain the expected
     * object the test fails.
     * 
     * @param message
     *                       text to add to the failure message, which is displayed after a
     *                       failure.
     * @group assert
     */
    void assertFieldContains(String message, String fieldName, TestObject expected);

    /**
     * Check that the specified field contains the expected object (as
     * represented by the specifed view. If it does not contain the expected
     * object the test fails.
     */
    void assertFieldContains(String fieldName, TestObject expected);

    void assertFieldContainsType(String fieldName, String expected);

    void assertFieldContainsType(String message, String fieldName, String expected);

    void assertFieldContainsType(String message, String fieldName, String title, String expected);

    void assertFieldDoesNotContain(String fieldName, NakedObject expected);

    /*
     * Start of Field Not Contains /** Check that the specified field contains
     * the expected value. If it does not contain the expected value the test
     * fails.
     */
    void assertFieldDoesNotContain(String fieldName, String expected);

    void assertFieldDoesNotContain(String message, String fieldName, NakedObject expected);

    /**
     * Check that the specified field contains the expected value. If it does
     * not contain the expected value the test fails.
     * 
     * @param message
     *                       text to add to the failure message, which is displayed after a
     *                       failure.
     */
    void assertFieldDoesNotContain(String message, String fieldName, String expected);

    /**
     * Check that the specified field contains the expected object (as
     * represented by the specifed view. If it does not contain the expected
     * object the test fails.
     * 
     * @param message
     *                       text to add to the failure message, which is displayed after a
     *                       failure.
     * @group assert
     */
    void assertFieldDoesNotContain(String message, String fieldName, TestObject expected);

    /**
     * Check that the specified field contains the expected object (as
     * represented by the specifed view. If it does not contain the expected
     * object the test fails.
     */
    void assertFieldDoesNotContain(String fieldName, TestObject expected);
    
    public void assertFieldEntryCantParse(String fieldName, String value);

    public void assertFieldEntryInvalid(String fieldName, String value);

    void assertFieldExists(String fieldName);

    void assertFieldInvisible(String fieldName);

    void assertFieldModifiable(String fieldName);

    void assertFieldUnmodifiable(String fieldName);

    void assertFieldVisible(String fieldName);

    void assertFirstElementInField(String fieldName, String expected);

    
    
    void assertFirstElementInField(String message, String fieldName, String expected);
    
    void assertFirstElementInField(String message, String fieldName, TestObject expected);
    
    void assertFirstElementInField(String fieldName, TestObject expected);
    
    void assertLastElementInField(String fieldName, String expected);
    
    void assertLastElementInField(String message, String fieldName, String expected);

    
    void assertLastElementInField(String message, String fieldName, TestObject expected);
    
    void assertLastElementInField(String fieldName, TestObject expected);

    void assertNoOfElements(String collectionName, int noOfElements);

    void assertNoOfElementsNotEqual(String collectionName, int noOfElements);

    void assertNotEmpty(String fieldName);

    void assertNotEmpty(String message, String fieldName);

    /**
     * Check that the title of this object is the same as the expected title. If
     * it is not the same the test fails.
     */
    void assertTitleEquals(String expectedTitle);

    /**
     * Check that the title of this object is the same as the expected title. If
     * it is not the same the test fails.
     * 
     * @param message
     *                       text to add to the failure message, which is displayed after a
     *                       failure.
     */
    void assertTitleEquals(String message, String expectedTitle);

    void assertType(String expected);

    void assertType(String message, String expected);

    /**
     * Drop the specified view (object) into the specified field.
     * 
     * <p>
     * If the field already contains an object then, as an object cannot be
     * dropped on a non-empty field, the test will fail.
     * </p>
     * 
     * @group action
     */
    void associate(String fieldName, TestObject draggedView);

    /**
     * Removes an existing object reference from the specified field. Mirrors
     * the 'Remove Reference' menu option that each object field offers by
     * default.
     * 
     * @group action
     */
    void clearAssociation(String fieldName);

    /**
     * Removes the existing object reference, which has the specified title,
     * from the specified multi-object field. Mirrors the 'Remove Reference'
     * menu option that each collection field offers by default.
     * 
     * @group action
     */
    void clearAssociation(String fieldName, String title);

    /**
     * Enters text into an editable field. Data entered here is parsed in
     * exactly the same way as in GUI, and should therefore be given in the
     * correct form and formatted correctly. For fields that normally use some
     * other interaction, e.g. a checkbox, then the correct textual form must be
     * used (for the checkbox this is 'TRUE' and 'FALSE').
     * 
     * @group action
     */
    void fieldEntry(String name, String value);

    Action getAction(String name);

    /**
     * Returns the view, from within this collection, that has the specified
     * title.
     */
    TestObject getAssociation(String title);

    TestNaked getField(String fieldName);

    TestObject getFieldAsObject(final String fieldName);

    TestValue getFieldAsValue(final String fieldName);
    
    /**
     * Get the view for the object held within the named collection view, that
     * has the specified title.
     */
    TestObject getField(String fieldName, String title);

    /**
     * returns the title of the object as a String
     */
    String getFieldTitle(String field);

    /**
     * Invokes this object's zero-parameter action method of the the given name.
     * This mimicks the right-clicking on an object and subsequent selection of
     * a menu item.
     * 
     * @group action
     */
    TestObject invokeAction(String name);

    /**
     * Drop the specified view (object) onto this object and invoke the
     * corresponding <code>action</code> method. A new view representing the
     * returned object, if any is returned, from the invoked <code>action</code>
     * method is returned by this method.
     * 
     * @group action
     */
    TestObject invokeAction(String name, TestNaked parameter);

    TestObject invokeAction(String name, TestNaked[] parameter);

    /**
     * Test the named field by calling fieldEntry with the specifed value and
     * then check the value stored is the same.
     */
    void testField(String fieldName, String expected);

    /**
     * Test the named field by calling fieldEntry with the set value and then
     * check the value stored against the expected value.
     */
    void testField(String fieldName, String setValue, String expected);

    /**
     * Test the named field by calling fieldEntry with the set value and then
     * check the value stored against the expected value.
     */
    void testField(String fieldName, TestObject expected);
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
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