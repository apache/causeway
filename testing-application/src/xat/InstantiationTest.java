package xat;

import org.nakedobjects.application.system.ExplorationClock;
import org.nakedobjects.example.xat.JavaAcceptanceTestCase;
import org.nakedobjects.reflector.java.fixture.JavaFixture;
import org.nakedobjects.utility.Profiler;
import org.nakedobjects.xat.TestClass;
import org.nakedobjects.xat.TestObject;

import java.util.Locale;

import bom.Booking;
import bom.City;
import bom.CreditCard;
import bom.Customer;
import bom.Location;
import bom.Telephone;


public class InstantiationTest extends JavaAcceptanceTestCase {   
    protected Profiler profiler;

    public static void main(java.lang.String[] args) {
        String free = Profiler.memoryLog();
        System.out.println("startup\t" + free);
        junit.textui.TestRunner.run(InstantiationTest.class);
    }

    public InstantiationTest(String name) {
        super(name);
    }

    protected void setUpFixtures() {
        addFixture(new JavaFixture() {
           
            public void install() {
                profiler = new Profiler("");
                profiler.start();

                Locale.setDefault(Locale.FRANCE);

                ExplorationClock clock = new ExplorationClock();

                registerClass(Booking.class);
                registerClass(City.class);
                registerClass(Customer.class);
                registerClass(Location.class);
                registerClass(CreditCard.class);
                registerClass(Telephone.class);

                createCity("Boston");
                City city = createCity("New York");
                createCity("Chicago");
                createCity("Washington");
                createCity("Philadelphia");

                clock.setDate(2001, 12, 14);

                for (int i = 0; i < 1000; i++) {
                    Location l = (Location) createInstance(Location.class);
                    l.getStreetAddress().setValue(i * 2 + " Road Name");
                    l.setCity(city);
                }

                Customer c = (Customer) createInstance(Customer.class);
                Location l = (Location) createInstance(Location.class);

                c.getFirstName().setValue("Harry");
                c.addToLocations(l);
                profiler.stop();
                System.out.println("Setup " + profiler.timeLog()+ "\t" + Profiler.memoryLog());
            }

            private City createCity(String name) {
                City city = (City) createInstance(City.class);
                city.setName(name);
                return city;
            }
        });
    }

    public void testInstantiate() {
        profiler.start();
        for (int i = 0; i < 20000; i++) {
            setAssociation();
            if (i > 0 && i % 100 == 0) {
                profiler.stop();
                System.out.println(i + "\t" + profiler.timeLog() + "\t" + Profiler.memoryLog());
                profiler.reset();
            }
        }
    }

    public void setAssociation() {
        TestObject city = getTestClass(City.class.getName()).findInstance("New York");
        city.assertFieldContains("name", "New York");
        TestClass testClass = getTestClass(Location.class.getName());

        for (int i = 0; i < 10.; i++) {
            TestObject location = testClass.findInstance("680 Road Name, New York");
            location.clearAssociation("city");
            location.associate("city", city);
        }
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
