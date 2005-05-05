package org.nakedobjects.object.reflect.internal;

import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.MockNakedObject;
import org.nakedobjects.object.NakedObjectTestCase;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoader;

import java.lang.reflect.Method;
import java.util.Vector;

import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class InternalOneToManyAssociationTest extends NakedObjectTestCase {
    private static final String MEMBERS_FIELD_NAME = "members";
    private InternalObjectWithVector objectWithVector;
    private MockNakedObject nakedObject;
    private InternalOneToManyAssociation oneToOneAssociation;
    private InternalObjectForReferencing elements[];
    private MockNakedObjectSpecificationLoader loader;
    private DummyNakedObjectSpecification spec;

    public InternalOneToManyAssociationTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(InternalOneToManyAssociationTest.class));
    }

    protected void setUp() throws Exception {

        Logger.getRootLogger().setLevel(Level.OFF);
        loader = new MockNakedObjectSpecificationLoader();
        new NakedObjectsClient().setSpecificationLoader(loader);

        spec = new DummyNakedObjectSpecification();
        loader.addSpec(spec);

        objectWithVector = new InternalObjectWithVector();

        nakedObject = new MockNakedObject();
        nakedObject.setupObject(objectWithVector);
        elements = new InternalObjectForReferencing[3];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = new InternalObjectForReferencing();
        }

        Class cls = InternalObjectWithVector.class;
        Method get = cls.getDeclaredMethod("getMethod", new Class[0]);
        Method add = cls.getDeclaredMethod("addToMethod", new Class[] { InternalObjectForReferencing.class });
        Method remove = cls.getDeclaredMethod("removeFromMethod", new Class[] { InternalObjectForReferencing.class });

        oneToOneAssociation = new InternalOneToManyAssociation(MEMBERS_FIELD_NAME, InternalCollection.class, get, add, remove,
                null);
    }

    public void testType() {
        loader.addSpec(spec);
        assertEquals(spec, oneToOneAssociation.getType());
    }

    public void testAdd() {
        loader.addSpec(new DummyNakedObjectSpecification()); // for
                                                                                                                            // object

        //NakedObject associate =
        // NakedObjects.getPojoAdapterFactory().createNOAdapter(new
        // InternalObjectForReferencing());
        MockNakedObject associate = new MockNakedObject();
        associate.setupObject(new InternalObjectForReferencing());
        oneToOneAssociation.addAssociation(new DummyIdentifier(), nakedObject, associate);

        assertEquals(associate.getObject(), objectWithVector.added);
    }

    public void testRemove() {
        loader.addSpec(spec);

        MockNakedObject associate = new MockNakedObject();
        associate.setupObject(new InternalObjectForReferencing());
        oneToOneAssociation.removeAssociation(null, nakedObject, associate);

        assertEquals(associate.getObject(), objectWithVector.removed);
    }

    public void testGet() {
        loader.addSpec(spec);
        assertNull(oneToOneAssociation.getAssociations(new DummyIdentifier(), nakedObject));

        objectWithVector.collection = new Vector();
        assertNotNull(oneToOneAssociation.getAssociations(new DummyIdentifier(), nakedObject));
        assertEquals(objectWithVector.collection, oneToOneAssociation.getAssociations(new DummyIdentifier(), nakedObject).getObject());
    }

    public void testName() {
        assertEquals(MEMBERS_FIELD_NAME, oneToOneAssociation.getName());
    }

    public void testAbout() {
        assertFalse(oneToOneAssociation.hasHint());
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
