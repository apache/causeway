package org.nakedobjects.object.reflect.internal;

import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.object.NakedError;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectSpecificationException;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.defaults.InternalNakedObject;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoader;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.DummyPojoAdapterFactory;
import org.nakedobjects.object.reflect.FieldPeer;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class InternalReflectorTest extends TestCase {

    private InternalReflector reflector;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(InternalReflectorTest.class));
    }
 
    protected void setUp() throws ClassNotFoundException {
    	LogManager.getLoggerRepository().setThreshold(Level.OFF);
        
    	new MockNakedObjectSpecificationLoader();     	
    	
    	NakedObjectLoader objectLoader = new DummyPojoAdapterFactory();
    	new NakedObjectsClient().setObjectLoader(objectLoader);
		
        reflector = new MockInternalReflector(InternalObjectForReflector.class.getName());
    }

    public void testObjectActions() throws NakedObjectSpecificationException {
         ActionPeer[] actions = reflector.actionPeers(false);
         assertEquals(1, actions.length);
         
         ActionPeer member = (ActionPeer) actions[0];
         assertEquals("MethodOne", member.getName());
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
    
   public void testShortName() {
        assertEquals("InternalObjectForReflector", reflector.shortName());
    }

    public void testPluralName() {
        assertEquals("Plural", reflector.pluralName());
    }

    public void testClassAbout() {
        Hint about = reflector.classHint();
        assertEquals(about, InternalObjectForReflector.about);
    }


    public void testSingularName() {
        assertEquals("Singular", reflector.singularName());
    }
    
    public void testClassActions() throws NakedObjectSpecificationException, ClassNotFoundException {
//        JavaReflector c = new JavaReflector(ContactTestObject.class.getName());
//        Action[] actions = c.actions(Reflector.CLASS);
    }

    public void testCreate() throws Exception {
        new InternalReflector(InternalObjectForReflector.class.getName());

        try {
            new InternalReflector(NonInternalObject.class.getName());
            fail("Accepts types other than InternalNakedObject");
        } catch (NakedObjectSpecificationException ok) {
        }
    }

    public void testFields() throws Exception {
        FieldPeer[] fields = reflector.fields();
        
        assertEquals(1, fields.length);
        
        InternalOneToOneAssociation member = (InternalOneToOneAssociation) fields[0];
        assertEquals("Value", member.getName());
//        assertEquals("getValue", member.getMethod().getName());
        assertEquals("aboutValue", member.getAboutMethod().getName());
//        assertEquals("validValue", member.getValidMethod().getName());
    }

    public void testSuperclass() {
        assertEquals(InternalObjectForReflectorSuperclass.class.getName(), reflector.getSuperclass());
    }
    
    
    public void testInterfaces() {
        String[] interfaces = reflector.getInterfaces();
        assertEquals(2, interfaces.length);
        assertEquals(NakedError.class.getName(), interfaces[0]);
        assertEquals(InternalNakedObject.class.getName(), interfaces[1]);
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
