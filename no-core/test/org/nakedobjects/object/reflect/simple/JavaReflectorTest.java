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

package org.nakedobjects.object.reflect.simple;

import java.util.Vector;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.nakedobjects.object.ContactTestObject;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.ClassAbout;
import org.nakedobjects.object.reflect.ActionDelegate;
import org.nakedobjects.object.reflect.MemberIf;
import org.nakedobjects.object.reflect.NakedClassException;
import org.nakedobjects.object.reflect.OneToOneAssociationIF;

import com.mockobjects.ExpectationList;
import com.mockobjects.ExpectationSet;


public class JavaReflectorTest extends TestCase {

    private JavaReflector c;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaReflectorTest.class));
    }

    public JavaReflectorTest(String name) {
        super(name);
    }

    private String[] abouts(MemberIf[] attributes) {
    	Vector v = new Vector();
    	
    	for (int i = 0; i < attributes.length; i++) {
    		MemberIf att = attributes[i];
    		
    		if (att.hasAbout()) {
    			v.addElement(att.getName());
    		}
    	}
    	
    	String[] names = new String[v.size()];
    	
    	v.copyInto(names);
    	
    	return names;
    }

    private String[] associates(MemberIf[] fields) {
        Vector v = new Vector();

        for (int i = 0; i < fields.length; i++) {
	
        	MemberIf member = fields[i];

            if (member instanceof OneToOneAssociationIF && 
                    ((OneToOneAssociationIF) member).hasAddMethod()) {
                v.addElement(member.getName());
            }
        }

        String[] names = new String[v.size()];

        v.copyInto(names);

        return names;
    }

    private String[] memberNames(MemberIf[] attributes) {
        String[] names = new String[attributes.length];
        int i = 0;

        for (int j = 0; j < attributes.length; j++) {
            MemberIf member = attributes[i];
            names[i++] = member.getName();
        }

        return names;
	}

    private String[] objectAttributes(MemberIf[] fields) {
        Vector v = new Vector();
        
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] instanceof OneToOneAssociationIF) {
                v.addElement(fields[i].getName());
            }
        }

        String[] names = new String[v.size()];

        v.copyInto(names);

        return names;
    }

    protected void setUp() throws ClassNotFoundException {
    	LogManager.getLoggerRepository().setThreshold(Level.OFF);
    	
        c = new JavaReflector(ContactTestObject.class.getName());
    }

    /**
     * 
     */
    public void testObjectActions() throws NakedClassException {
         ActionDelegate[] actions = c.actions(false);

        // check for all actions
        ExpectationSet exp = new ExpectationSet("object actions");

        // 1 param actions
        exp.addExpected("AddContact");
        exp.addExpected("Renew");

        // 0 param actions
        exp.addExpected("CreateInvoice");
        exp.addExpected("Duplicate");
        exp.addExpected("SetUp");
        exp.addExpected("ResetWorth");

        exp.addExpected("Class");
        exp.addExpected("Clone");
        exp.addExpected("Persist");

        exp.addActualMany(memberNames(actions));
        exp.verify();

        // check for about methods
        exp = new ExpectationSet("abouts for processes");
        exp.addExpected("AddContact");
        exp.addExpected("Duplicate");
        exp.addExpected("SetUp");
        exp.addExpected("ResetWorth");
        exp.addExpected("Persist");
 
        exp.addActualMany(abouts(actions));
        exp.verify();
    }
    
    public void testFieldSortOrder() throws NakedClassException {
        ExpectationList exp = new ExpectationList("actions");
        exp.addExpected("Name");
        exp.addExpected("Address");
        exp.addExpected("Worth");
        exp.addExpected("Is Contact");

        String[] fields = c.fieldSortOrder();
        for (int i = 0; i < fields.length; i++) {
            exp.addActual(fields[i]);
        }
        exp.verify();
    }
    
    public void testActionSortOrder() throws NakedClassException {
        ExpectationList exp = new ExpectationList("actions");
        exp.addExpected("Do Whatever");
        exp.addExpected("Create Invoice");
        exp.addExpected("Duplicate");

        String[] fields = c.actionSortOrder();
        for (int i = 0; i < fields.length; i++) {
            exp.addActual(fields[i]);
        }
        exp.verify();
    }
    
    public void testClassActionSortOrder() throws NakedClassException {
        ExpectationList exp = new ExpectationList("actions");
        exp.addExpected("Class Op");
        exp.addExpected("Do Whatever");
        
        String[] fields = c.classActionSortOrder();
        for (int i = 0; i < fields.length; i++) {
            exp.addActual(fields[i]);
        }
        exp.verify();
    }
    

    public void testAcquire() {
        NakedObject instance = c.acquireInstance();
        assertNotNull(instance);
        assertTrue(instance instanceof ContactTestObject);
    }
    
   public void testShortName() {
        assertEquals("ContactTestObject", c.shortName());
    }

    public void testPluralName() {
        assertEquals("Contacts", c.pluralName());
    }

    public void testSingularName() {
        assertEquals("Contact", c.singularName());
    }
    

    public void testClassAbout() {
        Object about = c.classAbout();
        // if we get the proper About it will be a ClassAbout
        assertEquals(ClassAbout.class, about.getClass());
    }

    public void testClassActions() throws NakedClassException, ClassNotFoundException {
        JavaReflector c = new JavaReflector(ContactTestObject.class.getName());
        ActionDelegate[] actions = c.actions(JavaReflector.CLASS);

        // check for all actions
        ExpectationSet exp = new ExpectationSet("actions");

        // 1 param actions
        exp.addExpected("Example");
        
        // 0 param actions
        exp.addExpected("ClassOp");
        exp.addExpected("DoWhatever");
        exp.addActualMany(memberNames(actions));
        exp.verify();


        // check for about methods
        exp = new ExpectationSet("abouts for actions");
        exp.addExpected("ClassOp");
        exp.addExpected("Example");
       exp.addActualMany(abouts(actions));
        exp.verify();
    }

    /**
     * @throws ClassNotFoundException
     * 
     */
    public void testCreate() throws NakedClassException, ClassNotFoundException {
        new JavaReflector(ContactTestObject.class.getName());

        try {
            new JavaReflector(Object.class.getName());
            fail("Accept types other than Naked");
        } catch (NakedClassException ok) {
        }
    }

    /**
     * @throws ClassNotFoundException
     * 
     */
    public void testFields() throws NakedClassException, ClassNotFoundException {
        JavaReflector c = new JavaReflector(ContactTestObject.class.getName());
        MemberIf[] fields = c.fields();

        // check for all fields
        ExpectationSet exp = new ExpectationSet("fields");

        exp.addExpected("Favourite");
        exp.addExpected("Address");
        exp.addExpected("IsContact");
        exp.addExpected("Worth");
        exp.addExpected("Name");
        exp.addActualMany(memberNames(fields));
        exp.verify();


        // check for set methods on reference objects
        exp = new ExpectationSet("object field");
        exp.addExpected("Favourite");
        exp.addActualMany(objectAttributes(fields));
        exp.verify();


        // check for set methods on reference objects
        exp = new ExpectationSet("abouts for fields");
        exp.addExpected("Name");
        exp.addExpected("Worth");
        exp.addExpected("IsContact");
        exp.addActualMany(abouts(fields));
        exp.verify();


        // check for associate methods on reference objects
        exp = new ExpectationSet("associate methods");
        exp.addExpected("Favourite");
        exp.addActualMany(associates(fields));
        exp.verify();
    }

    public void testNameManipulations() {
        assertEquals("CarRegistration", JavaReflector.baseName("getCarRegistration"));
        assertEquals("Driver", JavaReflector.baseName("Driver"));
        assertEquals("Register", JavaReflector.baseName("actionRegister"));
        assertEquals("", JavaReflector.baseName("process"));

        assertEquals("CarRegistration", JavaReflector.naturalName("CarRegistration"));
        
        assertEquals("ContactObject", JavaReflector.naturalName("ContactObject"));
        assertEquals("getName", JavaReflector.naturalName("getName"));
        assertEquals("getLastName", JavaReflector.naturalName("getLastName"));
        
   //     assertEquals("invalid name", JavaReflector.naturalName(""));


    }
}
