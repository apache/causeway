package org.nakedobjects.object.reflect.defaults;

import org.nakedobjects.object.ContactTestObject;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationImpl;
import org.nakedobjects.object.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.value.TestClock;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.Member;
import org.nakedobjects.object.reflect.NakedObjectSpecificationException;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.Reflector;

import java.util.Vector;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import com.mockobjects.ExpectationSet;


public class JavaReflectorTest extends TestCase {

    private JavaReflector reflector;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaReflectorTest.class));
    }

    private String[] abouts(Member[] attributes) {
    	Vector v = new Vector();
    	
    	for (int i = 0; i < attributes.length; i++) {
    		Member att = attributes[i];
    		
    		if (att.hasAbout()) {
    			v.addElement(att.getName());
    		}
    	}
    	
    	String[] names = new String[v.size()];
    	
    	v.copyInto(names);
    	
    	return names;
    }

    private String[] associates(Member[] fields) {
        Vector v = new Vector();

        for (int i = 0; i < fields.length; i++) {
	
        	Member member = fields[i];

            if (member instanceof OneToOneAssociation && 
                    ((OneToOneAssociation) member).hasAddMethod()) {
                v.addElement(member.getName());
            }
        }

        String[] names = new String[v.size()];

        v.copyInto(names);

        return names;
    }

    private String[] memberNames(Member[] attributes) {
        String[] names = new String[attributes.length];
        int i = 0;

        for (int j = 0; j < attributes.length; j++) {
            Member member = attributes[i];
            names[i++] = member.getName();
        }

        return names;
	}

    private String[] objectAttributes(Member[] fields) {
        Vector v = new Vector();
        
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] instanceof OneToOneAssociation) {
                v.addElement(fields[i].getName());
            }
        }

        String[] names = new String[v.size()];

        v.copyInto(names);

        return names;
    }

    protected void setUp() throws ClassNotFoundException {
    	LogManager.getLoggerRepository().setThreshold(Level.OFF);
        
    	new NakedObjectSpecificationLoaderImpl();
    	NakedObjectSpecificationImpl.setReflectionFactory(new LocalReflectionFactory());
    	NakedObjectSpecificationImpl.setReflectorFactory(new JavaReflectorFactory());
     	
        new TestClock();
    	
        reflector = new MockJavaReflector(DummyReflectorTestObject.class.getName());
    }

    /**
     * 
     */
    public void testObjectActions() throws NakedObjectSpecificationException {
//         Action[] actions = reflector.actions(false);

    }
    
    public void testFieldSortOrder() throws NakedObjectSpecificationException {
        String[] fields = reflector.fieldSortOrder();
        assertEquals(3, fields.length);
        assertEquals("one", fields[0]);
        assertEquals("two", fields[1]);
        assertEquals("three", fields[2]);
        
    }
    
    public void testActionSortOrder() throws NakedObjectSpecificationException {
        String[] names = reflector.actionSortOrder();
        assertEquals(2, names.length);
        assertEquals("start", names[0]);
        assertEquals("stop", names[1]);
    }
    
    public void testClassActionSortOrder() throws NakedObjectSpecificationException {
        String[] names = reflector.classActionSortOrder();
        assertEquals(2, names.length);
        assertEquals("top", names[0]);
        assertEquals("bottom", names[1]);
    }
    

    public void testAcquire() {
        Naked instance = reflector.acquireInstance();
        assertNotNull(instance);
        assertTrue(instance instanceof DummyReflectorTestObject);
    }
    
   public void testShortName() {
        assertEquals("DummyReflectorTestObject", reflector.shortName());
    }

    public void testPluralName() {
        assertEquals("Plural", reflector.pluralName());
    }

    public void testSingularName() {
        assertEquals("Singular", reflector.singularName());
    }
    
    public void testClassActions() throws NakedObjectSpecificationException, ClassNotFoundException {
//        JavaReflector c = new JavaReflector(ContactTestObject.class.getName());
//        Action[] actions = c.actions(Reflector.CLASS);
    }

    /**
     * @throws ClassNotFoundException
     * 
     */
    public void testCreate() throws NakedObjectSpecificationException, ClassNotFoundException {
        new JavaReflector(ContactTestObject.class.getName());

        try {
            new JavaReflector(Object.class.getName());
            fail("Accepts types other than Naked");
        } catch (NakedObjectSpecificationException ok) {
        }
    }

    /**
     * @throws ClassNotFoundException
     * 
     */
    public void testFields() throws NakedObjectSpecificationException, ClassNotFoundException {
        Member[] fields = reflector.fields();
        
        assertEquals(1, fields.length);
    }

    public void testNameManipulations() {
        assertEquals("CarRegistration", JavaReflector.javaBaseName("getCarRegistration"));
        assertEquals("Driver", JavaReflector.javaBaseName("Driver"));
        assertEquals("Register", JavaReflector.javaBaseName("actionRegister"));
        assertEquals("", JavaReflector.javaBaseName("action"));
    }
}

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
