/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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

package org.nakedobjects.xat;


import java.util.Enumeration;
import java.util.Hashtable;

import junit.framework.Assert;

import org.nakedobjects.object.NakedObject;


public class ExpectationObject {
    private Class type;
    private Hashtable expectedReferences = new Hashtable();
    private Hashtable expectedValues = new Hashtable();
    private TestObject actual;
    private boolean hasExpectations;
    public ExpectationObject(Class type) {
         this.type = type;
    }

    /**
     * 
     * @param viewer org.nakedobjects.mockobject.View
     */
    public void addActual(TestObject viewer) {
        NakedObject obj = (NakedObject) viewer.getForObject();

        if (!obj.getClass().isAssignableFrom(type)) {
            Assert.fail("Expected an object of type " + type.getName() + " but got a " + obj.getClass().getName());
        }
        actual = viewer;
    }

    public void addExpectedReference(String name, NakedObject reference) {
        checkFieldUse(name);
        expectedReferences.put(name, new ExpectationValue(reference));
        setHasExpectations();
    }

    private void setHasExpectations() {
        hasExpectations = true;
    }

    public void addExpectedReference(String name, TestObject view) {
        checkFieldUse(name);
        expectedReferences.put(name, new ExpectationValue((NakedObject) view.getForObject()));
        setHasExpectations();
    }

    public void addExpectedText(String name, String value) {
        checkFieldUse(name);
        expectedValues.put(name, value);
        setHasExpectations();
    }

    private void checkFieldUse(String name) {
        if (expectedValues.containsKey(name) || expectedReferences.containsKey(name)) {
            throw new RuntimeException("Duplicate field: " + name);
        }
    }

    public void clearActual() {
        actual = null;
    }

    protected void clearExpectation() {
        expectedValues.clear();
        expectedReferences.clear();
    }

    public void setExpectNothing() {
        clearExpectation();
        setHasExpectations();
    }

    public void verify() {
        Enumeration e = expectedValues.keys();

        while (e.hasMoreElements()) {
            String fieldName = (String) e.nextElement();
            String fieldValue = actual.getField(fieldName).getTitle();

            Assert.assertEquals("Wrong value for " + fieldName, expectedValues.get(fieldName).toString(), fieldValue);
        }

        //
        e = expectedReferences.keys();
        while (e.hasMoreElements()) {
            String fieldName = (String) e.nextElement();
            NakedObject fieldValue = (NakedObject) actual.getField(fieldName).getForObject();
            NakedObject ref = ((ExpectationValue) expectedReferences.get(fieldName)).getReference();

            Assert.assertEquals("Wrong reference for " + fieldName, ref, fieldValue);
        }
    }
}
