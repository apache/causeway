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

package org.nakedobjects.object.collection;

import java.util.Enumeration;

import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.nakedobjects.object.ConcreteEmployee;
import org.nakedobjects.object.ConcreteEmployer;
import org.nakedobjects.object.Employee;
import org.nakedobjects.object.Employer;
import org.nakedobjects.object.MockObjectManager;
import org.nakedobjects.object.NakedObjectTestCase;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.reflect.Association;

import com.mockobjects.ExpectationSet;


public class NakedObjectCollectionAttributeTest
    extends NakedObjectTestCase {
    private Employer employer;
    private Employee e1;
    private Employee e2;
    private Employee e3;

    public NakedObjectCollectionAttributeTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(
                new TestSuite(NakedObjectCollectionAttributeTest.class));
    }

    public void setUp() throws ObjectStoreException {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);

        MockObjectManager manager = MockObjectManager.setup();
        manager.setupAddClass(ConcreteEmployer.class);
        manager.setupAddClass(ConcreteEmployee.class);
        manager.setupAddClass(Employer.class);
        manager.setupAddClass(Employee.class);
        
        employer = new ConcreteEmployer();
        employer.getCompanyName().setValue("Disney");
        employer.makePersistent();
        e1 = new ConcreteEmployee();
        e1.getName().setValue("Mickey");
        e1.makePersistent();
        e2 = new ConcreteEmployee();
        e2.getName().setValue("Pluto");
        e2.makePersistent();
        e3 = new ConcreteEmployee();
        e3.getName().setValue("Minnie");
        e3.makePersistent();
    }

    public void testAddEmployees() {
        Association att = findAssocation("Employees", employer);

        // add an employee
        assertNull(e1.getEmployer());
        att.setAssociation(employer, e1);
        assertEquals(employer, e1.getEmployer());
        assertTrue("has elements", !employer.getEmployees().isEmpty());
        assertTrue("Collection should contain the 1st employer as an element", 
                   ((InternalCollection) att.get(employer)).contains(e1));

        // add 2nd employee
        assertNull(e2.getEmployer());
        att.setAssociation(employer, e2);
        assertEquals(employer, e2.getEmployer());
        assertTrue("Collection should contain the 2nd employer as an element", 
                   ((InternalCollection) att.get(employer)).contains(e2));

        // add 3rd employee
        assertNull(e3.getEmployer());
        att.setAssociation(employer, e3);
        assertEquals(employer, e3.getEmployer());

        // check set
        ExpectationSet set = new ExpectationSet("employees has 3 elements.");
        set.addExpected(e1);
        set.addExpected(e2);
        set.addExpected(e3);
        
        Enumeration enum = employer.getEmployees().elements();
        while (enum.hasMoreElements()) {
			enum.nextElement();
			
			}
			
        set.addActualMany(employer.getEmployees().elements());
        set.verify();

        att.clearAssociation(employer, e2);

        // check set 2
        ExpectationSet set2 = new ExpectationSet(
                                      "employees now has 2 elements.");
        set2.addExpected(e1);
        set2.addExpected(e3);
        set2.addActualMany(employer.getEmployees().elements());
        set2.verify();
    }

    public void testCollectionObject() {
        ArbitraryCollection team = new ArbitraryCollection(); //(ArbitraryCollection)e1.getObjectStore().newInstance(ArbitraryCollection.class);

        team.created();


        //team.setType(Employee.class);
        team.add(e1);
        team.add(e2);

        //	assertEquals(2, team.size());
        Association association = findAssocation("Intra Company Team", employer);

        association.setAssociation(employer, team);

        assertEquals(team, employer.getIntraCompanyTeam());
    }

    public void testNewObjects() {
        assertEquals("Disney", employer.getCompanyName().title().toString());
        assertTrue("A collection", 
                   employer.getEmployees() instanceof org.nakedobjects.object.collection.AbstractNakedCollection);
        assertEquals("Mickey", e1.getName().title().toString());
        assertEquals(null, e1.getEmployer());
        assertEquals("Pluto", e2.getName().title().toString());
        assertEquals(null, e2.getEmployer());
        assertEquals("Minnie", e3.getName().title().toString());
        assertEquals(null, e3.getEmployer());
    }

    public void testSetEmployer() {
        // check no employees collection
        ExpectationSet set = new ExpectationSet("employees empty");

        set.addActualMany(employer.getEmployees().elements());
        set.verify();

        // set employer
        Association association = findAssocation("Employer", e1);

        assertNull(e1.getEmployer());
        association.setAssociation(e1, employer);
        assertEquals(employer, e1.getEmployer());

        // check employees collection
        ExpectationSet set2 = new ExpectationSet("employees has back link");

        set2.addExpected(e1);
        set2.addActualMany(employer.getEmployees().elements());
        set2.verify();


        // clear employer
        association.clearAssociation(e1, employer);
        assertNull(e1.getEmployer());


        // check empty
        set.addActualMany(employer.getEmployees().elements());
        set.verify();
    }
}
