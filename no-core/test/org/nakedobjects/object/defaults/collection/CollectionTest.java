package org.nakedobjects.object.defaults.collection;

import org.nakedobjects.object.ArbitraryNakedCollection;
import org.nakedobjects.object.AssociationExample;
import org.nakedobjects.object.ConcreteEmployee;
import org.nakedobjects.object.ConcreteEmployer;
import org.nakedobjects.object.Employee;
import org.nakedobjects.object.Employer;
import org.nakedobjects.object.IntegrationTestCase;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Person;
import org.nakedobjects.object.Role;
import org.nakedobjects.object.Team;
import org.nakedobjects.object.reflect.AssociationSpecification;

import java.util.Enumeration;

import junit.framework.TestSuite;

import com.mockobjects.ExpectationSet;


public class CollectionTest extends IntegrationTestCase {
    private Employer employer;
    private Employee e1;
    private Employee e2;
    private Employee e3;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(
                new TestSuite(CollectionTest.class));
    }

    protected void setUp() throws Exception {
        super.setUp();

        employer = new ConcreteEmployer();
        employer.setContext(context);
        employer.getCompanyName().setValue("Disney");
        employer.makePersistent();
        
        e1 = new ConcreteEmployee();
        e1.setContext(context);
        e1.getName().setValue("Mickey");
        e1.makePersistent();
        
        e2 = new ConcreteEmployee();
        e2.setContext(context);
        e2.getName().setValue("Pluto");
        e2.makePersistent();
        
        e3 = new ConcreteEmployee();
        e3.setContext(context);
        e3.getName().setValue("Minnie");
        e3.makePersistent();
    }

     public void testAddEmployees() {
        AssociationSpecification att = findAssocation("Employees", employer);

        // add an employee
        assertNull(e1.getEmployer());
        att.setAssociation(employer, e1);
        assertEquals(employer, e1.getEmployer());
        assertFalse("should now have some elements", employer.getEmployees().isEmpty());
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

    public void testArbitraryCollectionVector() {
        ArbitraryNakedCollection team = new ArbitraryCollectionVector(); 
        team.created();

        team.add(e1);
        team.add(e2);

        assertEquals(2, team.size());
        AssociationSpecification association = findAssocation("Intra Company Team", employer);

        association.setAssociation(employer, team);
        assertEquals(team, employer.getIntraCompanyTeam());
    }

    public void testNewObjects() {
        assertEquals("Disney", employer.getCompanyName().title().toString());
        assertTrue("A collection", 
                   employer.getEmployees() instanceof org.nakedobjects.object.defaults.collection.AbstractNakedCollectionVector);
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
        AssociationSpecification association = findAssocation("Employer", e1);

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
    
    public void testLargeCollection() throws ObjectStoreException {
        ArbitraryNakedCollection collection = new ArbitraryCollectionVector();
        Role[] e = setupCollection(collection, 200);
        Enumeration enum = collection.elements();

        int i = 0;

        while (enum.hasMoreElements()) {
            assertEquals(e[i], enum.nextElement());
            i++;
        }

        assertEquals(200, i);
    }


    /**
     *
     */
    public void testInternalCollection() throws ObjectStoreException {
        Team m = new Team();

        InternalCollection collection = m.getMembers();

        Person[] v = new Person[4];

        for (int i = 0; i < v.length; i++) {
            collection.add(v[i] = new Person());
        }

        assertEquals(4, collection.size());

        // try and add another type
        collection.add(new AssociationExample());
        collection.add(new Role());

        assertEquals("Size should be the same as before", 4, collection.size());
    }

    public void testRemove() throws ObjectStoreException {
        ArbitraryNakedCollection collection = new ArbitraryCollectionVector();

        Role[] e = setupCollection(collection, 26);

        for (int i = 5; i < 11; i++) {
            collection.remove(e[i]);
            assertFalse("removed BasicExample " + i, collection.contains(e[i]));
        }
    }

    public void testRemovePersistent() throws ObjectStoreException {
        ArbitraryNakedCollection collection = new ArbitraryCollectionVector();

        Role[] e = setupCollection(collection, 26);

        ExpectationSet set = new ExpectationSet("remove");

        for (int i = 0; i < 5; i++) {
            set.addExpected(e[i]);
        }

        for (int i = 11; i < e.length; i++) {
            set.addExpected(e[i]);
        }

        //
        for (int i = 5; i < 11; i++) {
            collection.remove(e[i]);
            assertTrue("removed BasicExample " + i, !collection.contains(e[i]));
        }

        set.addActualMany(collection.elements());
        set.verify();
    }

     public void testSize() throws ObjectStoreException {
        ArbitraryNakedCollection collection = new ArbitraryCollectionVector();

        setupCollection(collection, 26);

        assertEquals("26 elements added", 26, collection.size());
    }

     private Role[] setupCollection(ArbitraryNakedCollection collection, int size)
        throws ObjectStoreException {
//        collection.setContext(context);
        
        Role[] e = new Role[size];

        for (int i = 0; i < size; i++) {
            e[i] = new Role();
            e[i].created();
            e[i].getName().setValue("A" + i);
            collection.add(e[i]);
            assertTrue("Find added object " + e[i], collection.contains(e[i]));
        }

        assertEquals(size, collection.size());

        return e;
    }
     

     public void testAddTwice() {
         ArbitraryNakedCollection collection = new ArbitraryCollectionVector();
         collection.add(e1);
         collection.add(e1);
         assertEquals(1, collection.size());
     }

     public void testElements() throws ObjectStoreException {
         ArbitraryNakedCollection collection = new ArbitraryCollectionVector();
         collection.add(e1);
         collection.add(e2);
         collection.add(e3);
         
         Enumeration elements = collection.elements();
         Employee element = (Employee) elements.nextElement();
         assertEquals(e1, element);
         
         element = (Employee) elements.nextElement();
         assertEquals(e2, element);
         
         element = (Employee) elements.nextElement();
         assertEquals(e3, element);
         
         assertFalse(elements.hasMoreElements());
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
