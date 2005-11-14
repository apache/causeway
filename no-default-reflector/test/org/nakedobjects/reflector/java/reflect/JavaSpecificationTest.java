package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.application.NonPersistable;
import org.nakedobjects.object.Persistable;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.TestSystem;

import junit.framework.TestCase;

public class JavaSpecificationTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JavaSpecificationTest.class);
    }


    private TestSystem system;
    

    protected void setUp() throws ClassNotFoundException {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);
        
        system = new TestSystem();
        system.init();
        system.addSpecification(new DummyNakedObjectSpecification());
    }


    protected void tearDown() throws Exception {
        system.shutdown();
    }
    
    public void testPersistable() throws Exception {
        system.addSpecification(new DummyNakedObjectSpecification(Object.class.getName()));
        system.addSpecification(new DummyNakedObjectSpecification(Interface1.class.getName()));
        system.addSpecification(new DummyNakedObjectSpecification(Interface2.class.getName()));
        
        JavaSpecification spec = new JavaSpecification(JavaObjectForReflector.class, new DummyBuilder());
        spec.introspect();
        assertEquals(Persistable.USER_PERSISTABLE, spec.persistable());
    }
    
    public void testNotPersistable() throws Exception {
        system.addSpecification(new DummyNakedObjectSpecification(Object.class.getName()));
        system.addSpecification(new DummyNakedObjectSpecification(Interface1.class.getName()));
        system.addSpecification(new DummyNakedObjectSpecification(Interface2.class.getName()));
        system.addSpecification(new DummyNakedObjectSpecification(NonPersistable.class.getName()));
        
        JavaSpecification spec = new JavaSpecification(JavaObjectForReflectorTransient.class, new DummyBuilder());
        spec.introspect();
        assertEquals(Persistable.TRANSIENT, spec.persistable());

    }

}


/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */