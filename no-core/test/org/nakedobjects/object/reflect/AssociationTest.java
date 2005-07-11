package org.nakedobjects.object.reflect;

import org.nakedobjects.TestSystem;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectTestCase;
import org.nakedobjects.object.TextEntryParseException;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;

import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class AssociationTest extends NakedObjectTestCase {
    private static final String FIELD_NAME = "person";
    private static final String FIELD_LABEL = "Person";
    private NakedObject nakedObject;
    private OneToOneAssociation association;
    private NakedObject associate;
    private DummyNakedObjectSpecification type;
    private MockOneToOneAssociation associationDelegate;
    private TestSystem system;

    public AssociationTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(AssociationTest.class));
    }

    protected void setUp() throws Exception {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);

        system = new TestSystem();
        system.init();

        nakedObject = new DummyNakedObject();
        associate = new DummyNakedObject();
        
        associationDelegate = new MockOneToOneAssociation();
        type = new DummyNakedObjectSpecification();
        association = new OneToOneAssociation("", FIELD_NAME, type, associationDelegate);
    }
    
    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testType() {
        assertEquals(type, association.getSpecification());
    }

    public void testSet() {
        association.setAssociation(nakedObject, associate);
        associationDelegate.assertAction(0, "associate " + nakedObject);
        associationDelegate.assertAction(1, "associate " + associate);
    }

    public void testRemove() {
        association.clearAssociation(nakedObject, associate);
        associationDelegate.assertAction(0, "clear " + nakedObject);
        associationDelegate.assertAction(1, "clear " + associate);
    }

    public void testGet() {
        Naked object = association.get(nakedObject);
        assertNull(object);
        
        associationDelegate.getObject = new DummyNakedObject();
        object = association.get(nakedObject);
        assertEquals(associationDelegate.getObject, object);
        associationDelegate.assertAction(0, "get " + nakedObject);
    }

    public void testInitGet() {
   //     association.initData(nakedObject, person);

  //      assertEquals(person, role.getPerson());
    }

    public void testName() {
        assertEquals(FIELD_NAME, association.getName());
    }

    public void testLabel() {
        assertEquals(FIELD_NAME, association.getLabel(nakedObject));

        associationDelegate.label = FIELD_LABEL;
        associationDelegate.hasAbout = true;
        assertEquals(FIELD_LABEL, association.getLabel(nakedObject));
    }

    public void testAbout() {
        assertFalse(association.hasHint());

         Hint about = association.getHint(nakedObject, null);
        assertNull(associationDelegate.about);
        assertTrue(about instanceof DefaultHint);

        associationDelegate.hasAbout = true;
        assertTrue(association.hasHint());

        about = association.getHint(nakedObject, null);
        assertEquals(associationDelegate.about, about);
    }
    
    public void testIsEmpty() {
        assertFalse(association.isEmpty(nakedObject));
        associationDelegate.assertAction(0, "empty " + nakedObject);
        
        associationDelegate.isEmpty = true;
        assertTrue(association.isEmpty(nakedObject));
        associationDelegate.assertAction(1, "empty " + nakedObject);
    }
    
    public void testInitValue() throws TextEntryParseException, InvalidEntryException {
        association.initValue(nakedObject, "text");
        associationDelegate.assertAction(0, "init " + nakedObject);
        associationDelegate.assertAction(1, "init " + "text");
    }
    
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